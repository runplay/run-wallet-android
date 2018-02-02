package run.wallet.iota.helper;


import android.util.Log;

/**
 * Created by coops on 13/12/15.
 */
public class Tim {
    private static long startedat;

    public static void start() {
        startedat= System.currentTimeMillis();

        Log.e("TIM","Tim started..........");
    }
    public static void printTime(String extra) {
        long dt=(System.currentTimeMillis()-startedat);
        Log.e("TIM","----------------------------------------------    Tim: "+((System.currentTimeMillis()-startedat))+ "mil -- "+extra+" -- "+(dt/1000D)+" secs");
    }

}
