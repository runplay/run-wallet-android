package run.wallet.iota.model;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.List;

import run.wallet.common.Cal;
import run.wallet.common.TextFile;
import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;

/**
 * Created by coops on 26/01/18.
 */

public class OutState {
    /*
    only used during dev. app does not currently need to write to file.....
     */
    public static void Go(String appendname, Seeds.Seed seed, JSONObject job) {

        File dir=new File(Environment.getExternalStorageDirectory().toString()+"/runplay/wallet");
        File file=new File(Environment.getExternalStorageDirectory().toString()+"/runplay/wallet/"+appendname+"-"+seed.name+"-"+ System.currentTimeMillis()+".json");
        if(!dir.exists()) {
            dir.mkdirs();

        }
        TextFile.writeToFile(file.getAbsolutePath(),job.toString());
    }
    public static void Go(String appendname, Seeds.Seed seed, Wallet wallet, JSONArray transfers, JSONArray addresses) {
        JSONObject job=new JSONObject();

        job.put("wallet",wallet.toJson());
        job.put("seed",seed.getSystemShortValue());
        job.put("transfers",transfers);
        job.put("addresses",addresses);
        File dir=new File(Environment.getExternalStorageDirectory().toString()+"/runplay/wallet");
        File file=new File(Environment.getExternalStorageDirectory().toString()+"/runplay/wallet/"+appendname+"-"+seed.name+"-"+ System.currentTimeMillis()+".json");
        if(!dir.exists()) {
            dir.mkdirs();

        }
        TextFile.writeToFile(file.getAbsolutePath(),job.toString());
    }
}
