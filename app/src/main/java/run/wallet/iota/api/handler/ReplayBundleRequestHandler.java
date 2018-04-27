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

import jota.dto.response.GetNodeInfoResponse;
import jota.dto.response.RunReplayBundleResponse;
import run.wallet.R;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.ReplayBundleRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.ReplayBundleResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.NotificationHelper;
import run.wallet.iota.helper.Sf;
import run.wallet.iota.helper.Utils;

import java.util.Arrays;

import jota.RunIotaAPI;
import jota.error.ArgumentException;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.service.AppService;

public class ReplayBundleRequestHandler extends IotaRequestHandler {
    public ReplayBundleRequestHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return ReplayBundleRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        ApiResponse response;
        int notificationId = Utils.createNewID();

        //NotificationHelper.requestNotification(context, R.drawable.send_white, context.getString(R.string.notification_replay_bundle_request_title), notificationId);
        if(Store.getNodeInfo()==null) {
            NodeInfoRequestHandler.getNodeInfo(apiProxy,context);

        }
        try {
            RunReplayBundleResponse jresponse = apiProxy.replayBundle(((ReplayBundleRequest) request).getHash(),
                    ((ReplayBundleRequest) request).getDepth(),
                    ((ReplayBundleRequest) request).getMinWeightMagnitude());
            response = new ReplayBundleResponse(context,((ReplayBundleRequest) request).getSeed(),jresponse);



        } catch (ArgumentException e) {
            Log.e("REP","error: "+e.getMessage());
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.INVALID_HASH_ERROR);
            response = error;
        }

        if(!AppService.isAppStarted()) {
            if (response instanceof ReplayBundleResponse && Arrays.asList(((ReplayBundleResponse) response).getSuccessfully()).contains(true)) {
                NotificationHelper.responseNotification(context, R.drawable.ic_replay, context.getString(R.string.notification_replay_bundle_response_succeeded_title), notificationId);
            } else if (response instanceof NetworkError) {
                NotificationHelper.responseNotification(context, R.drawable.ic_replay, context.getString(R.string.notification_replay_bundle_response_failed_title), notificationId);
            }
        } else {
            NotificationHelper.vibrate(context);
        }
        return response;
    }
}
