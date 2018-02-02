package run.wallet.iota.model;

import run.wallet.common.json.JSONObject;

/**
 * Created by coops on 18/01/18.
 */

public class SystemMessage {
    private long date;
    private boolean disable;
    private boolean isread;
    private String message;

    public SystemMessage(JSONObject msgjson) {
        date=msgjson.optLong("date");
        disable=msgjson.optBoolean("dis");
        isread=msgjson.optBoolean("read");
        message=msgjson.optString("msg");

    }

    public JSONObject toJson() {
        JSONObject job=new JSONObject();
        job.put("date",date);
        job.put("dis",disable);
        job.put("msg",message);
        job.put("read", isread);
        return job;
    }

    public long getDate() {
        return date;
    }

    public boolean isDisable() {
        return disable;
    }

    public String getMessage() {
        return message;
    }

    public boolean isIsread() {
        return isread;
    }

    public void setIsread(boolean isread) {
        this.isread = isread;
    }
}
