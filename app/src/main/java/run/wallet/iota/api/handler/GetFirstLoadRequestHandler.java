/*
 * Copyright (C) 2017 IOTA Foundation
 *
 * Authors: pinpong, adrianziser, saschan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package run.wallet.iota.api.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jota.RunIotaAPI;
import jota.dto.response.GetBalancesResponse;
import jota.dto.response.GetNewAddressResponse;
import jota.model.Bundle;
import jota.utils.SeedRandomGenerator;
import run.wallet.common.Sf;
import run.wallet.iota.api.requests.ApiRequest;

import run.wallet.iota.api.requests.GetFirstLoadRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetFirstLoadResponse;
import run.wallet.iota.helper.Audit;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.TransferTransaction;
import run.wallet.iota.model.Wallet;
import run.wallet.iota.service.AppService;

public class GetFirstLoadRequestHandler extends IotaRequestHandler {
    public GetFirstLoadRequestHandler(RunIotaAPI iotaApi, Context context) {
        super(iotaApi, context);
    }
    private static final String defmessage = "";


    //public static int countaddresses;
    //public static int stageCompletedCount=0;
    private static HashMap<String,FirstTimeHolder> holders=new HashMap<>();

    public static FirstTimeHolder getHolder(String seedId) {
        return holders.get(seedId);
    }
    public static void setUserConfirm(String seedId,boolean userConfirm) {
        FirstTimeHolder fth=holders.get(seedId);
        if(fth!=null)
            fth.userConfirmedBalance=userConfirm;
    }
    public static class FirstTimeHolder {
        public long predictaddress=0;
        public String predictstr="";
        public boolean showWaitMessage=false;

        public int countaddress=0;
        public int counttransfers=0;
        public boolean isFinished=false;
        public Boolean userConfirmedBalance=null;
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return GetFirstLoadRequest.class;
    }


    @Override
    public ApiResponse handle(ApiRequest request) {
        FirstTimeHolder holder=new FirstTimeHolder();
        //StopWatch stopWatch = new StopWatch();
        GetFirstLoadRequest firstLoadRequest = (GetFirstLoadRequest) request;

        Wallet wallet=null;
        List<Address> allAddresses = new ArrayList<>();
        List<Transfer> transfers=new ArrayList<>();


        long started=System.currentTimeMillis();
        if(!firstLoadRequest.getSeed().isappgenerated) {
            holders.put(firstLoadRequest.getSeed().id,holder);

            while (holder.userConfirmedBalance == null || started < System.currentTimeMillis() - 60000) {

                try {
                    this.wait(1000);
                } catch (Exception e) {
                }

            }

        } else {
            holder.userConfirmedBalance=false;
            holders.put(firstLoadRequest.getSeed().id,holder);
        }

        boolean userDeclaredBalance=holder.userConfirmedBalance==null?false:holder.userConfirmedBalance.booleanValue();
        holder.userConfirmedBalance=userDeclaredBalance?Boolean.TRUE:Boolean.FALSE;
//Log.e("FL","uc="+holder.userConfirmedBalance+" - ud="+userDeclaredBalance);
        if(!userDeclaredBalance || firstLoadRequest.getSeed().isappgenerated) {

            try {
                final GetNewAddressResponse gna = apiProxy.getNewAddress(String.valueOf(Store.getSeedRaw(context,firstLoadRequest.getSeed())), firstLoadRequest.getSecurity(),
                        0, false, 1, false);
                for (String add : gna.getAddresses()) {
                    Address newaddress = new Address(add, false, false);
                    newaddress.setIndexName(1);
                    allAddresses.add(newaddress);
                }
            } catch (Exception e) {}

            wallet=new Wallet(firstLoadRequest.getSeed().id,0,System.currentTimeMillis());


        } else {
            long timestamp=System.currentTimeMillis();
            holder.showWaitMessage=true;
            holder.predictaddress=0;
            Map<Integer,Address> already=new HashMap<>();
            int start=0;
            boolean stop=false;

            List<Bundle> allbundles=new ArrayList<>();
            Map<String,Boolean> hasalready=new HashMap<>();
            wallet = new Wallet(((GetFirstLoadRequest) request).getSeed().id,0,System.currentTimeMillis());
            List<Transfer> addInTransfers = new ArrayList<>();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final int maxAddresses=Sf.toInt(prefs.getString(Constants.PREF_FIRST_LOAD_ATTEMPTS,""+Constants.PREF_FIRST_LOAD_ATTEMPTS_DEFAULT));
            final int stopWhenCountEmpty=Sf.toInt(prefs.getString(Constants.PREF_FIRST_LOAD_RANGE,""+Constants.PREF_FIRST_LOAD_RANGE_DEFAULT));
            while(!stop) {
                Address alreadyAddress=already.get(start);
                Map<String,Boolean> snapshotBalanceAlreadyAddresses=new HashMap();
                boolean foundTransfers=false;
                if(alreadyAddress!=null) {
                    holder.countaddress++;
                    alreadyAddress.setIndex(start);
                    alreadyAddress.setIndexName(start+1);
                    allAddresses.add(alreadyAddress);
                } else {


                    Address newaddress=null;
                    long useBalance=0L;

                    try {
                        GetNewAddressResponse gnr = apiProxy.getNewAddress(String.valueOf(Store.getSeedRaw(context,firstLoadRequest.getSeed())), firstLoadRequest.getSecurity(),
                                start, false, 1, false);
                        String add = gnr.getAddresses().get(0);
                        //Log.e("FIRST-TIME", "CALC ADDRESS: " + add + " -- " + start+"-");
                        newaddress = new Address(add, false, false);
                        holder.countaddress++;
                        newaddress.setIndex(start);
                        newaddress.setIndexName(start + 1);

                        allAddresses.add(newaddress);

                        List<String> tmpadd=new ArrayList<>();
                        tmpadd.add(newaddress.getAddress());

                        GetBalancesResponse gbal = apiProxy.getBalances(100,tmpadd);
                        useBalance=Sf.toLong(gbal.getBalances()[0]);

                        newaddress.setValue(useBalance);
                        newaddress.setLastMilestone(gbal.getMilestoneIndex());
                        newaddress.setAttached(true);

                        Bundle[] bundles = apiProxy.bundlesFromAddresses(new String[]{newaddress.getAddress()}, true);
                        if (bundles != null && bundles.length > 0) {
                            newaddress.setAttached(true);
                            foundTransfers=true;
                            for (int i = 0; i < bundles.length; i++) {

                                String hash = bundles[i].getTransactions().get(0).getHash();
                                if (hasalready.get(hash) == null) {
                                    hasalready.put(hash, true);
                                    allbundles.add(bundles[i]);
                                }
                            }
                        }

                        long oldWallet=wallet.getBalance();
                        long expecting=oldWallet+useBalance;

                        holder.predictaddress=expecting;

                        transfers.clear();
                        Audit.bundlePopulateTransfers(allbundles.toArray(new Bundle[allbundles.size()]),transfers,allAddresses);
                        Audit.setTransfersToAddresses(((GetFirstLoadRequest) request).getSeed(),transfers,allAddresses,wallet,addInTransfers);

                        if(wallet.getBalance()!=expecting && useBalance>0) {
                            if(snapshotBalanceAlreadyAddresses.get(newaddress.getAddress())==null) {
                                long theBalance=expecting-wallet.getBalance();
                                //Log.e("MK-SNAP","snap::: set-bal: "+theBalance+" - wallet:"+wallet.getBalance()+" - expect:"+expecting+" - add-bal"+useBalance+" - snap-add-tran-value:"+newaddress.getValue()+"  - on-address:"+newaddress.getAddress());

                                Transfer addInTran = new Transfer(timestamp, newaddress.getAddress(), "SNAP"+ SeedRandomGenerator.generateNewSeed().substring(0,23)
                                        , true, theBalance, "Snapshot balance confirmed", "SNAP999999999999999999");
                                TransferTransaction tt = new TransferTransaction(newaddress.getAddress(), theBalance);
                                List<TransferTransaction> tmptt = new ArrayList<>();
                                tmptt.add(tt);
                                addInTran.setTransactions(tmptt);
                                addInTransfers.add(addInTran);
                                Audit.setTransfersToAddresses(((GetFirstLoadRequest) request).getSeed(),transfers,allAddresses,wallet,addInTransfers);
                            }
                        }

                        snapshotBalanceAlreadyAddresses.put(newaddress.getAddress(),true);
                        holder.counttransfers = transfers.size();
                    } catch (Exception e) {
                        Log.e("ERR-FLR020","address index: "+start+", ex: "+e.getMessage());
                    }

                }


                int lastComplete=0;
                //boolean hasLastComplete=false;
                for(int i=0; i<allAddresses.size(); i++) {
                    Address tmpadd= allAddresses.get(i);
                    if(tmpadd.getValue()!=0) {
                        lastComplete=i;
                    }
                }
                //Log.e("WALLET","current value: "+wallet.getBalance()+" - "+wallet.getBalancePendingIn()+" -- "+wallet.getBalancePendingOut());
                int countempty=0;

                if(!foundTransfers && lastComplete>0 && allAddresses.size()>=stopWhenCountEmpty) {
                    for (int i = allAddresses.size() - 1; i >= 0 && i > lastComplete; i--) {
                        countempty++;
                    }
                }
                start++;
                //Log.e("COUNT_EMPTY",allAddresses.size()+" addresses, empty: "+countempty);
                if(countempty>=stopWhenCountEmpty || start>maxAddresses) {
                    stop=true;
                }
            }

            for(Transfer tran: transfers) {
                if(!tran.getTransactions().isEmpty()) {
                    if(tran.getTimestamp()<timestamp)
                        timestamp=tran.getTimestamp();
                }
            }
            timestamp=timestamp-600000;
            for(Transfer addIn: addInTransfers) {
                addIn.setTimestamp(--timestamp);
            }

            allAddresses=allAddresses.subList(0,allAddresses.size()-(stopWhenCountEmpty));

            // lastly do a double check if spend from
            String[] checkaddresses = new String[allAddresses.size()];
            int index=0;
            for(Address add: allAddresses) {
                checkaddresses[index++]=add.getAddress();
            }
            boolean[] checkedAddresses=apiProxy.checkWereAddressSpentFrom(checkaddresses);
            index=0;
            for(Address add: allAddresses) {
                add.setUsed(checkedAddresses[index++]);
            }

            Audit.setTransfersToAddresses(((GetFirstLoadRequest) request).getSeed(),transfers,allAddresses,wallet,addInTransfers);

        }

        boolean hasTransfer=false;
        for(int i=allAddresses.size()-1; i>=0; i--) {
            Address address= allAddresses.get(i);
            if(address.getValue()!=0 || address.getPendingValue()!=0) {
                hasTransfer=true;
            } else if(hasTransfer) {
                address.setUsed(true);
            }

        }
        Store.setAccountData(context, ((GetFirstLoadRequest) request).getSeed(),wallet,transfers,allAddresses);
        holder.isFinished=true;
        AppService.auditAddressesWithDelay(context,((GetFirstLoadRequest) request).getSeed());

        return new GetFirstLoadResponse();
    }
}
