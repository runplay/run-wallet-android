package run.wallet.iota.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jota.model.Bundle;
import jota.model.Transaction;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.model.Address;

import run.wallet.iota.model.NudgeTransfer;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.TransferTransaction;
import run.wallet.iota.model.Wallet;

/**
 * Created by coops on 05/01/18.
 */

public class Audit {
    private static final String defmessage = "";

    public static void processNudgeAttempts(Context context, Seeds.Seed seed, List<Transfer> transfers) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int nudgeAttempts = Sf.toInt(prefs.getString(Constants.PREF_TRANSFER_NUDGE_ATTEMPTS, "" + Constants.PREF_TRANSFER_NUDGE_ATTEMPTS_VALUE));

        Collections.reverse(transfers);
        List<NudgeTransfer> alreadyNudge=Store.getNudgeTransfers();
        Map<String, Boolean> completedLookup = new HashMap<>();
        for (Transfer transfer : transfers) {
            if (transfer.isCompleted()) {
                completedLookup.put(transfer.getValue() + "" + transfer.getAddress(), true);
            }
        }
        for (Transfer transfer : transfers) {
            if (!transfer.isCompleted() && completedLookup.get(transfer.getValue() + "" + transfer.getAddress()) == null
                    ) {

                if (transfer.getValue()<=0
                        && !transfer.isMarkDoubleAddress()
                        && !transfer.isMarkDoubleSpend()
                        && !transfer.getTransactions().isEmpty()
                        && transfer.getNudgeCount() < nudgeAttempts) {

                    NodeInfoResponse nir = Store.getNodeInfo();
                    if (nir != null) {
                        Store.addIfNoNudgeTransfer(context, seed, transfer);
                    }
                }
            } else if(transfer.isCompleted()) {
                for(NudgeTransfer nudge: alreadyNudge) {
                    if(transfer.getHash().equals(nudge.transfer.getHash())) {

                        List<NudgeTransfer> remove=new ArrayList<>();
                        remove.add(nudge);
                        Store.removeNudgeTransfer(context,remove);
                    }
                }
            }
        }
        Store.saveNudgeTransfers(context);

