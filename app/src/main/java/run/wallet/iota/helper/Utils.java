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

package run.wallet.iota.helper;

import android.content.Context;
import android.content.SharedPreferences;

import jota.model.Transfer;
import jota.utils.SeedRandomGenerator;
import jota.utils.TrytesConverter;
import run.wallet.common.ui.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;



import com.mobapphome.mahencryptorlib.MAHEncryptor;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

import run.wallet.common.Currency;

/**
 * This class provides some utility method used across the app
 */
public class Utils {

    private static final String PRIVATE_SHARED_KEY="nj4H5bj3h8d%$nn8*nsW£";

    public static String stripHttp(String fromString) {
        return fromString.replace("http:", "").replace("https:", "").replace("udp:", "")
                .replaceAll("/","").replaceAll("\\/", "");

    }
     public static final String encryptMessageForTransfer(String textMessage, String tag, String privateSharedKey) {
        String message=Base64.encodeToString(textMessage.getBytes(),false);
        try {
            MAHEncryptor mahEncryptor = MAHEncryptor.newInstance(tag+PRIVATE_SHARED_KEY);
            message=mahEncryptor.encode(message);

        } catch (Exception e) {}

        message=Base64.encodeToString(textMessage.getBytes(),false);
        message=TrytesConverter.toTrytes(message);
        return message;

    }
    private static final String blank ="";
    public static String removeTrailingNines(String str ){
        if (str == null || str.isEmpty()){
            return blank;
        }
        char[] chars = str.toCharArray();int length,index ;length = str.length();
        index = length -1;
        for (; index >=0;index--)
        {
            if (chars[index] != '9'){
                break;}
        }
        return (index == length-1) ? str :str.substring(0,index+1);
    }
    public static final String decryptMessageFromTransfer(String trytesMessage, String trytesTag, String privateSharedKey) {

        trytesMessage=removeTrailingNines(trytesMessage);
        String message=TrytesConverter.toString(trytesMessage);


        message = new String(Base64.decode(message.getBytes()));

        try {
            MAHEncryptor mahEncryptor = MAHEncryptor.newInstance(trytesTag+PRIVATE_SHARED_KEY);
            message = mahEncryptor.decode(message);

        } catch (Exception e) {}
        message = new String(Base64.decode(message.getBytes()));
        return message;
    }
    public static void fixListView(final ListView lv, final SwipeRefreshLayout swipeLayout) {
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lv == null || lv.getChildCount() == 0) ? 0 : lv.getChildAt(0).getTop();
                swipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
    }

    public static String getPasswordId() {
        return "RI"+System.currentTimeMillis()+"D9";
    }
    /**
     * @return the currency of the run.wallet.monero.wallet
     */
    public static Currency getBaseCurrency() {
        return new Currency("IOT");
    }

    public static Currency getConfiguredAlternateCurrency(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new Currency(prefs.getString(Constants.PREFERENCE_WALLET_VALUE_CURRENCY, "USD"));
    }

    public static String timeStampToDate(long timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp);
        return df.format(date);
    }

    public static File getExternalIotaDirectory(Context context) {
        try {
            File cacheDir = new File(context.getExternalCacheDir(), "iota");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            return cacheDir;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int createNewID(){
        Date now = new Date();
        return Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
    }
}

