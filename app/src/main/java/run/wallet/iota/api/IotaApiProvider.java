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

import run.wallet.iota.api.handler.AddNeighborsRequestHandler;
import run.wallet.iota.api.handler.AddressSecurityChangeRequestHandler;
import run.wallet.iota.api.handler.AuditAddressesRequestHandler;
import run.wallet.iota.api.handler.AuditSeedRequestHandler;
import run.wallet.iota.api.handler.AutoNudgeHandler;
import run.wallet.iota.api.handler.GetFirstLoadRequestHandler;
import run.wallet.iota.api.handler.NudgeRequestHandler;
import run.wallet.iota.api.handler.WebGetExchangeRatesHistoryRequestHandler;
import run.wallet.iota.api.handler.WebGetExchangeRatesRequestHandler;
import run.wallet.iota.api.handler.FindTransactionsRequestHandler;
import run.wallet.iota.api.handler.GetAccountDataRequestHandler;
import run.wallet.iota.api.handler.GetBalanceAndFormatRequestHandler;
import run.wallet.iota.api.handler.GetBundleRequestHandler;
import run.wallet.iota.api.handler.GetNeighborsRequestHandler;
import run.wallet.iota.api.handler.GetNewAddressRequestHandler;
import run.wallet.iota.api.handler.NodeInfoRequestHandler;
import run.wallet.iota.api.handler.RemoveNeighborsRequestHandler;
import run.wallet.iota.api.handler.ReplayBundleRequestHandler;
import run.wallet.iota.api.handler.RequestHandler;
import run.wallet.iota.api.handler.SendTransferRequestHandler;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.AuditAddressesRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.api.responses.error.NetworkErrorType;
import run.wallet.iota.helper.Constants;

import java.util.HashMap;
import java.util.Map;

import cfb.pearldiver.PearlDiverLocalPoW;
import jota.RunIotaAPI;

public class IotaApiProvider implements ApiProvider {
    private final RunIotaAPI iotaApi;
    private final Context context;
    private Map<Class<? extends ApiRequest>, RequestHandler> requestHandlerMap;

    public IotaApiProvider(String protocol, String host, int port, Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        if (prefs.getBoolean(Constants.PREFERENCES_LOCAL_POW, true))
            this.iotaApi = new RunIotaAPI.Builder().localPoW(new PearlDiverLocalPoW()).protocol(protocol).host(host).port(((Integer) port).toString()).build();
        else
            this.iotaApi = new RunIotaAPI.Builder().protocol(protocol).host(host).port(((Integer) port).toString()).build();

        this.context = context;
        loadRequestMap();
    }


    private void loadRequestMap() {
        Map<Class<? extends ApiRequest>, RequestHandler> requestHandlerMap = new HashMap<>();

        AddressSecurityChangeRequestHandler addressSecurity = new AddressSecurityChangeRequestHandler(iotaApi,context);
        AddNeighborsRequestHandler addNeighborsAction = new AddNeighborsRequestHandler(iotaApi, context);
        AuditAddressesRequestHandler auditAddresses = new AuditAddressesRequestHandler(iotaApi, context);
        AutoNudgeHandler autoNudge = new AutoNudgeHandler(iotaApi, context);

        FindTransactionsRequestHandler findTransactionsAction = new FindTransactionsRequestHandler(iotaApi, context);
        GetBundleRequestHandler getBundleAction = new GetBundleRequestHandler(iotaApi, context);
        GetNeighborsRequestHandler getNeighborsAction = new GetNeighborsRequestHandler(iotaApi, context);
        GetNewAddressRequestHandler getNewAddressAction = new GetNewAddressRequestHandler(iotaApi, context);
        GetAccountDataRequestHandler getAccountDataAction = new GetAccountDataRequestHandler(iotaApi, context);
        RemoveNeighborsRequestHandler removeNeighborsAction = new RemoveNeighborsRequestHandler(iotaApi, context);
        ReplayBundleRequestHandler replayBundleAction = new ReplayBundleRequestHandler(iotaApi, context);
        SendTransferRequestHandler sendTransferAction = new SendTransferRequestHandler(iotaApi, context);
        NodeInfoRequestHandler nodeInfoAction = new NodeInfoRequestHandler(iotaApi, context);
        GetFirstLoadRequestHandler firstLoad = new GetFirstLoadRequestHandler(iotaApi,context);
        AuditSeedRequestHandler auditRequest = new AuditSeedRequestHandler(iotaApi,context);
        NudgeRequestHandler nudgeRequest = new NudgeRequestHandler(iotaApi,context);
        //MessageSendRequestHandler sendMessageAction = new MessageSendRequestHandler(iotaApi, context);
        GetBalanceAndFormatRequestHandler getBalanceAndFormatRequest = new GetBalanceAndFormatRequestHandler(iotaApi,context);

        requestHandlerMap.put(autoNudge.getType(),autoNudge);
        requestHandlerMap.put(nudgeRequest.getType(),nudgeRequest);
        requestHandlerMap.put(addressSecurity.getType(),addressSecurity);
        requestHandlerMap.put(auditAddresses.getType(), auditAddresses);
        requestHandlerMap.put(auditRequest.getType(), auditRequest);
        requestHandlerMap.put(firstLoad.getType(), firstLoad);
        requestHandlerMap.put(addNeighborsAction.getType(), addNeighborsAction);

        requestHandlerMap.put(findTransactionsAction.getType(), findTransactionsAction);
        requestHandlerMap.put(getBundleAction.getType(), getBundleAction);
        requestHandlerMap.put(getNeighborsAction.getType(), getNeighborsAction);
        requestHandlerMap.put(getNewAddressAction.getType(), getNewAddressAction);
        requestHandlerMap.put(getAccountDataAction.getType(), getAccountDataAction);
        requestHandlerMap.put(removeNeighborsAction.getType(), removeNeighborsAction);
        requestHandlerMap.put(replayBundleAction.getType(), replayBundleAction);
        requestHandlerMap.put(sendTransferAction.getType(), sendTransferAction);
        requestHandlerMap.put(nodeInfoAction.getType(), nodeInfoAction);
        //requestHandlerMap.put(sendMessageAction.getType(), sendMessageAction);
        requestHandlerMap.put(getBalanceAndFormatRequest.getType(), getBalanceAndFormatRequest);

        this.requestHandlerMap = requestHandlerMap;
    }

    @Override
    public ApiResponse processRequest(ApiRequest apiRequest) {
        ApiResponse response = null;

        try {
            //Log.e("iotaApiProv",apiRequest.getClass()+" - "+this.requestHandlerMap.containsKey(apiRequest.getClass()));
            if (this.requestHandlerMap.containsKey(apiRequest.getClass())) {
                RequestHandler requestHandler = this.requestHandlerMap.get(apiRequest.getClass());
                response = requestHandler.handle(apiRequest);
            }
        } catch (IllegalAccessError e) {
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.ACCESS_ERROR);
            error.setMessage(e.getMessage());
            response = error;
            //if(error.getErrorType()==401 && e.getMessage().contains("getNeighbours"))
            Log.e("ERR-API1",""+e.getMessage());
        } catch (Exception e) {
            Log.e("ERR-API2",e.toString()+" -- "+e.getMessage());
            response = new NetworkError();
        }
        return response == null ? new NetworkError() : response;
    }
}
