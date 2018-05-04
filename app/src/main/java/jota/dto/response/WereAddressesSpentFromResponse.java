package jota.dto.response;

import java.util.List;

/**
 * Created by coops on 30/04/18.
 */

public class WereAddressesSpentFromResponse  extends AbstractResponse {

    private boolean[] states;

    /**
     * Gets the states.
     *
     * @return The states.
     */
    public boolean[] getStates() {
        return states;
    }

}
