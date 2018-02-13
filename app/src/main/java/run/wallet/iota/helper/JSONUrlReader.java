package run.wallet.iota.helper;



import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;

import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;
import run.wallet.iota.model.Store;


public class JSONUrlReader {
	  private static int URL_TIMEOUT_MILLIS=15000; // 5 second timeout
	  private static HashMap<String,String> cookieStore= new HashMap<String,String>();
	  
	  private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }
    public static JSONObject readJsonFromUrlPlainText(Context context, String url)  {
        URL serverUrl = null;
        try {
            serverUrl=new URL(url);
        } catch(Exception e) {
            //Log.e("JSONpt1", "" + e.getMessage());
        }
        JSONObject json=null;
        InputStream is=null;
        try {
            is=serverUrl.openStream();
        } catch(IOException e) {
            //Log.e("JSONpt2",""+e.getMessage());
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            json= new JSONObject(jsonText);

        } catch(Exception e) {
            //Log.e("JSONpt3",""+e.getMessage());
        }
        try {
            is.close();
        } catch(Exception e) {
            //Log.e("JSONpt4",""+e.getMessage());
        }
        return json;
    }
    public static JSONObject readJsonObjectFromUrl(Context context, String url)  {
        URL serverUrl = null;
        HttpURLConnection urlConnection = null;
        try {
          serverUrl=new URL(url);
          urlConnection = (HttpURLConnection) serverUrl.openConnection();
        } catch(Exception e) {
          //Log.e("JSON1", "" + e.getMessage());
        }


        JSONObject json=null;
        String useCookie = cookieStore.get(url);
        InputStream is=null;

        if(useCookie!=null) {
          urlConnection.setRequestProperty("Cookie", useCookie);
        }
        try {
          urlConnection.setRequestProperty("User-Agent",getUserAgent(context));
          urlConnection.setRequestMethod("GET");
          urlConnection.setDoOutput(true);
          urlConnection.setDoInput(true);
          urlConnection.setConnectTimeout(URL_TIMEOUT_MILLIS);
          urlConnection.setReadTimeout(URL_TIMEOUT_MILLIS);
          urlConnection.setInstanceFollowRedirects(true);

        } catch(Exception e) {
          //Log.e("readJsonObjectFromUrl().error.msg","2:"+e.getMessage());
        }
        try {
          urlConnection.connect();
        } catch(Exception e) {
          cookieStore.remove(url);
          //Log.e("readJsonObjectFromUrl().error.msg","3 - connect(): "+e.getMessage());
        }
        try {
          is = urlConnection.getInputStream();
        } catch(IOException e) {
          //Log.e("JSON4",""+e.getMessage());
        }
        try {
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        //BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String jsonText = readAll(rd);
        //Log.e("GOTJSON",jsonText+"");
        json= new JSONObject(jsonText);

        } catch(Exception e) {
          //Log.e("JSON5",""+e.getMessage());
        }
        try {
          is.close();
        } catch(Exception e) {
          //Log.e("JSON6",""+e.getMessage());
        }
        return json;
    }
    private static String USER_AGENT=null;
    private static String getUserAgent(Context context) {
        if(USER_AGENT==null) {
            USER_AGENT="Mozilla/5.0 (Linux; U; Android "+ Build.VERSION.RELEASE+"; "+ Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry()+"; "+Build.MODEL+" Build/"+Build.ID+") rp wallet ("+getUniqueDeviceId(context)+" "+getVersionName(context)+"."+getVersionCode(context)+")";
        }

        return USER_AGENT;
    }
    private static String getVersionName(Context context) {
        try {
            PackageInfo manager=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return manager.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return "1";
        }
    }
    private static int getVersionCode(Context context) {
        try {
            PackageInfo manager=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return manager.versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return 1;
        }
    }
    private static String getUniqueDeviceId(Context context) {
        String m_szAndroidID="";
        String m_szDevIDShort="";

        m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.DEVICE.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10; //13 digits
        String m_sextra=Build.ID+Build.FINGERPRINT+Build.SERIAL;

        m_szAndroidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        String m_szLongID = m_szDevIDShort + m_szAndroidID + m_sextra;
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
        }
        byte p_md5Data[]=null;
        if(m==null) {
            p_md5Data= Base64.encodeToByte(m_szLongID.getBytes(),false);
        } else {
            m.update(m_szLongID.getBytes(),0,m_szLongID.length());
            p_md5Data= m.digest();
        }
        String m_szUniqueID="";
        for (int i=0;i<p_md5Data.length;i++) {
            int b =  (0xFF & p_md5Data[i]);
            if (b <= 0xF) m_szUniqueID+="0";
            m_szUniqueID+=Integer.toHexString(b);
        }
        m_szUniqueID = m_szUniqueID.toUpperCase();
        return m_szUniqueID;

    }
    public static JSONArray readJsonArrayFromUrl(Context context, String url)  {
        //Log.e("JSON",url);
        URL serverUrl = null;
        HttpURLConnection urlConnection = null;
        try {
            serverUrl=new URL(url);
            urlConnection = (HttpURLConnection) serverUrl.openConnection();
        } catch(Exception e) {
            //Log.e("JSON1", "" + e.getMessage());
        }


        JSONArray json=null;
        String useCookie = cookieStore.get(url);
        InputStream is=null;

        if(useCookie!=null) {
            urlConnection.setRequestProperty("Cookie", useCookie);
        }
        try {
            urlConnection.setRequestProperty("User-Agent",getUserAgent(context));
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(URL_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(URL_TIMEOUT_MILLIS);
            urlConnection.setInstanceFollowRedirects(true);

        } catch(Exception e) {
            //Log.e("readJsonObjectFromUrl().error.msg","2:"+e.getMessage());
        }
        try {
            //urlConnection.conn
            urlConnection.connect();
        } catch(Exception e) {
            cookieStore.remove(url);
            //Log.e("readJsonObjectFromUrl().error.msg","3 - connect(): "+e.getMessage());
        }
        try {
            is = urlConnection.getInputStream();
        } catch(IOException e) {
            //Log.e("JSON4",""+e.getMessage());
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            //BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String jsonText = readAll(rd);
            //Log.e("GOTJSON",jsonText+"");
            json= new JSONArray(jsonText);

        } catch(Exception e) {
            //Log.e("JSON5",""+e.getMessage());
        }
        try {
            is.close();
        } catch(Exception e) {
            //Log.e("JSON6",""+e.getMessage());
        }
        return json;
    }
}