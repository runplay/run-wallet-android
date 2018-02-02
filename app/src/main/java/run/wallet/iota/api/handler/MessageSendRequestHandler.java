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

import java.util.Arrays;

import jota.RunIotaAPI;
import jota.error.ArgumentException;
import run.wallet.R;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.MessageSendRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.MessageSendResponse;
import run.wallet.iota.api.responses.SendTransferResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.service.IotaMsg;
import run.wallet.iota.helper.NotificationHelper;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.ui.dialog.KeyReuseDetectedDialog;

public class MessageSendRequestHandler extends IotaMessageRequestHandler {
    public MessageSendRequestHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return MessageSendRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        int notificationId = Utils.createNewID();
        ApiResponse response;
        Log.e("IotaMsg","MessageSendRequestHandler handle: "+request.toString());
        // if we generate a new address the tag == address
        if (((MessageSendRequest) request).getValue().equals("0")
                && ((MessageSendRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG)) {
            NotificationHelper.requestNotification(context,
                    R.drawable.ic_add, context.getString(R.string.notification_attaching_new_address_request_title), notificationId);
        } else {
            NotificationHelper.requestNotification(context,
                    R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_request_title), notificationId);
        }

        try {
            response = new MessageSendResponse(context,((MessageSendRequest) request).getSeed(),apiProxy.sendTransfer(((MessageSendRequest) request).getSeedValue(),
                    ((MessageSendRequest) request).getSecurity(),
                    ((MessageSendRequest) request).getDepth(),
                    ((MessageSendRequest) request).getMinWeightMagnitude(),
                    ((MessageSendRequest) request).prepareTransfer(),
                    //inputs
                    null,
                    //remainder address
                    null,
                    false,
                    true));
        } catch (ArgumentException | IllegalAccessError e) {
            NetworkError error = new NetworkError();
            Log.e("IotaMsg","MessageSendRequestHandler exception: "+e.getMessage());

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                Log.e("IotaMsg","MessageSendRequestHandler exception: 1");
                mNotificationManager.cancel(notificationId);
            }

            if (e instanceof ArgumentException) {
                Log.e("IotaMsg","MessageSendRequestHandler exception: 2: "+((ArgumentException) e).getMessage());
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
                Log.e("IotaMsg","MessageSendRequestHandler exception: 3: "+((IllegalAccessError) e).getMessage());
                error.setErrorType(NetworkErrorType.ACCESS_ERROR);
                if (((MessageSendRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG))
                    NotificationHelper.responseNotification(context, R.drawable.ic_error, context.getString(R.string.notification_address_attach_to_tangle_blocked_title), notificationId);
                else
                    NotificationHelper.responseNotification(context, R.drawable.ic_error, context.getString(R.string.notification_transfer_attach_to_tangle_blocked_title), notificationId);
            } else {
                Log.e("IotaMsg","MessageSendRequestHandler exception: 4");
                if (error.getErrorType() != NetworkErrorType.KEY_REUSE_ERROR) {
                    error.setErrorType(NetworkErrorType.NETWORK_ERROR);
                }
                if (((MessageSendRequest) request).getValue().equals("0") && ((MessageSendRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG)) {
                    NotificationHelper.responseNotification(context, R.drawable.ic_address, context.getString(R.string.notification_attaching_new_address_response_failed_title), notificationId);

                } else {
                    NotificationHelper.responseNotification(context, R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_response_failed_title), notificationId);
                }
            }
            Log.e("IotaMsg","MessageSendRequestHandler exception: "+error.toString());
            response = error;
        }

        if (response instanceof SendTransferResponse && ((MessageSendRequest) request).getValue().equals("0")
                && ((MessageSendRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG)) {
            Log.e("IotaMsg","MessageSendRequestHandler instanceof SendTransferResponse");
            if (Arrays.asList(((SendTransferResponse) response).getSuccessfully()).contains(true))
                NotificationHelper.responseNotification(context, R.drawable.ic_address, context.getString(R.string.notification_attaching_new_address_response_succeeded_title), notificationId);
            else
                NotificationHelper.responseNotification(context, R.drawable.ic_address, context.getString(R.string.notification_attaching_new_address_response_failed_title), notificationId);

        } else if (response instanceof SendTransferResponse) {
            Log.e("IotaMsg","MessageSendRequestHandler instanceof SendTransferResponse2");
            if (Arrays.asList(((SendTransferResponse) response).getSuccessfully()).contains(true))
                NotificationHelper.responseNotification(context, R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_response_succeeded_title), notificationId);
            else
                NotificationHelper.responseNotification(context, R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_response_failed_title), notificationId);
        }

        return response;
    }

}
