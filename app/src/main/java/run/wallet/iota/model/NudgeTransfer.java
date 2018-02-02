package run.wallet.iota.model;

import run.wallet.common.json.JSONObject;

/**
 * Created by coops on 25/01/18.
 */

public class NudgeTransfer {
    public static final int NUDGE_PENDING=0;
    public static final int NUDGE_CONFIRM=1;

    public int status;
    public String seedShort;
    public Transfer transfer;
    protected NudgeTransfer(Seeds.Seed seed, Transfer transfer) {
        this.seedShort=seed.getSystemShortValue();
        this.transfer=transfer;
    }
    public NudgeTransfer(JSONObject job) {
        status=job.optInt("status");
        seedShort=job.optString("seed");
        transfer=new Transfer(job.optJSONObject("tran"));
    }
    public JSONObject toJson() {
        JSONObject job=new JSONObject();
        job.put("seed",seedShort);
        job.put("tran",transfer.toJson());
        job.put("status",status);
        return job;
    }
}
