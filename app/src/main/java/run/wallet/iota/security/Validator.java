package run.wallet.iota.security;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import run.wallet.iota.api.handler.AutoNudgeHandler;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.activity.MainActivity;


public final class Validator {

    private static final String packageName = "run.wallet.iota";

    public static boolean isValidCaller() {
        Exception ex = new Exception();
        if(ex.getStackTrace().length>2) {
            String fromClass = ex.getStackTrace()[2].getClassName();
            //Log.e("ISVALID",""+fromClass);
            if(fromClass.startsWith(packageName)) {
                return true;
            }
        }

        return false;
    }
    private static final List<Class> validInit=new ArrayList<Class>();
    static {
        validInit.add(AppService.class);
        validInit.add(MainActivity.class);
        validInit.add(AutoNudgeHandler.class);
        validInit.add(Store.class);
    }
    public static boolean isValidInitaliser() {
        Exception ex = new Exception();
        if(ex.getStackTrace().length>2) {
            String fromClass = ex.getStackTrace()[2].getClassName();
            for(Class test: validInit) {
                if (fromClass.equals(test.getCanonicalName())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static final String PLAY_STORE_APP_ID = "com.android.vending";

    public static boolean verifyInstaller(final Context context) {

        final String installer = context.getPackageManager()

                .getInstallerPackageName(context.getPackageName());
//Log.e("INSTALLER","is: "+installer);
        return installer != null

                && installer.startsWith(PLAY_STORE_APP_ID);

    }
}
