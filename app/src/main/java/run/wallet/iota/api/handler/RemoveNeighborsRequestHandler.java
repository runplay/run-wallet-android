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
import run.wallet.iota.api.requests.RemoveNeighborsRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.RemoveNeighborsResponse;

import jota.RunIotaAPI;

public class RemoveNeighborsRequestHandler extends IotaRequestHandler {
    public RemoveNeighborsRequestHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return RemoveNeighborsRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        return new RemoveNeighborsResponse(this.apiProxy.removeNeighbors(((RemoveNeighborsRequest) request).getUris()));
    }
}
