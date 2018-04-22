package run.wallet.iota.helper;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;

import javax.annotation.Nullable;

import info.guardianproject.netcipher.proxy.OrbotHelper;

import run.wallet.iota.ui.activity.MainActivity;
import run.wallet.iota.ui.fragment.AboutFragment;

public class TorHelper {

    private static TorHelper tor = new TorHelper();

    private static final String SP_TOR="sp_tor_data";
    public static final String PREF_TOR_FORCE = "pref_tor_force";
    private static final String PREF_TOR_HOST = "torHost";
    private static final String PREF_TOR_PORT = "torPort";
    private static final String PREF_TOR_USER = "torUser";
    private static final String PREF_TOR_PASS = "torPass";
    private static final String PREF_TOR_NAV = "pref_tor_nav";

    public static final int STATUS_OFF=0;
    public static final int STATUS_STARTING=1;
    public static final int STATUS_STOPPING=2;
    public static final int STATUS_ON=5;

    private static final int PORT_TOR = 8118;
    private static final String HOST_TOR = "localhost";

    private static final String ORBOT_NAME="org.torproject.android";
    public static final String ORBOT_GOOGLE_PLAY="https://play.google.com/store/apps/details?id="+ORBOT_NAME;

    private Context context;
    private WebView webView;
    private SharedPreferences sp;
    private int status=STATUS_OFF;

    public static final boolean isEnabled() {
        return tor.status==STATUS_ON?true:false;
    }

    private boolean force;
    private String host;
    private int port;
    private String user;
    private String password;
    private boolean torNav;
    private OrbotHelper orbotHelper;

    private TorHelper() {

    }
    public static  void save() {
        if(tor.context!=null) {
            tor.sp.edit().putString(PREF_TOR_HOST,tor.host).putInt(PREF_TOR_PORT,tor.port)
                .putString(PREF_TOR_USER,tor.user).putString(PREF_TOR_PASS,tor.password).putBoolean(PREF_TOR_FORCE,tor.force)
                .putBoolean(PREF_TOR_NAV,tor.torNav).commit();
        }
    }
    public static void destroy() {
        tor=new TorHelper();

    }
    public static void init(Context context, WebView webView) {
        if(tor.context==null) {
            tor.status=STATUS_OFF;
            tor.context = context;
            tor.webView = webView;
            tor.sp = context.getSharedPreferences(SP_TOR, Context.MODE_PRIVATE);
            tor.host = tor.sp.getString(PREF_TOR_HOST, HOST_TOR);
            tor.port = tor.sp.getInt(PREF_TOR_PORT, PORT_TOR);
            tor.user = tor.sp.getString(PREF_TOR_USER, "");
            tor.password = tor.sp.getString(PREF_TOR_PASS, "");
            tor.force = tor.sp.getBoolean(PREF_TOR_FORCE, false);
            tor.torNav = tor.sp.getBoolean(PREF_TOR_NAV, false);
            if (tor.force) {
                torEnable();
            }
        }
    }

    public static void openGooglePlayTor() {
        try {
            tor.context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ORBOT_NAME)));
        } catch (Exception e) {
            tor.context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ORBOT_GOOGLE_PLAY)));
        }
    }
    public static final boolean isTorInstalled(Context context) {
        return OrbotHelper.isOrbotInstalled(context);
    }
    public static int getTorStatus() {
        return tor.status;
    }
    private static boolean isTorForce() {
        return tor.sp.getBoolean(PREF_TOR_FORCE, false);
    }

    public static void torEnable() {
        if(tor.context!=null && isTorInstalled(tor.context)) {
            if (tor.webView != null) {
                CookieHelper.acceptCookies(tor.webView, false);
                CookieHelper.deleteCookies();
            }

            try {
                tor.orbotHelper=OrbotHelper.get(tor.context);
                tor.orbotHelper.addStatusCallback(
                        // this status callback seems to have no effect on test devices, No events called
                        new OrbotHelper.SimpleStatusCallback() {
                            @Override
                            public  void onEnabled(Intent statusIntent) {
                                //Log.e("TOR","Listner is ENABLED");
                                tor.status=STATUS_ON;
                            }

                            @Override
                            public  void onStatusTimeout() {
                                //Log.e("TOR","Listner is TIMEOUT");
                                OrbotHelper.get(tor.context).removeStatusCallback(this);
                                tor.status=STATUS_OFF;
                            }

                            @Override
                            public void onStarting() {
                                //Log.e("TOR","Listner is STARTING");
                                tor.status=STATUS_STARTING;
                            }

                            @Override
                            public void onStopping() {
                                //Log.e("TOR","Listner is STOPPING");
                                tor.status=STATUS_STOPPING;
                            }

                            @Override
                            public void onDisabled() {
                                //Log.e("TOR","Listner is DISABLED");
                                tor.status=STATUS_OFF;
                            }

                            @Override
                            public void onNotYetInstalled() {
                                //Log.e("TOR","Listner is onNotYetInstalled()");
                                tor.status=STATUS_OFF;
                            }
                        });
                tor.orbotHelper.requestStartTor(tor.context);
                System.setProperty("http.proxyHost", "localhost");
                System.setProperty("http.proxyPort", "" + PORT_TOR);
                System.setProperty("https.proxyHost", "localhost");
                System.setProperty("https.proxyPort", "" + PORT_TOR);
                tor.status=STATUS_ON;

                //WebkitProxy.setProxy(MainActivity.class.getName(), tor.context, null, "localhost", PORT_TOR);

            } catch (Exception e) {
                Log.e("TOR","ex: "+e.getMessage());
            }
            if (tor.webView != null) {
                tor.webView.reload();
            }
        }
    }

    public static void torDisable() {
        if(tor.context!=null) {
            CookieHelper.deleteCookies();

            try {
                System.clearProperty("http.proxyHost");
                System.clearProperty("http.proxyPort");
                System.clearProperty("https.proxyHost");
                System.clearProperty("https.proxyPort");
                //WebkitProxy.resetProxy(MainActivity.class.getName(), tor.context);

                if(tor.webView!=null)
                    CookieHelper.acceptCookies(tor.webView, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static final void restartApp() {
        Intent mStartActivity = new Intent(tor.context, MainActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(tor.context, 12374, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) tor.context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static boolean isForce() {
        return tor.force;
    }

    public static void setForce(boolean force) {
        tor.force = force;
    }

    public static String getHost() {
        return tor.host;
    }

    public static void setHost(String host) {
        tor.host = host;
    }

    public static int getPort() {
        return tor.port;
    }

    public static void setPort(int port) {
        tor.port = port;
    }

    public static String getUser() {
        return tor.user;
    }

    public static void setUser(String user) {
        tor.user = user;
    }

    public static String getPassword() {
        return tor.password;
    }

    public static void setPassword(String password) {
        tor.password = password;
    }

    public static boolean isTorNav() {
        return tor.torNav;
    }

    public static void setTorNav(boolean torNav) {
        tor.torNav = torNav;
    }

    private static class CookieHelper {
        public static void acceptCookies(WebView webView, boolean accept) {
            CookieManager.getInstance().setAcceptCookie(accept);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, accept);
            }
        }

        public static void deleteCookies() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookies(null);
            }
            CookieManager.getInstance().removeAllCookie();
        }
    }
}