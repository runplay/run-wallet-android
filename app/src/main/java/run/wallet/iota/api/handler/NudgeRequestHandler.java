package run.wallet.iota.api.handler;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jota.RunIotaAPI;
import jota.dto.response.GetNewAddressResponse;
import jota.dto.response.RunSendTransferResponse;
import jota.error.ArgumentException;
import jota.utils.SeedRandomGenerator;
import run.wallet.R;
import run.wallet.common.Sf;
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

public class NudgeRequestHandler extends IotaRequestHandler {
    public NudgeRequestHandler(RunIotaAPI apiProxy, Context context) {
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
        NudgeRequest request=(NudgeRequest) inrequest;

        Transfer nudgeMe= request.getTransfer();

        try {

            List<Address> alreadyAddress = Store.getAddresses(context,request.getSeed());
            //String useAddress="RUN9IOTA9WALLET9NUDGE9PROMOTE9TRANSFER9ADDRESSRUN9IOTA9WALLET9NUDGE9PROMOTE9TRANS";

            String random = SeedRandomGenerator.generateNewSeed();
            String useAddress="RUN9IOTA9WALLET9NUDGE9PROMOTE9TRANSFER99"+ Sf.restrictLength(random,41);
            NudgeResponse nresp=null;
            if(useAddress!=null) {
                List<Transfer> transfers = new ArrayList<>();
                List<Transfer> alreadyTransfers=Store.getTransfers(context,request.getSeed());

                Transfer already=Store.isAlreadyTransfer(nudgeMe.getHash(),alreadyTransfers);
                if(already!=null) {
                    RunSendTransferResponse rstr = apiProxy.sendNudgeTransfer(String.valueOf(Store.getSeedRaw(context,request.getSeed())),
                            nudgeMe.getHash(),
                            useAddress,
                            1,
                            2,
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
                        Wallet wallet = Store.getWallet(context, request.getSeed());
                        Audit.setTransfersToAddresses(request.getSeed(), transfers, alreadyAddress, wallet, alreadyTransfers);
                        if(!request.isIsquicknudge()) {
                            Audit.processNudgeAttempts(context, request.getSeed(), transfers);
                        }
                        Store.updateAccountData(context, request.getSeed(), wallet, transfers, alreadyAddress);
                    }

                    if(!AppService.isAppStarted()) {
                        NotificationHelper.clearAll(context);
                        NotificationHelper.responseNotification(context, R.drawable.nudge_orange, context.getString(R.string.notification_nudge_succeeded_title), notificationId);

                    } else {
                        //NotificationHelper.vibrate(context);
                    }
                    return nresp;

                }
            }

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
