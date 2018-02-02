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

import jota.RunIotaAPI;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.AuditRequest;
import run.wallet.iota.api.responses.ApiResponse;

public class AuditSeedRequestHandler extends IotaRequestHandler {
    public AuditSeedRequestHandler(RunIotaAPI iotaApi, Context context) {
        super(iotaApi, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return AuditRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        ApiResponse response=null;
/*
        try {


            //Store.addFirstTimeLoadData(context, ((GetFirstLoadRequest) request).getSeed(),response);
                //if()

        } catch (ArgumentException e) {
            Log.e("FIRST-TIME","error: "+e.getMessage());
            return new NetworkError();
        }
        */
        return response;
    }
}
