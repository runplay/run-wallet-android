package run.wallet.iota.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Checksum;

/**
 * Created by coops on 13/01/18.
 */

public class PayPacket {

    private static PayPacket packet = new PayPacket();


    private List<PayTo> payto=new ArrayList();
    private List<Address> payfrom=new ArrayList();
    private Address remainer;
    private boolean breakPig=false;
    private boolean isValid=false;

    private String error;



    public static void setErrorMessage(String error) {
        packet.error=error;
    }

    public static class PayTo {
        public long value;
        public String address;
        public boolean failCheck;
        public PayTo(long value, String address) {
            this.value=value; this.address=address;
        }
    }

    public static long getTotalToPay() {
        long total=0L;
        for(PayTo pt: packet.payto) {
            total+=pt.value;
        }
        return total;
    }
    public static void removePayTo(PayTo pt) {
        packet.payto.remove(pt);
    }
    public static void removePayTo(int index) {
        packet.payto.remove(index);
    }
    public static void addPayTo(PayTo pt) {

        try {
            if(jota.utils.Checksum.isAddressWithChecksum(pt.address)) {
                pt.address= jota.utils.Checksum.removeChecksum(pt.address);
            }
        } catch(Exception e) {}
        if(pt.address.length()==81 && pt.address.matches("^[A-Z9]+$")) {
            packet.payto.add(pt);
        }
    }
    public static boolean updatePayPacket() {
        findApplyAddresses(getTotalToPay());
        checkValid();
            //Log.e("PAYPACK","pay: "+getTotalToPay()+", to: "+packet.payto.get(0).address +", pig: "+packet.breakPig+", valid: "+packet.isValid +", from: "+packet.payfrom.size()+" - ");
        return isValid();
    }
    public static void start() {
        clear();
    }
    public static void clear() {
        packet=new PayPacket();
        /*
        packet.remainer=null;
        packet.payfrom.clear();
        packet.payto.clear();
        packet.isValid=false;
        packet.breakPig=false;
        packet.error=null;
        */
    }
    public static String getError() {
        return packet.error;
    }
    public static boolean isBreakPig() {
        return packet.breakPig;
    }
    public static boolean isValid() {
        for(PayTo pt: getPayTo()) {
            if(pt.failCheck)
                return false;
        }
        return packet.isValid;
    }

    public static class AvailableBalances {
        public long available;
        public long locked;
        private AvailableBalances(){}
    }
    public static synchronized AvailableBalances calculateAvailableBalances() {
        List<Address> stored=new ArrayList<>();
        stored.addAll(Store.getAddresses());
        AvailableBalances balances=new AvailableBalances();
        for(Address address: stored) {
            if(!address.isPig() && !address.isUsed() && address.getValue()>0 && address.getPendingValue()==0) {
                balances.available+=address.getValue();
            } else if(!address.isPig() && address.isUsed() && address.getValue()>0 && (address.getValue()+address.getPendingValue())>0) {
                balances.available+=(address.getValue()+address.getPendingValue());
            } else  {
                balances.locked+=address.getValue()+address.getPendingValue();
            }
        }
        return balances;
    }

