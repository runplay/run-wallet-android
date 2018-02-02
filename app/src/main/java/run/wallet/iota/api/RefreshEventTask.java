package run.wallet.iota.api;

import android.content.Context;
import android.os.AsyncTask;

import org.greenrobot.eventbus.EventBus;

import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.RefreshEventResponse;


/**
 * Created by coops on 15/01/18.
 */

public class RefreshEventTask  extends AsyncTask<Boolean, Boolean, Boolean> {
    private EventBus bus;

    public RefreshEventTask() {
        bus = EventBus.getDefault();
    }

    @Override
    protected Boolean doInBackground(Boolean... params) {
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        bus.post(new RefreshEventResponse());
    }
}