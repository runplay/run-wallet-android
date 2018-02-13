package jota.dto.response;

import jota.model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of api request 'sendTransfer'.
 *
 *
 * This was cloned from SendTransferResponse and
 * changed to pass the hashes
 * not the transactions that can be used to search for the hashes when already done
 *
 *
 *
 **/
public class RunSendTransferResponse extends AbstractResponse {

    private List<String> hashes = new ArrayList<>();
    private Boolean[] successfully;

    /**
     * Initializes a new instance of the SendTransferResponse class.
     */
    public static RunSendTransferResponse create(List<String> hashes, Boolean[] successfully, long duration) {
        RunSendTransferResponse res = new RunSendTransferResponse();
        res.hashes = hashes;
        res.successfully = successfully;
        res.setDuration(duration);
        return res;
    }

    /**
     * Gets the transactions.
     *
     * @return The transactions.
     */
    public List<String> getHashes() {
        return hashes;
    }

    /**
     * Sets the transactions.
     *
     * @param hashes The transactions.
     */
    public void setTransactions(List<String> hashes) {
        this.hashes = hashes;
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
}