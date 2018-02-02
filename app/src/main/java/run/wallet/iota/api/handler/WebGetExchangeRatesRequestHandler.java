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


import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.WebGetExchangeRatesRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.WebGetExchangeRatesResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.JSONUrlReader;
import run.wallet.iota.model.Store;

import jota.RunIotaAPI;

public class WebGetExchangeRatesRequestHandler extends BasicRequestHandler {
    public WebGetExchangeRatesRequestHandler(Context context) {
        super(context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return WebGetExchangeRatesRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest apiRequest) {

        JSONObject result = JSONUrlReader.readJsonObjectFromUrl(context,Constants.WWW_RUN_IOTA+"/xchange.jsp");
        if(result!=null) {
            Store.updateTickers(context,result);

            JSONArray msgs=result.optJSONArray("sysmsg");
            if(msgs!=null && msgs.length()>0) {
                for(int i=0; i<msgs.length(); i++) {
                    Store.addSystemMessage(context,msgs.optJSONObject(i));
                }
            }

            return new WebGetExchangeRatesResponse(result);

        }
        return new WebGetExchangeRatesResponse(new JSONObject());
        //NetworkError error = new NetworkError();
        //error.setErrorType(NetworkErrorType.NETWORK_ERROR);
        //return error;
    }
}
