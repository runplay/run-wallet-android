package run.wallet.iota.api.handler;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jota.RunIotaAPI;
import jota.dto.response.GetNewAddressResponse;
import jota.dto.response.RunSendTransferResponse;
import jota.error.ArgumentException;
import run.wallet.R;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.GetNewAddressRequest;
import run.wallet.iota.api.requests.NudgeRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.NudgeResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;
import run.wallet.iota.helper.Audit;
import run.wallet.iota.helper.NotificationHelper;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.Wallet;
import run.wallet.iota.service.AppService;

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
        int notificationId = Utils.createNewID();
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

                final GetNewAddressResponse gna = apiProxy.getNewAddress(String.valueOf(Store.getSeedRaw(context,request.getSeed())), gnr.getSecurity(),
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

                    RunSendTransferResponse rstr = apiProxy.sendNudgeTransfer(String.valueOf(Store.getSeedRaw(context,request.getSeed())),
                            nudgeMe.getHash(),
                            useAddress,
                            fullAddress.getIndex(),
                            fullAddress.getSecurity(),
                            request.getDepth(),
                            request.getMinWeightMagnitude());
                    nresp = new NudgeResponse(rstr);

                    String gotHash=null;
                    if (nresp != null && nresp.getSuccessfully()) {
                        gotHash=nresp.getHashes().get(0);
                        already.addNudgeHash(gotHash);
                    } else {
                        already.addNudgeHash("Failed nudge");
                    }
                    jota.dto.response.GetNodeInfoResponse nodeInfo=apiProxy.getNodeInfo();
                    if(nodeInfo!=null) {
                        already.setMilestone(nodeInfo.getLatestMilestoneIndex());
                    }

                    if(gotHash!=null) {
                        Transfer transfer=new Transfer(useAddress, 0, "RUN9NUDGE9HASH9"+nudgeMe.getHash()+"9END", RunIotaAPI.NUDGE_TAG);
                        transfer.setHash(gotHash);
                        transfer.setTimestamp(System.currentTimeMillis());
                        if(nodeInfo!=null) {
                            transfer.setMilestoneCreated(nodeInfo.getLatestMilestoneIndex());
                        }

                        transfers.add(transfer);
                        Wallet wallet = Store.getWallet(context, request.getSeed());
                        Audit.setTransfersToAddresses(request.getSeed(), transfers, alreadyAddress, wallet, alreadyTransfers);
                        Audit.processNudgeAttempts(context, request.getSeed(), transfers);
                        Store.updateAccountData(context, request.getSeed(), wallet, transfers, alreadyAddress);
                    }

                    if(!AppService.isAppStarted()) {
                        NotificationHelper.responseNotification(context, R.drawable.nudge_orange, context.getString(R.string.notification_nudge_succeeded_title), notificationId);

                    } else {
                        NotificationHelper.vibrate(context);
                    }
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
