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
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jota.RunIotaAPI;
import jota.dto.response.GetBalancesResponse;
import jota.dto.response.GetTransferResponse;
import jota.model.Bundle;
import jota.utils.IotaToText;
import jota.utils.StopWatch;
import run.wallet.R;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.GetAccountDataRequest;
import run.wallet.iota.api.requests.RefreshUsedAddressesRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetAccountDataResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.helper.Audit;
import run.wallet.iota.helper.Sf;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.Wallet;
import run.wallet.iota.ui.fragment.WalletAddressesFragment;
import run.wallet.iota.ui.fragment.WalletTransfersFragment;

public class RefreshUsedAddressesHandler extends IotaRequestHandler {
    public RefreshUsedAddressesHandler(RunIotaAPI iotaApi, Context context) {
        super(iotaApi, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return RefreshUsedAddressesRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest inrequest) {


        RefreshUsedAddressesRequest request = (RefreshUsedAddressesRequest) inrequest;

        NodeInfoRequestHandler.getNodeInfo(apiProxy,context);

        if(Store.isNodeSynced()) {
            if(request.getSeed()!=null) {
                //Log.e("CHK-USED","SINGLE");
                checkUsedAddressForSeed(request.getSeed(), true);
            } else {
                Store.updateLastUsedCheck(context);
                for(int i=0; i<Store.getSeedList().size(); i++) {
                    //Log.e("CHK-USED","MULTI: "+i);
                    checkUsedAddressForSeed(Store.getSeedList().get(i),false);

                }
            }
        }
        return new ApiResponse();
    }

    private GetAccountDataResponse checkUsedAddressForSeed(Seeds.Seed seed, boolean report) {

        Wallet wallet = Store.getWallet(context,seed);

        if(wallet!=null) {
            List<Address> alreadyAddress = Store.getAddresses(context, seed);
            GetTransferResponse gtr=null;
            StopWatch stopWatch = new StopWatch();
            List<Address> checkAddress = new ArrayList<>();
            for(Address tmp: alreadyAddress) {
                if(tmp.isUsed() && tmp.getValue()==0) {
                    checkAddress.add(tmp);
                }
            }
            if(!checkAddress.isEmpty()) {

                try {
                    List<String> checkAddressString = new ArrayList<>();
                    List<String> checkAddressBal = new ArrayList<>();
                    for(Address addr: checkAddress) {
                        checkAddressBal.add(addr.getAddress());
                    }
                    GetBalancesResponse gbal = apiProxy.getBalances(100,checkAddressBal);
                    for(int i=0; i<gbal.getBalances().length; i++) {
                        Address addr=checkAddress.get(i);
                        long gotBalance=Sf.toLong(gbal.getBalances()[i]);

                        if(gotBalance!=0 || addr.getPendingValue()!=0) {
                            addr.setValue(gotBalance);
                            checkAddressString.add(addr.getAddress());
                        }
                        if(report) {
                            if(gotBalance>0) {
                                Store.setUsedAddressCheckResult(context.getString(R.string.usedAddressNoOk) + " " + IotaToText.convertRawIotaAmountToDisplayText(gotBalance, true));
                            } else {
                                Store.setUsedAddressCheckResult(context.getString(R.string.usedAddressOk));
                            }
                        }
                    }

                    if(!checkAddressString.isEmpty()) {

                        Bundle[] bundles = apiProxy.bundlesFromAddresses(checkAddressString.toArray(new String[checkAddressString.size()]), true);
                        gtr = GetTransferResponse.create(bundles, stopWatch.getElapsedTimeMili());
                    }
                    List<Transfer> transfers=new ArrayList<>();

                    if (gtr!=null && gtr.getTransfers().length > 0) {

                        List<Transfer> alreadyTransfer=Store.getTransfers(context, seed);
                        Audit.bundlePopulateTransfers(gtr.getTransfers(),transfers,alreadyAddress);

                        Audit.setTransfersToAddresses(seed, transfers, alreadyAddress, wallet, alreadyTransfer);


                        Audit.processNudgeAttempts(context,seed,transfers);
                        Store.updateAccountData(context, seed, wallet, transfers, alreadyAddress);

                    }


                } catch (Exception e) {
                    Log.e("ERR066","ERROR: "+e.getMessage());
                }


            }


        }
        return new GetAccountDataResponse();
    }
}
