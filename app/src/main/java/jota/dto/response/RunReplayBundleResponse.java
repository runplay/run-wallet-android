package jota.dto.response;

import java.util.List;

import jota.model.Transaction;

/**
 * Response of api request 'replayBundle'.
 **/
public class RunReplayBundleResponse extends AbstractResponse {

    private Boolean[] successfully;
    private List<Transaction> trxs;

    /**
     * Initializes a new instance of the ReplayBundleResponse class.
     */
    public static RunReplayBundleResponse create(List<Transaction> intrxs,Boolean[] successfully, long duration) {
        RunReplayBundleResponse res = new RunReplayBundleResponse();
        res.successfully = successfully;
        res.setDuration(duration);
        res.trxs=intrxs;
        return res;
    }

    /**
     * Gets the successfully.
     *
     * @return The successfully.
     */
    public Boolean[] getSuccessfully() {
        return successfully;
    }

    /**
     * Sets the successfully.
     *
     * @param successfully The successfully.
     */
    public void setSuccessfully(Boolean[] successfully) {
        this.successfully = successfully;
    }

    public List<Transaction> getTrxs() {
        return trxs;
    }
}
