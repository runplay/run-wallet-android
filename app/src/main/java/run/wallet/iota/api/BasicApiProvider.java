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

import run.wallet.iota.api.handler.RequestHandler;
import run.wallet.iota.api.handler.WebGetExchangeRatesHistoryRequestHandler;
import run.wallet.iota.api.handler.WebGetExchangeRatesRequestHandler;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;

public class BasicApiProvider implements ApiProvider {
    private final Context context;
    private static Map<Class<? extends ApiRequest>, RequestHandler> useRequestHandlerMap;

    public BasicApiProvider(Context context) {
        this.context = context;
        loadRequestMap();
    }

    private void loadRequestMap() {
        if(useRequestHandlerMap==null) {
            Map<Class<? extends ApiRequest>, RequestHandler> requestHandlerMap = new HashMap<>();

            WebGetExchangeRatesRequestHandler webExchangeAction = new WebGetExchangeRatesRequestHandler(context);
            WebGetExchangeRatesHistoryRequestHandler webExchangeHistoryAction = new WebGetExchangeRatesHistoryRequestHandler(context);

            requestHandlerMap.put(webExchangeAction.getType(), webExchangeAction);
            requestHandlerMap.put(webExchangeHistoryAction.getType(), webExchangeHistoryAction);
            useRequestHandlerMap = requestHandlerMap;
        }

    }

    @Override
    public ApiResponse processRequest(ApiRequest apiRequest) {
        ApiResponse response = null;

        try {
            if (useRequestHandlerMap.containsKey(apiRequest.getClass())) {
                RequestHandler requestHandler = useRequestHandlerMap.get(apiRequest.getClass());
                response = requestHandler.handle(apiRequest);
            }
        } catch (IllegalAccessError e) {
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.ACCESS_ERROR);
            response = error;
            Log.e("ERR-B-API1",""+e.getMessage());
        } catch (Exception e) {
            Log.e("ERR-B-API2",e.toString()+" -- "+e.getMessage());
            response = new NetworkError();
        }
        return response == null ? new NetworkError() : response;
    }
}
