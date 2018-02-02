/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package run.wallet.iota.helper;

import java.text.DecimalFormat;

/**
 *
 * @author root
 */
public class Num {
    private static DecimalFormat df = new DecimalFormat( "#0.00" );
    private static long shouldbeMegs=1000*1024;

    public static String friendlyTimeDuration(long millis) {
        if(millis<60000) {
            return (millis/1000)+"s";
        } else if(millis<(Cal.MINUTES_1_IN_MILLIS*5)) {
            long seconds=((millis/1000)%60);
            if(seconds==0)
                return Double.valueOf(millis/Cal.MINUTES_1_IN_MILLIS).intValue() +"mins";
            return Double.valueOf(millis/Cal.MINUTES_1_IN_MILLIS).intValue() +"min, "+seconds+"s";
        } else {
            return Double.valueOf(millis/Cal.MINUTES_1_IN_MILLIS).intValue() +"mins";
        }
    }
    private static final int minute=60;
    public static String friendlyTimeDuration(int seconds) {
        if(seconds<minute) {
            return seconds+"s";
        } else if(seconds<(minute*5)) {
            if((seconds%60)==0)
                return Double.valueOf(seconds/minute).intValue() +"mins";
            return Double.valueOf(seconds/minute).intValue() +"min, "+(seconds%60)+"s";
        } else {
            return Double.valueOf(seconds/minute).intValue() +"mins";
        }
    }
    public static int getRandom(int min, int max) {
        int rand = Double.valueOf(((max+1-min)* Math.random())+min).intValue();
        if(rand>max)
            rand=max;
        return rand;
    }

    public static String btyesToFileSizeString(long fileSizeInBtyes) {
    	if(fileSizeInBtyes>shouldbeMegs)
    		return df.format((fileSizeInBtyes/1024D)/1024D)+" mb";
    	else
    		return df.format(fileSizeInBtyes/1024D)+" kb";
    }
    
    public static double getRandomDouble(double min, double max) {
        double rand = (((max+1-min)* Math.random())+min);
        if(rand>max)
            rand=max;
        return rand;
    }
}
