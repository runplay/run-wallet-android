package run.wallet.iota.model;


import java.text.DecimalFormat;

import run.wallet.common.json.JSONObject;

/**
 * Created by coops on 24/12/17.
 */

public class Tick {



    protected JSONObject json;

    protected double last;
    protected double vol;

    protected static final double toMiota=1000000D;
    protected static final DecimalFormat df= new DecimalFormat("#,###,###,##0.00");
    protected static final DecimalFormat dfs= new DecimalFormat("#,###,###,##0.0000");

    public String getIotaValString(long iota) {
        double val=getIotaValue(iota);
        if(val<0.01) {
            return dfs.format(val);
        }
        return df.format(val);
    }
    public double getIotaValue(long iota) {
        return (iota/toMiota)*getLast();
    }
    public Tick(JSONObject fromjson) {
        json=fromjson;
        if(json==null)
            json=new JSONObject();
        if(json!=null) {
            last=json.optDouble("last");
            vol=json.optDouble("vol");
        }

    }
    public JSONObject getJson() {
        return json;
    }


    /**
     * @return the last
     */
    public double getLast() {
        return last;
    }

    /**
     * @param last the last to set
     */
    public void setLast(double last) {
        this.last = last;
    }


    /**
     * @return the vol
     */
    public double getVol() {
        return vol;
    }

    /**
     * @param vol the vol to set
     */
    public void setVol(double vol) {
        this.vol = vol;
    }

    /**
     * @return the qvol
     */



}
