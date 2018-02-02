package run.wallet.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by coops on 03/08/16.
 */
public class PemMan {


    public static final int REQUEST_BLUETOOTH = 9101;
    public static final int REQUEST_WRITE_STORAGE = 9111;
    public static final int REQUEST_CONTACTS=9211;



    public static void requestBluetoothPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN},
                REQUEST_BLUETOOTH);

    }
    public static final boolean hasBluetoothPermission(Context context) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED);
        return hasPermission;
    }


    public static final boolean hasContactsPermission(Context context) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
        return hasPermission;
    }
    public static final boolean hasFilePermission(Context context) {
        boolean hasPermission = (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        return hasPermission;
    }
    public static void requestFileWritePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);

    }
    public static void requestContactsPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_CONTACTS);

    }

}
