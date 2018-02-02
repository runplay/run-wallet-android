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
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.Date;

import run.wallet.iota.IOTA;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.model.Nodes;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;

class MessageRequestTask extends AsyncTask<ApiRequest, String, ApiResponse> {

    //private static ApiProvider iotaApi;
    private WeakReference<Context> context;
    private EventBus bus;
    private Date start;
    private String tag = "";

    private long taskId;

    public MessageRequestTask(Context context) {
        this.context = new WeakReference<>(context);
        this.bus = EventBus.getDefault();
    }

    public void setTaskId(long id) {
        this.taskId=id;
    }
    @Override
    protected ApiResponse doInBackground(ApiRequest... params) {

        Context context = this.context.get();

        if (context != null) {
            //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            //String protocol = prefs.getString(Constants.PREFERENCE_NODE_PROTOCOL, Constants.PREFERENCE_NODE_DEFAULT_PROTOCOL);
            //String host = prefs.getString(Constants.PREFERENCE_NODE_IP, Constants.PREFERENCE_NODE_DEFAULT_IP);
            //int port = Integer.parseInt(prefs.getString(Constants.PREFERENCE_NODE_PORT, Constants.PREFERENCE_NODE_DEFAULT_PORT));
            Nodes.Node node = Store.getNode();
            //Log.e("MESSAGE REQUEST NODE:",node.protocol+":"+node.ip+":"+node.port);
            String protocol = node.protocol;
            String host = node.ip;
            int port = node.port;
            //if (IOTA.DEBUG) {
            //    Log.e("ApiRequest", params[0].toString());
            //start = new Date();
            //    Log.e("started at", start.getTime() + "");
            //}

            ApiRequest apiRequest = params[0];
            tag = apiRequest.getClass().getName();


            ApiProvider apiProvider = new IotaMessageApiProvider(protocol, host, port, context);

            ApiResponse response = apiProvider.processRequest(apiRequest);
            return response;

        }

        TaskManager.removeTask(tag);
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(ApiResponse result) {
        AppService.markTaskFinished(taskId);
        TaskManager.removeTask(tag);
        if (this.isCancelled()) return;
        //if (IOTA.DEBUG) {
            //if (result != null)
            //    Log.e("ApiResponse", new Gson().toJson(result));
            //if (start != null) {
            //    Log.e("duration", (new Date().getTime()) - start.getTime() + "");
            //}
        //}

        if (result != null) {
            bus.post(result);
        } else {
            bus = null;
        }


    }
}