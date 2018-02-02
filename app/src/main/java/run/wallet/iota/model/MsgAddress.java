package run.wallet.iota.model;

/**
 * Created by coops on 02/01/18.
 */

public class MsgAddress extends Address {
    private String sharedprivatekey;

    public MsgAddress(String address, boolean used,String sharedkey) {
        super(address,used);
        sharedprivatekey=sharedkey;
    }
}
