package run.wallet.iota.model;

import run.wallet.common.json.JSONObject;

/**
 * Created by coops on 23/12/17.
 */

public class Wallet {



    private long balance;
    private long balancePendingIn;
    private long balancePendingOut;
    private long lastUpdate;
    private String seedId;
    private int status;

    public Wallet(String seedid, long balance, long lastupdate) {
        this.seedId=seedid;
        this.setBalance(balance);
        this.setLastUpdate(lastupdate);
    }

    public void setStatus(int STATUS_) {
        status=STATUS_;
    }
    public int getStatus() {
        return status;
    }
    public Wallet(JSONObject job) {
        if(job!=null) {
            seedId=job.optString("seedid");
            balance=job.optLong("balance");
            setLastUpdate(job.optLong("lastupdate"));
            balancePendingIn=job.optLong("pendingin");
            balancePendingOut=job.optLong("pendingout");
        }

    }
    public JSONObject toJson() {
        JSONObject job = new JSONObject();
        job.put("seedid",seedId);
        job.put("balance", balance);
        job.put("lastupdate", getLastUpdate());
        job.put("pendingin", balancePendingIn);
        job.put("pendingout", balancePendingOut);
        return job;
    }
    public String getSeedId() {
        return seedId;
    }
    public long getBalance() {
        return balance;
    }
    public long getBalanceDisplay() {
        int type=Store.getBalanceDisplayType();
        switch(type) {
            case 2:
                return balance+balancePendingOut+balancePendingIn;
            case 1:
                return balance+balancePendingOut;
            default:
                return balance;
        }
    }
    public long getBalancePendingIn() {
        return balancePendingIn;
    }
    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }


    public void setBalancePendingIn(long balancePendingIn) {
        this.balancePendingIn = balancePendingIn;
    }

    public long getBalancePendingOut() {
        return balancePendingOut;
    }

    public void setBalancePendingOut(long balancePendingOut) {
        this.balancePendingOut = balancePendingOut;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
