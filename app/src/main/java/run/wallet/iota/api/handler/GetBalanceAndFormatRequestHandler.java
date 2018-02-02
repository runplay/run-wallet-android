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
import java.util.List;

import jota.RunIotaAPI;

import jota.error.ArgumentException;
import jota.model.Input;
import jota.model.Inputs;
import jota.utils.StopWatch;
import run.wallet.R;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.GetBalanceAndFormatRequest;
import run.wallet.iota.api.requests.GetBalanceAndFormatRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetBalanceAndFormatResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.NotificationHelper;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.ui.dialog.KeyReuseDetectedDialog;

public class GetBalanceAndFormatRequestHandler extends IotaRequestHandler {
    public GetBalanceAndFormatRequestHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return GetBalanceAndFormatRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        int notificationId = Utils.createNewID();
        ApiResponse response;

        try {
            StopWatch stopWatch=new StopWatch();
            //stopWatch.
            Log.e("BALF","Getting balances and format");
            response = new GetBalanceAndFormatResponse(apiProxy.getBalanceAndFormat(((GetBalanceAndFormatRequest) request).addresses,0L,0,stopWatch,0
                    ));
            List<Input> inputs=((GetBalanceAndFormatResponse)response).getInputs();
            Log.e("BALF","total balance: "+((GetBalanceAndFormatResponse)response).getBalance());
            for(Input inp: inputs) {
                Log.e("BALF","input: "+inp.getBalance()+" - "+inp.getAddress());
            }
        } catch (ArgumentException e) {
            NetworkError error = new NetworkError();

            if (e instanceof ArgumentException) {

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

            if (error.getErrorType() != NetworkErrorType.KEY_REUSE_ERROR) {
                error.setErrorType(NetworkErrorType.NETWORK_ERROR);
            }

            response = error;
        }


        return response;
    }
}
