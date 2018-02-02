package run.wallet.iota.model;


import java.text.DecimalFormat;

import run.wallet.common.json.JSONObject;

/**
 * Created by coops on 24/12/17.
 */

public class Ticker extends Tick {

    private long id;
    private int excid;
    private String cp;
    private double high;
    private double ask;
    private double bid;
    private double low;
    private double qvol;
    private long ts;
    private double vwap;

    private static final double toMiota=1000000D;
    private static final DecimalFormat df= new DecimalFormat("#,###,###,##0.00");
    private static final DecimalFormat dfs= new DecimalFormat("#,###,###,##0.0000");

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
    public Ticker(JSONObject fromjson) {
        super(fromjson);
        cp=json.optString("cp");
        //last=json.optDouble("last");
        high=json.optDouble("high");
        ask=json.optDouble("ask");
        bid=json.optDouble("bid");
        low=json.optDouble("low");
        //vol=json.optDouble("vol");
        qvol=json.optDouble("qvol");
        vwap=json.optDouble("vwap");
        ts=json.optLong("ts");


    }
    public JSONObject getJson() {
        return json;
    }
    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the excid
     */
    public int getExcid() {
        return excid;
    }

    /**
     * @param excid the excid to set
     */
    public void setExcid(int excid) {
        this.excid = excid;
    }

    /**
     * @return the cp
     */
    public String getCp() {
        return cp;
    }

    /**
     * @param cp the cp to set
     */
    public void setCp(String cp) {
        this.cp = cp;
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
     * @return the high
     */
    public double getHigh() {
        return high;
    }

    /**
     * @param high the high to set
     */
    public void setHigh(double high) {
        this.high = high;
    }

    /**
     * @return the ask
     */
    public double getAsk() {
        return ask;
    }

    /**
     * @param ask the ask to set
     */
    public void setAsk(double ask) {
        this.ask = ask;
    }

    /**
     * @return the bid
     */
    public double getBid() {
        return bid;
    }

    /**
     * @param bid the bid to set
     */
    public void setBid(double bid) {
        this.bid = bid;
    }

    /**
     * @return the low
     */
    public double getLow() {
        return low;
    }

    /**
     * @param low the low to set
     */
    public void setLow(double low) {
        this.low = low;
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
    public double getQvol() {
        return qvol;
    }

    /**
     * @param qvol the qvol to set
     */
    public void setQvol(double qvol) {
        this.qvol = qvol;
    }

    /**
     * @return the ts
     */
    public long getTs() {
        return ts;
    }

    /**
     * @param ts the ts to set
     */
    public void setTs(long ts) {
        this.ts = ts;
    }

    /**
     * @return the vwap
     */
    public double getVwap() {
        return vwap;
    }

    /**
     * @param vwap the vwap to set
     */
    public void setVwap(double vwap) {
        this.vwap = vwap;
    }


}
