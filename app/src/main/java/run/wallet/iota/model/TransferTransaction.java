package run.wallet.iota.model;

import run.wallet.common.json.JSONObject;

/**
 * Created by coops on 10/01/18.
 */

public class TransferTransaction {
    private String address;
    private long value;
    private long payFromAddressZero=0;
    public TransferTransaction(String address, long value) {
        this.address=address;
        this.value=value;
    }
    public TransferTransaction(JSONObject job) {
        this.address=job.optString("a");
        this.value=job.optLong("v");
        this.payFromAddressZero=job.optLong("z");
    }
    private TransferTransaction() {

    }
    public JSONObject toJson() {
        JSONObject job = new JSONObject();
        job.put("a",address);
        job.put("v",value);
        job.put("z",payFromAddressZero);
        return job;
    }
    public TransferTransaction fromJson(JSONObject job) {
        TransferTransaction tb=new TransferTransaction();
        tb.address=job.optString("a");
        tb.value=job.optLong("v");
        return tb;
    }
    public String getAddress() {
        return address;
    }
    public long getPayFromAddressZero() {
        return payFromAddressZero;
    }

    public long getValue() {
        return value;
    }


    public boolean isPayFromAddressZero() {
        return payFromAddressZero==0?false:true;
    }
    public void setPayFromAddressValue(long payFromAddressValue) {
        this.payFromAddressZero = payFromAddressValue;
    }

}