        Collections.reverse(transfers);

    }

    public static void populateTxToTransfers(List<Transaction> inTransactions, NodeInfoResponse nodeInfo, List<Transfer> transfers, List<Address> allAddresses) {
        long totalValueTransfer = 0;
        long timestamp = 0;
        String address = "";
        String hash = "";
        Boolean persistence = false;
        long value = 0;
        String tag = "";
        String destinationAddress = "";
        List<TransferTransaction> transactions=new ArrayList<>();
        List<TransferTransaction> othertransactions=new ArrayList<>();
        String message = defmessage;
        for (Transaction trx : inTransactions) {
            try {

                address = trx.getAddress();
                persistence = trx.getPersistence();
                value = trx.getValue();

                Address hasAddress = Store.isAlreadyAddress(address, allAddresses);
                if(hasAddress!=null) {
                    hasAddress.setAttached(true);
                }
                if (value != 0 && hasAddress != null) {
                    transactions.add(new TransferTransaction(hasAddress.getAddress(),value));

                    totalValueTransfer+=value;
                } else if(value!=0) {
                    othertransactions.add(new TransferTransaction(trx.getAddress(),value));
                }

                if (trx.getCurrentIndex() == 0) {
                    message = Utils.removeTrailingNines(trx.getSignatureFragments());
                    timestamp = trx.getAttachmentTimestamp();
                    tag = trx.getTag();
                    destinationAddress = address;
                    hash = trx.getHash();
                }
                if (hasAddress!=null) {
                    boolean isRemainder = (trx.getCurrentIndex() == trx.getLastIndex()) && trx.getLastIndex() != 0;
                    if (value < 0 && !isRemainder) {
                        hasAddress.setUsed(true);
                    } else {
                        //hasAddress.setUsed(false);
                        hasAddress.setAttached(true);
                    }
                }
            } catch (Exception e) {
                Log.e("AUDIT","exception: "+e.getMessage());
            }
        }

        Transfer addtransfer = new Transfer(timestamp, destinationAddress, hash, persistence, totalValueTransfer, message, tag);

        if(nodeInfo!=null) {
            addtransfer.setMilestone(nodeInfo.getLatestMilestoneIndex());
            addtransfer.setMilestoneCreated(nodeInfo.getLatestMilestoneIndex());
        }
        addtransfer.setTransactions(transactions);
        addtransfer.setOtherTransactions(othertransactions);
        transfers.add(addtransfer);
    }

    public static void bundlePopulateTransfers(Bundle[] transferBundle, List<Transfer> transfers, List<Address> allAddresses) {
        if (transferBundle != null) {
            NodeInfoResponse nodeInfo = Store.getNodeInfo();
            for (Bundle aTransferBundle : transferBundle) {
                //Log.e("TRAN","\n\n"+aTransferBundle.getTransactions().size()+" - "+aTransferBundle.toString()+"\n");
                //long totalValue = 0;
                populateTxToTransfers(aTransferBundle.getTransactions(),nodeInfo,transfers,allAddresses);

            }
        }
    }
    public static void setTransfersToAddresses(Seeds.Seed seed, List<Transfer> transfers, List<Address> allAddresses, Wallet wallet, List<Transfer> addinTransfers) {

        for(Address add: allAddresses) {
            if(add.getValue()!=0||add.getPendingValue()!=0)
                add.setUsed(false);
            //add.setValue(0);
            add.setPendingValue(0);
            if(!add.isPigUser()) {
                add.setPigInt(0);
            }
        }


        if(addinTransfers!=null) {
            List<Transfer> allTransfers = new ArrayList<>();
            for (Transfer tran : addinTransfers) {
                Transfer already = Store.isAlreadyTransfer(tran, transfers);
                if (already == null) {
                    allTransfers.add(tran);
                } else {
                    //already.setMarkDoubleAddress(tran.isMarkDoubleAddress());
                    already.setLastDoubleCheck(tran.getLastDoubleCheck());
                    already.setMilestone(tran.getMilestone());
                    already.setMilestoneCreated(tran.getMilestoneCreated());
                    already.setNudgeHashes(tran.getNudgeHashes());
                }
            }
            transfers.addAll(allTransfers);
        }

        if(!transfers.isEmpty()) {
            ProcessResult result = processTransfersToAddresses(seed,transfers, allAddresses);
            Collections.sort(transfers);
            wallet.setBalance(result.seedTotal);
            wallet.setBalancePendingIn(result.seedTotalPendingIn);
            wallet.setBalancePendingOut(result.seedTotalPendingOut);
        }
        wallet.setLastUpdate(System.currentTimeMillis());
    }

    private static class ProcessResult {

        long seedTotalPendingIn=0;
        long seedTotalPendingOut=0;
        long seedTotal=0;
    }
    private static ProcessResult processTransfersToAddresses(Seeds.Seed seed, List<Transfer> transfers, List<Address> allAddresses) {
        Collections.sort(transfers);
        Collections.reverse(transfers);

        HashMap<String,Transfer> completed=new HashMap<String,Transfer>();
        HashMap<String,Transfer> completedAddresses=new HashMap<String,Transfer>();
        for (Transfer transfer: transfers) {
            if(!transfer.getTransactions().isEmpty()) {
                String key = transfer.getValue() + transfer.getAddress();
                Transfer comp = completed.get(key);
                if (comp == null) {
                    completed.put(key, transfer);
                } else {
                    if (comp.isCompleted()) {
                        transfer.setMarkDoubleSpend(true);
                    } else {
                        comp.setMarkDoubleSpend(true);
                        completed.put(key, transfer);
                    }
                }
                if (transfer.isCompleted()) {
                    transfer.setMarkDoubleAddress(false);
                    transfer.setMarkDoubleSpend(false);
                    if (transfer.getTimestampConfirmed() == 0)
                        transfer.setTimestampConfirmed(System.currentTimeMillis());
                    for (TransferTransaction trans : transfer.getTransactions()) {
                        if (trans.getValue() < 0) {
                            //Log.e("AUDIT","comp address: "+trans.getValue()+" - "+trans.getAddress());
                            completedAddresses.put(trans.getAddress(), transfer);
                        }
                    }
                }
            }
        }


        for(Transfer transfer: transfers) {
            if(!transfer.isCompleted() && !transfer.isMarkDoubleSpend()) {
                //Log.e("AUDIT","check: "+transfer.getValue()+" - "+transfer.getHash());
                List<String> testAddress=new ArrayList<>();
                for(TransferTransaction trans: transfer.getTransactions()) {
                    if(trans.getValue()<0) {
                        //Log.e("AUDIT","-address: "+trans.getValue()+" - "+trans.getAddress());
                        testAddress.add(trans.getAddress());
                    }
                }
                if(!testAddress.isEmpty()) {
                    for(String testadd: testAddress) {
                        Transfer completedOnTransfer = completedAddresses.get(testadd);
                        if(completedOnTransfer!=null
                                && !completedOnTransfer.getHash().equals(transfer.getHash())) {
                            Address tmpadd=Store.isAlreadyAddress(testadd,allAddresses);
                            if(tmpadd!=null && tmpadd.getValue()==0) {
                                //Log.e("AUDIT", "completedOnTransfer: " + completedOnTransfer.getValue() + " - " + completedOnTransfer.getAddress());
                                transfer.setMarkDoubleAddress(true);
                            }
                        }
                    }
                }
            }
        }


        long seedTotalPendingIn=0;
        long seedTotalPendingOut=0;

        List<TransferTransaction> already=new ArrayList<>();

        for(Transfer transfer: transfers) {

            if(transfer.isCompleted()) {
                for(TransferTransaction trans: transfer.getTransactions()) {
                    Address address = Store.isAlreadyAddress(trans.getAddress(),allAddresses);
                    if (address!=null) {
                        //address.setValue(address.getValue() + trans.getValue());
                        if (trans.getValue() <0) {
                            address.setUsed(true);

                        }
                    }
                }
            } else if(!transfer.isCompleted()) {
                if(transfer.isInternal() || (!transfer.isMarkDoubleSpend() && !transfer.isMarkDoubleAddress())) {
                    for(TransferTransaction trans: transfer.getTransactions()) {
                        Address address = Store.isAlreadyAddress(trans.getAddress(),allAddresses);
                        if (address!=null) {
                            address.setPendingValue(address.getPendingValue() + trans.getValue());
                            if (trans.getValue() < 0) {
                                seedTotalPendingOut += trans.getValue();
                            } else {
                                seedTotalPendingIn += trans.getValue();
                            }
                            already.add(trans);

                        }
                    }
                }
            }
        }

        long seedTotal=0;
        for(Address address: allAddresses) {

            seedTotal+=address.getValue();
            if(address.getPendingValue()!=0 && !address.isPigUser()) {
                address.setPigInt(1);
            }
        }
        //Log.e("SEED-TOTAL",seedTotal+" - pending out: "+seedTotalPendingOut);

        ProcessResult result = new ProcessResult();
        result.seedTotal=seedTotal;
        result.seedTotalPendingIn=seedTotalPendingIn;
        result.seedTotalPendingOut=seedTotalPendingOut;
        return result;
    }

}



