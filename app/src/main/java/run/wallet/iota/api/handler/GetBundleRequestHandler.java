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

import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.GetBundleRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetBundleResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;

import jota.RunIotaAPI;
import jota.error.ArgumentException;

public class GetBundleRequestHandler extends IotaRequestHandler {
    public GetBundleRequestHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return GetBundleRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        ApiResponse response;
        try {
            response =  new GetBundleResponse(apiProxy.getBundle(((GetBundleRequest) request).getTransaction()));
        } catch (ArgumentException e) {
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.INVALID_HASH_ERROR);
            response = error;
        }
        return response;
    }
}
