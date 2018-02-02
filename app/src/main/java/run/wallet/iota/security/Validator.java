package run.wallet.iota.security;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;


public final class Validator {

    private static final String packageName = "run.wallet";

    public static boolean isValidCaller() {
        Exception ex = new Exception();
        if(ex.getStackTrace().length>2) {
            String fromClass = ex.getStackTrace()[2].getClassName();
            if(fromClass.startsWith(packageName)) {
                return true;
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
