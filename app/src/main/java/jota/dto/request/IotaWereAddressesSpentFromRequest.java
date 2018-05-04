package jota.dto.request;

import jota.IotaAPICommands;


/**
 * This class represents the core API request 'getInclusionStates'.
 **/
public class IotaWereAddressesSpentFromRequest extends IotaCommandRequest {

    private String[] addresses;

    /**
     * Initializes a new instance of the IotaWereAddressesSpentFromRequest class.
     */
    public IotaWereAddressesSpentFromRequest(final String[] addresses) {

        super(IotaAPICommands.WERE_ADDRESSES_SPENT_FROM);
        this.addresses = addresses;
    }

    /**
     * Create a new instance of the IotaWereAddressesSpentFromRequest class.
     */
    public static IotaWereAddressesSpentFromRequest createWereAddressSpendFromRequest(String[] addresses) {
        return new IotaWereAddressesSpentFromRequest(addresses);
    }


    public String[] getAddresses() {
        return addresses;
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }
}
