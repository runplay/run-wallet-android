package run.wallet.iota.api.requests;

import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;

/**
 * Created by coops on 15/01/18.
 */

public class AddressSecurityChangeRequest extends SeedApiRequest {

    private int security=2;
    private Address address;
    public AddressSecurityChangeRequest(Seeds.Seed seed, Address changeAddress, int setSecurity) {
        super(seed);
        security=setSecurity;
        address=changeAddress;
    }
    public int getSecurity() {
        return security;
    }


    public Address getAddress() {
        return address;
    }
}
