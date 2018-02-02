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

package run.wallet.iota.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cfb.pearldiver.PearlDiverLocalPoW;
import jota.RunIotaAPI;
import run.wallet.iota.api.handler.MessageFirstLoadRequestHandler;
import run.wallet.iota.api.handler.MessageNewAddressRequestHandler;
import run.wallet.iota.api.handler.RequestHandler;
import run.wallet.iota.api.handler.MessageSendRequestHandler;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.MessageFirstLoadRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.service.IotaMsg;

public class IotaMessageApiProvider implements ApiProvider {
    private final RunIotaAPI iotaApi;
    private final Context context;
    private Map<Class<? extends ApiRequest>, RequestHandler> requestHandlerMap;

    public IotaMessageApiProvider(String protocol, String host, int port, Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());


        this.iotaApi = new RunIotaAPI.Builder().localPoW(new PearlDiverLocalPoW()).protocol(protocol).host(host).port(((Integer) port).toString()).build();


        this.context = context;
        loadRequestMap();
    }

    private void loadRequestMap() {
        Map<Class<? extends ApiRequest>, RequestHandler> requestHandlerMap = new HashMap<>();

        MessageSendRequestHandler sendMessageAction = new MessageSendRequestHandler(iotaApi, context);
        MessageNewAddressRequestHandler newAddressAction = new MessageNewAddressRequestHandler(iotaApi, context);
        MessageFirstLoadRequestHandler firstLoad = new MessageFirstLoadRequestHandler(iotaApi,context);


        requestHandlerMap.put(sendMessageAction.getType(), sendMessageAction);


        this.requestHandlerMap = requestHandlerMap;
    }

    @Override
    public ApiResponse processRequest(ApiRequest apiRequest) {
        ApiResponse response = null;

        try {
            //Log.e("IotaMessageApiProvider", "");
            if (this.requestHandlerMap.containsKey(apiRequest.getClass())) {
                //Log.e("IotaMessageApiProvider", "requesting");
                RequestHandler requestHandler = this.requestHandlerMap.get(apiRequest.getClass());
                response = requestHandler.handle(apiRequest);
            }
        } catch (IllegalAccessError e) {
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.ACCESS_ERROR);
            response = error;
            //Log.e("IotaMessageApi ERR",""+e.getMessage());
        } catch (Exception e) {
            Log.e("ERR-API","ex: "+e.getMessage());
            response = new NetworkError();
        }
        return response == null ? new NetworkError() : response;
    }
}
