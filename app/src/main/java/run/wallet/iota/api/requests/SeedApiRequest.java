package run.wallet.iota.api.requests;

import run.wallet.iota.model.Seeds;

/**
 * Created by coops on 06/01/18.
 */

abstract class SeedApiRequest extends ApiRequest {



    protected Seeds.Seed seed;

    public Seeds.Seed getSeed() {
        return seed;
    }
    private SeedApiRequest() {

    }
    public SeedApiRequest(Seeds.Seed seed) {
        this.seed=seed;

    }
}
