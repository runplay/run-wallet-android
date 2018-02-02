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
import java.util.List;

import jota.dto.response.GetTransferResponse;
import jota.model.Bundle;

import jota.utils.StopWatch;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.GetAccountDataRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetAccountDataResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;
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

            List<String> checkAddressString = new ArrayList<>();
            List<Address> checkAddress = new ArrayList<>();

            if(request.getIfSingleAddressOrNull()!=null) {

                checkAddressString.add(request.getIfSingleAddressOrNull());
            } else {
                for(Address add: alreadyAddress) {
                    if (!add.isUsed()||(add.isUsed()&&(add.getPendingValue()!=0||add.getValue()!=0))) {
                        if(request.isForce() || nodeInfo.getLatestMilestoneIndex()!=add.getLastMilestone()) {
                            checkAddressString.add(add.getAddress());
                            checkAddress.add(add);
                            //Log.e("GET-ACC", "check address: " + add.getAddress());
                        }
                    }
                }
            }
            if(!checkAddress.isEmpty()) {

                try {
                    Bundle[] bundles = apiProxy.bundlesFromAddresses(checkAddressString.toArray(new String[checkAddressString.size()]), true);
                    gtr = GetTransferResponse.create(bundles, stopWatch.getElapsedTimeMili());
                } catch (Exception e) {
                    Log.e("ERR066","ERROR: "+e.getMessage());
                }
                List<Transfer> transfers=new ArrayList<>();

                if(gtr!=null) {
                    if (gtr.getTransfers().length > 0) {

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
                AppService.AuditAddresses(context, request.getSeed());
            }

            return new GetAccountDataResponse();
        }

        return new ApiResponse();
    }
}
