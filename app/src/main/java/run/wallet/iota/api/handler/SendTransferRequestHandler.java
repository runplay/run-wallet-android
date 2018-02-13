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

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import jota.model.Input;
import jota.model.Transfer;
import run.wallet.R;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.SendTransferRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.SendTransferResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.NotificationHelper;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.PayPacket;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.dialog.KeyReuseDetectedDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jota.RunIotaAPI;
import jota.error.ArgumentException;

public class SendTransferRequestHandler extends IotaRequestHandler {
    public SendTransferRequestHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return SendTransferRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        int notificationId = Utils.createNewID();
        ApiResponse response;


        try {
            List<Transfer> transfers = ((SendTransferRequest) request).prepareTransfers();

            List<Input> inputs=null;
            String remainder=null;

            if(((SendTransferRequest) request).getFromAddresses()!=null) {
                inputs=new ArrayList<>();
                for (Address address : ((SendTransferRequest) request).getFromAddresses()) {
                    Input inp = new Input(address.getAddress(), address.getValue(), address.getIndex(), address.getSecurity());

                    inputs.add(inp);
                }
                remainder=((SendTransferRequest) request).getRemainder().getAddress();
            }
            try {
                response = new SendTransferResponse(context, ((SendTransferRequest) request).getSeed()
                        , apiProxy.sendTransfer(String.valueOf(((SendTransferRequest) request).getSeed().value),
                        ((SendTransferRequest) request).getSecurity(),
                        ((SendTransferRequest) request).getDepth(),
                        ((SendTransferRequest) request).getMinWeightMagnitude(),
                        transfers,
                        //inputs
                        inputs,
                        //remainder address
                        remainder,
                        true,
                        false)
                );
            } catch (Exception e) {
                // try again (better to analyse why and respond, also need to provide storage for offline and try agains - todo)
                // ----reason: sometimes when quiet a Node can return null for getTruck or getBranch transactions, i.e the node has no other transactions to approve..
                // todo more here
                // currently this just waits 10 seconds and re-tries
                try {
                    wait(10000);
                } catch (Exception ew){}
                response = new SendTransferResponse(context, ((SendTransferRequest) request).getSeed()
                        , apiProxy.sendTransfer(String.valueOf(((SendTransferRequest) request).getSeed().value),
                        ((SendTransferRequest) request).getSecurity(),
                        ((SendTransferRequest) request).getDepth(),
                        ((SendTransferRequest) request).getMinWeightMagnitude(),
                        transfers,
                        //inputs
                        inputs,
                        //remainder address
                        remainder,
                        true,
                        false)
                );
            }

        } catch (ArgumentException | IllegalAccessError e) {
            //Log.e("SNT","ex: "+e.getMessage());
            NetworkError error = new NetworkError();

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                mNotificationManager.cancel(notificationId);
            }
            if (e instanceof IllegalStateException) {
                //if (e.getMessage().contains("Sending to a used address.") || e.getMessage().contains("Private key reuse detect!")) {
                    final Activity activity = (Activity) context;
                    Bundle bundle = new Bundle();
                    bundle.putString("error", e.getMessage());
                    KeyReuseDetectedDialog dialog = new KeyReuseDetectedDialog();
                    dialog.setArguments(bundle);
                    dialog.show(activity.getFragmentManager(), null);

                    error.setErrorType(NetworkErrorType.KEY_REUSE_ERROR);
                //}
            }
            if (e instanceof ArgumentException) {
                if (e.getMessage().contains("Sending to a used address.") || e.getMessage().contains("Private key reuse detect!")) {
                    final Activity activity = (Activity) context;
                    Bundle bundle = new Bundle();
                    bundle.putString("error", e.getMessage());
                    KeyReuseDetectedDialog dialog = new KeyReuseDetectedDialog();
                    dialog.setArguments(bundle);
                    dialog.show(activity.getFragmentManager(), null);
                    error.setErrorType(NetworkErrorType.KEY_REUSE_ERROR);
                }
            }

            if (e instanceof IllegalAccessError) {
                error.setErrorType(NetworkErrorType.ACCESS_ERROR);
                if (((SendTransferRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG))
                    NotificationHelper.responseNotification(context, R.drawable.ic_error, context.getString(R.string.notification_address_attach_to_tangle_blocked_title), notificationId);
                else
                    NotificationHelper.responseNotification(context, R.drawable.ic_error, context.getString(R.string.notification_transfer_attach_to_tangle_blocked_title), notificationId);
            } else {
                if (error.getErrorType() != NetworkErrorType.KEY_REUSE_ERROR) {
                    error.setErrorType(NetworkErrorType.NETWORK_ERROR);
                }
                if (((SendTransferRequest) request).getValue()==0 && ((SendTransferRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG)) {
                    NotificationHelper.responseNotification(context, R.drawable.ic_address, context.getString(R.string.notification_attaching_new_address_response_failed_title), notificationId);

                } else {
                    NotificationHelper.responseNotification(context, R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_response_failed_title), notificationId);
                }
            }

            response = error;
        }

        if (response instanceof SendTransferResponse && ((SendTransferRequest) request).getValue()==0
                && ((SendTransferRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG)) {

        } else if (response instanceof SendTransferResponse) {
            if (Arrays.asList(((SendTransferResponse) response).getSuccessfully()).contains(true)) {
                if(AppService.isAppStarted()) {
                    NotificationHelper.vibrate(context);
                } else {
                    NotificationHelper.responseNotification(context, R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_response_succeeded_title), notificationId);
                }

            } else {
                NotificationHelper.responseNotification(context, R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_response_failed_title), notificationId);
            }
        }

        return response;
    }
}
