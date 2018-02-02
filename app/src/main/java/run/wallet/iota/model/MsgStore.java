package run.wallet.iota.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import jota.utils.SeedRandomGenerator;
import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;
import run.wallet.iota.api.responses.MessageNewAddressResponse;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.SpManager;

/**
 * Created by coops on 18/12/17.
 */

public class MsgStore {

    private static MsgStore msgStore =new MsgStore();

    private Seeds.Seed currentSeed;
    private String name;
    private Wallet currentWallet;

    private List<Address> addresses = new ArrayList();
    private List<Transaction> transactions = new ArrayList();
    private List<Transfer> transfers = new ArrayList();



    private static final String SP_STORE="msgseed";

    private static final String PREF_MSG_SEED="seedval";
    private static final String PREF_MSG_NAME="msgname";

    private static final String PREF_ADDRESSES="add";
    private static final String PREF_TRANSFERS="traf";
    private static final String PREF_WALLET= "run/wallet/monero/wallet";


    private MsgStore() {}



    public static void init(Context context) {
        loadSeed(context);
        reinit(context);
    }

    public static final String getName() {
        return msgStore.name;
    }
    public static final Wallet getWallet() {
        return msgStore.currentWallet;
    }

    public static void getNewMsgAddress(Context context) {
        AppService.generateMessageNewAddress(context);
    }
    public static void createSeed(Context context) {
        if(msgStore.currentSeed==null) {
            String fullSeed=SeedRandomGenerator.generateNewSeed();
            Seeds.Seed msgSeed=new Seeds.Seed();
            msgSeed.value=fullSeed.toCharArray();
            msgSeed.id=Long.valueOf(System.currentTimeMillis()).toString();
            msgSeed.name="messaging";
            msgSeed.isdefault=true;
            setSeed(context,msgSeed);
        }
    }
    public static void updateMessageData(Context context, Wallet wallet, List<Transfer> transfers, List<Address> addresses) {
        if(msgStore.currentSeed!=null) {
            SharedPreferences sp = context.getSharedPreferences(msgStore.currentSeed.id, Context.MODE_PRIVATE);
            JSONArray trans = new JSONArray();
            for (Transfer t : transfers) {
                trans.put(t.toJson());
            }
            SpManager.setEncryptedPreference(sp, PREF_TRANSFERS, trans.toString());
            JSONArray jar = new JSONArray();
            for (Address add : addresses) {
                jar.put(add.toJson());
            }
            SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());
            //Wallet run.wallet.monero.wallet = new Wallet(seed.id,balance,System.currentTimeMillis());
            MsgStore.addUpdateWallet(context, msgStore.currentSeed, wallet);
        }
        //if(msgStore.currentSeed!=null && seed.id.equals(msgStore.currentSeed.id)) {
        reinit(context);
        //}
    }
    public static void addUpdateWallet(Context context, Seeds.Seed seed, Wallet wallet) {
        SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
        SpManager.setEncryptedPreference(sp, PREF_WALLET, wallet.toJson().toString());
    }
    public static void reinit(Context context) {
        loadSeedData(context);
    }
    private static void loadSeedData(Context context) {
        loadWallet(context);
        loadAddresses(context);
        loadTransfers(context);
    }

    public static final List<Address> getAddresses() {
        return msgStore.addresses;
    }
    public static final List<Transfer> getTransfers() {
        return msgStore.transfers;
    }


    public static Seeds.Seed getSeed() {
        //Log.e("CURR-SEED-2",msgStore.currentSeed.id+"--"+msgStore.currentSeed.value.length);
        return msgStore.currentSeed;
    }
    private static void loadSeed(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_STORE, Context.MODE_PRIVATE);
        String jString = SpManager.getEncryptedPreference(sp, PREF_MSG_SEED, "");
        if(!jString.isEmpty()) {
            JSONObject job=new JSONObject(jString);
            Seeds.Seed seed = new Seeds.Seed();
            seed.value=job.optString(Seeds.J_VALUE).toCharArray();
            seed.name=job.optString(Seeds.J_NAME);
            seed.isdefault=job.optBoolean(Seeds.J_DEFAULT);
            seed.id=job.optString(Seeds.J_ID);
            msgStore.currentSeed=seed;
        }
    }
    private static void setSeed(Context context, Seeds.Seed seed) {
        SharedPreferences sp = context.getSharedPreferences(SP_STORE, Context.MODE_PRIVATE);

        JSONObject job=new JSONObject();
        msgStore.currentSeed=seed;

        job.put(Seeds.J_ID,seed.id);
        job.put(Seeds.J_VALUE,String.valueOf(seed.value));
        job.put(Seeds.J_NAME,seed.name);
        job.put(Seeds.J_DEFAULT,seed.isdefault);

        sp.edit().putString(PREF_MSG_SEED,job.toString()).commit();

    }
    private static void loadAddresses(Context context) {
        if(msgStore.currentSeed!=null) {
            SharedPreferences sp = context.getSharedPreferences(msgStore.currentSeed.id, Context.MODE_PRIVATE);
            String jarrayString = SpManager.getEncryptedPreference(sp, PREF_ADDRESSES, "[]");
            //Log.e("MSG-LOAD-ADDRESS",jarrayString);
            msgStore.addresses.clear();
            try {
                JSONArray jar = new JSONArray(jarrayString);
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject job = jar.getJSONObject(i);
                    Address add = new Address(job.optString("address"), job.optBoolean("used"));
                    msgStore.addresses.add(add);
                }
            } catch (Exception e) {
            }
        }
    }
    public static void addAddress(Context context, MessageNewAddressResponse response) {
        //if(seed!=null) {

            SharedPreferences sp = context.getSharedPreferences(msgStore.currentSeed.id, Context.MODE_PRIVATE);
            //String jarrayString=SpManager.getEncryptedPreference(sp, PREF_ADDRESSES,"[]");
            JSONArray jar = new JSONArray();
            for(String add: response.getAddresses()) {
                Address address = new Address(add,false);
                jar.put(address.toJson());
            }
            //Log.e("MSG-SAVE-ADDRESS","NO ACTION:   "+jar.toString());

            SpManager.setEncryptedPreference(sp, PREF_ADDRESSES, jar.toString());
            //if(store.currentSeed.id.equals(seed.id)) {
            loadAddresses(context);
            //}

    }
    private static void loadWallet(Context context) {
        if(msgStore.currentSeed!=null) {
            SharedPreferences sp = context.getSharedPreferences(msgStore.currentSeed.id, Context.MODE_PRIVATE);
            String jobString = SpManager.getEncryptedPreference(sp, PREF_WALLET, "[]");
            msgStore.currentWallet=null;
            try {
                JSONObject job = new JSONObject(jobString);
                if(job!=null) {
                    msgStore.currentWallet=new Wallet(job);
                }
            } catch (Exception e) {
            }
        }
    }


    private static void loadTransfers(Context context) {
        if(msgStore.currentSeed!=null) {
            SharedPreferences sp = context.getSharedPreferences(msgStore.currentSeed.id, Context.MODE_PRIVATE);
            String jarrayString = SpManager.getEncryptedPreference(sp, PREF_TRANSFERS, "[]");
            //String jarrayString = sp.getString(PREF_SEEDS,"[]");
            msgStore.transfers.clear();
            try {
                JSONArray jar = new JSONArray(jarrayString);
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject job = jar.getJSONObject(i);
                    Transfer add = new Transfer(job);
                    msgStore.transfers.add(add);
                }
            } catch (Exception e) {
            }
        }
    }
    public static List<Transfer> addTransfers(Context context, Seeds.Seed seed, List<Transfer> transfers) {
        List<Transfer> ntransfers = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(seed.id, Context.MODE_PRIVATE);
        String jarrayString = SpManager.getEncryptedPreference(sp, PREF_TRANSFERS, "[]");

        try {
            JSONArray jar = new JSONArray(jarrayString);
            for (int i = 0; i < jar.length(); i++) {
                JSONObject job = jar.getJSONObject(i);
                Transfer add = new Transfer(job);
                ntransfers.add(add);
            }
        } catch (Exception e) {
        }
        for(Transfer tran: transfers) {
            ntransfers.add(tran);
        }
        JSONArray trans = new JSONArray();
        for (Transfer t : ntransfers) {
            trans.put(t.toJson());
        }
        SpManager.setEncryptedPreference(sp, PREF_TRANSFERS, trans.toString());
        return transfers;
    }
}
