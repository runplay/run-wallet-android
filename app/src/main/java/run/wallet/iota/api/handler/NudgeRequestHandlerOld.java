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
import jota.dto.response.GetNewAddressResponse;
import jota.dto.response.RunSendTransferResponse;
import jota.error.ArgumentException;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.GetNewAddressRequest;
import run.wallet.iota.api.requests.NudgeRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.NudgeResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;
import run.wallet.iota.helper.Audit;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.Wallet;

public class NudgeRequestHandlerOld extends IotaRequestHandler {
    public NudgeRequestHandlerOld(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return NudgeRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest inrequest) {
        return doNudge(apiProxy,context,inrequest);
    }

    public static ApiResponse doNudge(RunIotaAPI apiProxy, Context context,ApiRequest inrequest) {
        ApiResponse response;
        //int notificationId = Utils.createNewID();

        NudgeRequest request=(NudgeRequest) inrequest;
        //NotificationHelper.requestNotification(context, R.drawable.send_white, context.getString(R.string.notification_replay_bundle_request_title), notificationId);

        Transfer nudgeMe= request.getTransfer();
        try {

            List<Address> alreadyAddress = Store.getAddresses(context,request.getSeed());

            String useAddress=null;
            Address fullAddress=null;

            List<Address> emptyAttached=Store.getEmptyAttached(alreadyAddress);
            int max=Store.getAutoAttach()+5;  // allows up to 5 pre hidden addresses
            if(emptyAttached.size()<=max) {

                GetNewAddressRequest gnr = new GetNewAddressRequest(request.getSeed());
                gnr.setIndex(alreadyAddress.size());

                final GetNewAddressResponse gna = apiProxy.getNewAddress(String.valueOf(request.getSeed().value), gnr.getSecurity(),
                        alreadyAddress.size(), gnr.isChecksum(), 1, gnr.isReturnAll());

                run.wallet.iota.api.responses.GetNewAddressResponse gnar = new run.wallet.iota.api.responses.GetNewAddressResponse(request.getSeed(), gna);
                Store.addAddress(context, gnr, gnar);

                alreadyAddress = Store.getAddresses(context, request.getSeed());

                useAddress = gnar.getAddresses().get(0);
                fullAddress = Store.isAlreadyAddress(useAddress,alreadyAddress);
            } else {
                fullAddress = emptyAttached.get(0);
                useAddress=fullAddress.getAddress();
            }
            //useAddress=nudgeMe

            //}

            NudgeResponse nresp=null;
            if(useAddress!=null) {
                List<Transfer> transfers = new ArrayList<>();
                List<Transfer> alreadyTransfers=Store.getTransfers(context,request.getSeed());

                Transfer already=Store.isAlreadyTransfer(nudgeMe.getHash(),alreadyTransfers);

                if(already!=null) {

                    RunSendTransferResponse rstr = apiProxy.sendNudgeTransfer(String.valueOf(request.getSeed().value),
                            nudgeMe.getHash(),
                            useAddress,
                            fullAddress.getIndex(),
                            fullAddress.getSecurity(),
                            request.getDepth(),
                            request.getMinWeightMagnitude());
                    nresp = new NudgeResponse(rstr);

                    if (nresp != null && nresp.getSuccessfully()) {
                        already.addNudgeHash(nresp.getHashes().get(0));
                    } else {
                        already.addNudgeHash("Failed nudge");
                    }
                    jota.dto.response.GetNodeInfoResponse nodeInfo=apiProxy.getNodeInfo();
                    if(nodeInfo!=null) {
                        already.setMilestone(nodeInfo.getLatestMilestoneIndex());
                    }


                    Wallet wallet = Store.getWallet(context, request.getSeed());
                    Audit.setTransfersToAddresses(request.getSeed(), transfers, alreadyAddress, wallet, alreadyTransfers);
                    Audit.processNudgeAttempts(context, request.getSeed(), transfers);
                    Store.updateAccountData(context, request.getSeed(), wallet, transfers, alreadyAddress);
                    return nresp;

                }
            }

            //if(nresp==null) {
                NetworkError error = new NetworkError();
                error.setErrorType(NetworkErrorType.INVALID_HASH_ERROR);
                return error;


        } catch (ArgumentException e) {
            Log.e("NUDGE","error: "+e.getMessage());
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.INVALID_HASH_ERROR);
            response = error;
        }

        return response;
    }
}
