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
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

import cfb.pearldiver.PearlDiverLocalPoW;

import jota.RunIotaAPI;
import jota.error.ArgumentException;
import run.wallet.iota.api.requests.AddressSecurityChangeRequest;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.GetNewAddressRequest;
import run.wallet.iota.api.responses.AddressSecurityChangeResponse;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetNewAddressResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;

public class AddressSecurityChangeRequestHandler extends IotaRequestHandler {
    public AddressSecurityChangeRequestHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return AddressSecurityChangeRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        ApiResponse response;
        AddressSecurityChangeRequest gnr=((AddressSecurityChangeRequest) request);

        try {
            jota.dto.response.GetNewAddressResponse resp=apiProxy.getNewAddress(String.valueOf(Store.getSeedRaw(context,gnr.getSeed())),
                    gnr.getSecurity(),
                    gnr.getAddress().getIndex(),
                    false,
                    1,
                    true);
            response = new GetNewAddressResponse(gnr.getSeed(),resp);

            Store.updateAddressFromSecurity(context,gnr,(GetNewAddressResponse)response);

            return new AddressSecurityChangeResponse();


        } catch (ArgumentException e) {
            response = new NetworkError();
        }
        return response;
    }
}
