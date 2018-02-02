package run.wallet.iota.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import run.wallet.iota.helper.SeedValidator;
import run.wallet.iota.security.Validator;
import run.wallet.iota.ui.SpManager;

/**
 * Created by coops on 16/12/17.
 */

public class Seeds {
    private static final List<Seed> seeds = new ArrayList<>();
    //private static Seed activeSeed;

    private static final String PREF_SEEDS = "seeds";

    protected static final String SP_WALLETS="wallets";

    protected static final String J_ID="id";
    protected static final String J_VALUE="jv";
    protected static final String J_NAME="jn";
    protected static final String J_DEFAULT="jd";
    protected static final String J_APPGEN="ag";

    public static class Seed {
        public String id;
        public char[] value;
        public String name;
        public boolean isdefault;
        public boolean isappgenerated;

        protected Seed() {

        }

        public String getShortValue() {
            return String.valueOf(value).substring(0,9);
        }
        public String getSystemShortValue() {
            return String.valueOf(value).substring(0,16);
        }
    }


    protected final Seed getSeedById(String id) {
        for(Seed seed: seeds) {
            if(seed.id.equals(id))
                return seed;
        }
        return null;
    }

    protected Seeds(Context context) {
        seeds.clear();
        if(Validator.isValidCaller()) {
            //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences sp = context.getSharedPreferences(SP_WALLETS, Context.MODE_PRIVATE);
            String jarrayString = SpManager.getEncryptedPreference(sp, PREF_SEEDS, "[]");
            //String jarrayString = sp.getString(PREF_SEEDS,"[]");
            try {
                JSONArray jar = new JSONArray(jarrayString);
                for (int i = 0; i < jar.length(); i++) {
                    JSONObject job = jar.getJSONObject(i);
                    Seed seed = new Seed();
                    seed.value = job.optString(J_VALUE).toCharArray();
                    seed.name = job.optString(J_NAME);
                    seed.isdefault = job.optBoolean(J_DEFAULT);
                    seed.id = job.optString(J_ID);
                    seed.isappgenerated = job.optBoolean(J_APPGEN);
                    seeds.add(seed);

                }
            } catch (Exception e) {
            }
        }

    }

    protected final List<Seed> getSeeds() {
        return seeds;
    }
    /*
    protected void addSeed(Context context, char[] seedString,String name,boolean isappgenerate) {
        addSeed(context,seedString,name,false,isappgenerate);
    }
    */
    protected synchronized void removeSeed(Context context, String seedId) {
        if(Validator.isValidCaller()) {
            Seed removeseed = null;
            for (Seed seed : seeds) {
                if (seed.id.equals(seedId)) {
                    removeseed = seed;
                    break;

                }
            }
            if (removeseed != null) {
                seeds.remove(removeseed);
                save(context);
            }
        }
    }
    protected synchronized void addSeed(Context context, char[] seedString,String name,boolean isdef,boolean isappgenerate) {
        String seedStr=String.valueOf(seedString);
        if(SeedValidator.isSeedValid(context,seedStr)==null) {
            Seed seed = new Seed();
            seed.name=name;
            seed.value=seedString;
            seed.id=Long.valueOf(System.currentTimeMillis()).toString();
            seed.isdefault=isdef;
            seed.isappgenerated=isappgenerate;
            seeds.add(seed);
            save(context);
        }

    }
    protected void save(Context context) {
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sp = context.getSharedPreferences(SP_WALLETS,Context.MODE_PRIVATE);
        JSONArray jar = new JSONArray();
        try {
            for(Seed seed: seeds) {
                JSONObject job = new JSONObject();
                job.put(J_NAME,seed.name);
                job.put(J_VALUE,new String(seed.value));
                job.put(J_DEFAULT,seed.isdefault);
                job.put(J_ID,seed.id);
                job.put(J_APPGEN,seed.isappgenerated);
                jar.put(job);
            }
        } catch (Exception e) {}
        if(jar.length()>0) {
            SpManager.setEncryptedPreference(sp,PREF_SEEDS,jar.toString());
        }
    }

    protected List<String> getSeedNames() {
        List<String> names = new ArrayList<>();
        for(Seed seed: seeds) {
            names.add(seed.name);
        }
        return names;
    }
    protected Seeds.Seed getSeed(int index) {
        if(seeds.size()>index) {
            return seeds.get(index);
        }
        return null;
    }
    protected char[] getSeedValue(int index) {
        if(seeds.size()>index) {
            return seeds.get(index).value;
        }
        return null;
    }
}
