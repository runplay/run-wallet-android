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

import java.util.List;

import jota.RunIotaAPI;
import jota.error.ArgumentException;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.GetNewAddressRequest;
import run.wallet.iota.api.requests.MessageNewAddressRequest;
import run.wallet.iota.api.requests.MessageSendRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetNewAddressResponse;
import run.wallet.iota.api.responses.MessageNewAddressResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.MsgStore;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.IotaMsg;

public class MessageNewAddressRequestHandler extends IotaMessageRequestHandler {
    public MessageNewAddressRequestHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return MessageNewAddressRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        ApiResponse response;
        List<Address> addressList = MsgStore.getAddresses();
        try {

            jota.dto.response.GetNewAddressResponse resp=apiProxy.getNewAddress(String.valueOf(Store.getSeedRaw(context,((MessageNewAddressRequest) request).getSeed())),
                    ((MessageNewAddressRequest) request).getSecurity(),
                    addressList.size(),
                    ((MessageNewAddressRequest) request).isChecksum(),
                    1,
                    ((MessageNewAddressRequest) request).isReturnAll());
            response = new MessageNewAddressResponse(resp);
            //Store.addAddress(context,((MessageNewAddressRequest) request).getSeed(),(MessageNewAddressResponse)response);
        } catch (ArgumentException e) {
            response = new NetworkError();
        }
        return response;
    }
}
