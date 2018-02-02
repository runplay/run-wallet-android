package run.wallet.iota.ui;

import android.content.SharedPreferences;

import com.mobapphome.mahencryptorlib.MAHEncryptor;

/**
 * Created by coops on 18/12/17.
 */

public class SpManager {
    private static final String key="j32RXI£%COI£ee38FR34984£ijew*&jsgjkkk";
    public static boolean setEncryptedPreference(SharedPreferences sharedPref, String PREF_, String value) {
        try {
            MAHEncryptor mahEncryptor = MAHEncryptor.newInstance(key);
            //String toDecode = mahEncryptor.decode(sharedPref.getString("protect_PW", ""));
            String toEncode = mahEncryptor.encode(value);
            sharedPref.edit().putString(PREF_,toEncode).commit();
            return true;
        } catch (Exception e) {}
        return false;
    }
    public static String getEncryptedPreference(SharedPreferences sharedPref, String PREF_,String noPref) {
        try {
            MAHEncryptor mahEncryptor = MAHEncryptor.newInstance(key);
            return mahEncryptor.decode(sharedPref.getString(PREF_, noPref));

        } catch (Exception e) {}
        return noPref;
    }
}
