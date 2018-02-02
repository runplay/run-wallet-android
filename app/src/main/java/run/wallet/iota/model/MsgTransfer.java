package run.wallet.iota.model;

/**
 * Created by coops on 02/01/18.
 */

public class MsgTransfer extends Transfer {
    public MsgTransfer(long timestamp, String address, String hash, Boolean persistence,
                    long value, String message, String tag) {
        super(timestamp,address,hash,persistence,value,message,tag);
    }
}