    private static void findApplyAddresses(long forPayValue) {
        packet.payfrom.clear();
        packet.remainer=null;
        packet.isValid=false;
        packet.breakPig=false;
        packet.error=null;

        List<Address> use=new ArrayList<>();
        List<Address> higherpigs=new ArrayList<>();
        List<Address> lowerpigs=new ArrayList<>();
        List<Address> loweropen=new ArrayList<>();
        List<Address> higheropen=new ArrayList<>();

        List<Address> stored=Store.getAddresses();

        Address perfect=null;
        List<Address> uselower=new ArrayList<>();
        if(stored!=null && !stored.isEmpty()) {
            for(Address address: stored) {
                boolean okgo=true;
                for(PayTo addstr: packet.payto) {
                    if((address.isUsed() && address.getValue()==0) || address.getAddress().startsWith(addstr.address)) {
                        okgo=false;
                        break;
                    }
                }
                if(okgo && address.getValue()>0) {
                    if (address.getValue() >= forPayValue) {
                        if (address.isPig()) {
                            higherpigs.add(address);
                        } else {
                            higheropen.add(address);
                            if (perfect == null || perfect.getValue() > address.getValue()) {
                                perfect = address;
                            }
                        }
                    } else {
                        if (address.isPig()) {
                            lowerpigs.add(address);
                        } else {
                            loweropen.add(address);
                        }
                    }
                }
            }
        }
        List<List<Address>> perms=permute(loweropen,2);
        List<Address> lowest = null;
        for(List<Address> plist:perms) {
            if(lowest==null)
                lowest=plist;
            long total=totalAddresses(lowest);
            List<Address> tmpuselower=new ArrayList<>();
            for(Address lower: plist) {
                tmpuselower.add(lower);
                //Log.e("ADD","match loweropen: "+totalAddresses(uselower));
                long tmpTotal=totalAddresses(tmpuselower);
                if(tmpTotal>=forPayValue && tmpTotal<total) {
                    lowest=tmpuselower;
                    break;
                }
            }
        }
        uselower.addAll(lowest);
        if(perfect!=null) {
            long totallower=totalAddresses(uselower);
            //Log.e("ADD","match perfect");
            if(totallower>=forPayValue) {
                if(totallower<=perfect.getValue()) {
                    use.addAll(uselower);
                }
            }

            if(use.isEmpty()) {
                //Log.e("ADD","match perfect is best");
                use.add(perfect);
            }
        }
        if(use.isEmpty()) {
            if(!uselower.isEmpty()) {
                use.addAll(uselower);
            }
        }
        if(use.isEmpty()) {
            //Log.e("ADD","use is empty");
            if(!higheropen.isEmpty()) {
                //Log.e("ADD","add higheropen");
                use.add(higheropen.get(0));
            }
        }
        if(!use.isEmpty()) {
            packet.isValid=true;
            packet.payfrom.addAll(use);
            //Log.e("ADD","use is ok, mark valid");
        } else {
            packet.isValid=false;
            // use is empty needs a Pig
            if (!higherpigs.isEmpty()) {
                packet.breakPig = true;
            } else {
                long totallower = totalAddresses(lowerpigs);
                if (totallower >= forPayValue) {
                    packet.breakPig = true;
                }
            }
        }

        // find remainder
        for(Address address: stored) {
            if(Store.isAlreadyAddress(address,getPayFrom())==null) {
                if(address.isAttached() && !address.isUsed() && !address.isPig() && address.getValue()==0 && address.getPendingValue()==0) {
                    boolean okgo=true;
                    for(PayTo addstr: packet.payto) {
                        if(address.getAddress().equals(addstr.address)) {
                            okgo=false;
                            break;
                        }
                    }

                    //Log.e("ADD","use remainder ok");
                    if(okgo) {
                        packet.remainer = address;
                        break;
                    }
                }
            }
        }
    }
    public static List<List<Address>> permute(List<Address> addresses, int size) {
        List<List<Address>> permutations = new ArrayList<List<Address>>();
        permutations.add(new ArrayList<Address>());

        for ( int i = 0; i < addresses.size(); i++ ) {
            List<List<Address>> current = new ArrayList<List<Address>>();
            for ( List<Address> permutation : permutations ) {
                for ( int j = 0, n = permutation.size() + 1; j < n; j++ ) {
                    List<Address> temp = new ArrayList<Address>(permutation);
                    temp.add(j, addresses.get(i));
                    current.add(temp);
                }
            }
            permutations = new ArrayList<List<Address>>(current);
        }

        return permutations;
    }
    private static long totalAddresses(List<Address> addresses) {
        long total=0;
        for(Address add: addresses) {
            total+=add.getValue();
        }
        return total;
    }

    public static final void checkValid() {
        boolean valid=true;
        if(getTotalToPay()<=0) {
            valid=false;
            packet.error="Value must be positive amount";
        } else if(packet.payto==null || packet.payto.isEmpty()) {
            valid=false;
            packet.error="No payee's";
        } else if(packet.payfrom==null || packet.payfrom.isEmpty()) {
            valid=false;
            packet.error="Not enough IOTA";
            if(packet.breakPig) {
                packet.error+=", break a Pig to enable funds";
            }
        } else if(packet.remainer==null) {
            valid=false;
            packet.error="Remainder address needed, see receive area";
        } else {
            long covered=0;
            for(Address add: packet.payfrom) {
                covered+=add.getValue();
            }
            if(covered<getTotalToPay()) {
                valid=false;
                packet.error="Not enough funds to pay the send value";
            }
        }
        //Log.e("PKT","packet error: "+packet.error);
        packet.isValid=valid;
    }

    public static final List<PayTo> getPayTo() {
        return packet.payto;
    }
    public static final List<Address> getPayFrom() {
        return packet.payfrom;
    }
    public static final Address getRemainder() {
        return packet.remainer;
    }
}
