/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package run.wallet.common;

import java.text.DecimalFormat;

/**
 *
 * @author root
 */
public class Num {
    private static DecimalFormat df = new DecimalFormat( "#0.00" );
    private static long shouldbeMegs=1000*1024;

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
