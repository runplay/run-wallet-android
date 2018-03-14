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
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import jota.dto.response.GetBalancesResponse;
import jota.dto.response.GetTransferResponse;
import jota.model.Bundle;

import jota.utils.StopWatch;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.GetAccountDataRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetAccountDataResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.api.responses.RefreshEventResponse;
import run.wallet.iota.api.responses.error.NetworkError;

import jota.RunIotaAPI;
import jota.error.ArgumentException;
import run.wallet.iota.helper.Audit;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Sf;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.Wallet;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.fragment.WalletAddressesFragment;
import run.wallet.iota.ui.fragment.WalletTransfersFragment;

public class GetAccountDataRequestHandler extends IotaRequestHandler {
    public GetAccountDataRequestHandler(RunIotaAPI iotaApi, Context context) {
        super(iotaApi, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return GetAccountDataRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest inrequest) {


        GetAccountDataRequest request = (GetAccountDataRequest) inrequest;
        GetTransferResponse gtr=null;
        StopWatch stopWatch = new StopWatch();
        NodeInfoResponse nodeInfo=Store.getNodeInfo();

        Wallet wallet = Store.getWallet(context,request.getSeed());
        if(wallet!=null && nodeInfo!=null) {
            List<Address> alreadyAddress = Store.getAddresses(context, request.getSeed());
            List<Address> usingAddress = Store.getDisplayAddresses(alreadyAddress);
            List<Address> checkAddress = new ArrayList<>();
            if(request.getIfSingleAddressOrNull()!=null) {
                Address address= Store.isAlreadyAddress(request.getIfSingleAddressOrNull(),alreadyAddress);
                if(address!=null) {
                    checkAddress.add(address);
                }
            } else {
                for(Address add: usingAddress) {
                    if (!add.isUsed()||(add.isUsed()&&(add.getPendingValue()!=0||add.getValue()!=0))) {
                        if(request.isForce() || nodeInfo.getLatestMilestoneIndex()!=add.getLastMilestone()) {
                            checkAddress.add(add);
                        }
                    }
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

                        if(addr.getValue()!=gotBalance || addr.getPendingValue()!=0) {
                            addr.setValue(gotBalance);
                            checkAddressString.add(addr.getAddress());
                        }
                    }

                    if(!checkAddressString.isEmpty()) {

                        Bundle[] bundles = apiProxy.bundlesFromAddresses(checkAddressString.toArray(new String[checkAddressString.size()]), true);
                        gtr = GetTransferResponse.create(bundles, stopWatch.getElapsedTimeMili());
                    }
                    List<Transfer> transfers=new ArrayList<>();

                    if (gtr!=null && gtr.getTransfers().length > 0) {

                        List<Transfer> alreadyTransfer=Store.getTransfers(context, request.getSeed());
                        Audit.bundlePopulateTransfers(gtr.getTransfers(),transfers,alreadyAddress);

                        if (request.getIfSingleAddressOrNull() != null) {
                            Audit.setTransfersToAddresses(request.getSeed(), transfers, alreadyAddress, wallet, alreadyTransfer);
                        } else {
                            Audit.setTransfersToAddresses(request.getSeed(), transfers, alreadyAddress, wallet, alreadyTransfer);
                        }

                        Audit.processNudgeAttempts(context,request.getSeed(),transfers);
                        Store.updateAccountData(context, request.getSeed(), wallet, transfers, alreadyAddress);

                    }

                    if(request.getIfSingleAddressOrNull() == null ) {
                        WalletAddressesFragment.setShouldRefresh(true);
                        WalletTransfersFragment.setShouldRefresh(true);


                        List<String> checkOthersString = new ArrayList<>();
                        for(Address address: checkAddress) {
                            if(!checkAddressString.contains(address.getAddress()))
                                checkOthersString.add(address.getAddress());
                        }
                        if(!checkOthersString.isEmpty()) {

                            Bundle[] bundles = apiProxy.bundlesFromAddresses(checkOthersString.toArray(new String[checkOthersString.size()]), true);
                            gtr = GetTransferResponse.create(bundles, stopWatch.getElapsedTimeMili());
                        }

                        if (gtr!=null && gtr.getTransfers().length > 0) {
                            transfers=new ArrayList<>();
                            List<Transfer> alreadyTransfer=Store.getTransfers(context, request.getSeed());
                            Audit.bundlePopulateTransfers(gtr.getTransfers(),transfers,alreadyAddress);

                            if (request.getIfSingleAddressOrNull() != null) {
                                Audit.setTransfersToAddresses(request.getSeed(), transfers, alreadyAddress, wallet, alreadyTransfer);
                            } else {
                                Audit.setTransfersToAddresses(request.getSeed(), transfers, alreadyAddress, wallet, alreadyTransfer);
                            }

                            Audit.processNudgeAttempts(context,request.getSeed(),transfers);
                            Store.updateAccountData(context, request.getSeed(), wallet, transfers, alreadyAddress);
                        }
                    }


                } catch (Exception e) {
                    Log.e("ERR066","ERROR: "+e.getMessage());
                }


            }

            return new GetAccountDataResponse();
        }

        return new ApiResponse();
    }
}
