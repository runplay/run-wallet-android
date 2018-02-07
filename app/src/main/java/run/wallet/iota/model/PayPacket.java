package run.wallet.iota.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Checksum;

/**
 * Created by coops on 13/01/18.
 */

public class PayPacket {

    private static final PayPacket packet = new PayPacket();

    private long value;
    private List<String> payto=new ArrayList();
    private List<Address> payfrom=new ArrayList();
    private Address remainer;
    private boolean breakPig=false;
    private boolean isValid=false;
    private String error;


    public static boolean createPayPacket(long value, String payto) {
        List<String> to=new ArrayList<String>();
        try {
            if(jota.utils.Checksum.isAddressWithChecksum(payto)) {
                payto= jota.utils.Checksum.removeChecksum(payto);
            }
        } catch(Exception e) {}
        to.add(payto);
        return createPayPacket(value,to);
    }
    private static boolean createPayPacket(long value, List<String> payto) {
        clear();
        if(value>0 && payto!=null && !payto.isEmpty()) {
            packet.payto.addAll(payto);
            findApplyAddresses(value);
            checkValid();

            //Log.e("PAYPACK","pay: "+packet.value+", to: "+packet.payto.get(0)
            //        +", pig: "+packet.breakPig+", valid: "+packet.isValid
            //        +", from: "+packet.payfrom.size()+" - ");
        }
        return packet.isValid;
    }
    private static void clear() {
        packet.value=0;
        packet.remainer=null;
        packet.payfrom.clear();
        packet.payto.clear();
        packet.isValid=false;
        packet.breakPig=false;
        packet.error=null;
    }
    public static String getError() {
        return packet.error;
    }
    public static boolean isBreakPig() {
        return packet.breakPig;
    }
    public static boolean isValid() {
        return packet.isValid;
    }

    public static class AvailableBalances {
        public long available;
        public long locked;
        private AvailableBalances(){}
    }
    public static AvailableBalances calculateAvailableBalances() {
        List<Address> stored=Store.getAddresses();
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
        packet.value=forPayValue;

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
                for(String addstr: getPayTo()) {
                    if(address.getAddress().startsWith(addstr)) {
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
                            //Log.e("ADD", "add lowerpig");
                            lowerpigs.add(address);
                        } else {
                            //Log.e("ADD", "add loweropen");
                            loweropen.add(address);
                        }
                    }
                }
            }
        }

        // traverse smaller to total value
        for(Address lower: loweropen) {
            uselower.add(lower);
            //Log.e("ADD","match loweropen: "+totalAddresses(uselower));
            if(totalAddresses(uselower)>=forPayValue) {
                break;
            }
        }
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
            //Log.e("ADD","use is empty, mark invalid, break pig: "+packet.breakPig);
        }
        // find remainder
        for(Address address: stored) {
            if(Store.isAlreadyAddress(address,getPayFrom())==null) {
                if(address.isAttached() && !address.isUsed() && !address.isPig() && address.getValue()==0 && address.getPendingValue()==0) {
                    boolean okgo=true;
                    for(String addstr: getPayTo()) {
                        if(address.getAddress().equals(addstr)) {
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
        /*

                for(Address address: stored) {
            if(Store.isAlreadyAddress(address,use)==null) {
                if(address.isAttached() && !address.isUsed() && !address.isPig() && address.getValue()==0 && address.getPendingValue()==0) {
                    boolean okgo=true;
                    for(String addstr: getPayTo()) {
                        if(address.getAddress().equals(addstr)) {
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




        if(packet.remainer==null) {
            for(Address address: stored) {
                if(Store.isAlreadyAddress(address,use)==null) {
                    if(address.isAttached() && !address.isUsed()) {
                        boolean okgo=true;
                        for(String addstr: getPayTo()) {
                            if(address.getAddress().startsWith(addstr)) {
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
        */
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
        if(getValue()<=0) {
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
            if(covered<packet.value) {
                valid=false;
                packet.error="From addresses total value does not cover the send value";
            }
        }
        //Log.e("PKT","packet error: "+packet.error);
        packet.isValid=valid;
    }
    public static final long getValue() {
        return packet.value;
    }
    public static final List<String> getPayTo() {
        return packet.payto;
    }
    public static final List<Address> getPayFrom() {
        return packet.payfrom;
    }
    public static final Address getRemainder() {
        return packet.remainer;
    }
}
