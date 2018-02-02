/*
 * Copyright (C) 2017 IOTA Foundation
 *
 * Authors: pinpong, adrianziser, saschan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package run.wallet.iota.api.requests;

import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;

public class GetNewAddressRequest extends SeedApiRequest {


    private int security = 2;
    private int index = 0;
    private boolean checksum = false;
    private int total = 0;
    private boolean returnAll = false;

    public GetNewAddressRequest(Seeds.Seed seed) {

        super(seed);
        security= Store.getAddressSecurityDefault();
    }
    public GetNewAddressRequest(Seeds.Seed seed,int security) {

        super(seed);
        this.security= security;
    }
    public String getSeedValue() {
        return String.valueOf(seed.value);
    }
    public Seeds.Seed getSeed() {
        return seed;
    }
    //public void setSeed(String seed) {
    //    this.seed = seed;
    //}

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        this.security = security;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isChecksum() {
        return checksum;
    }

    public void setChecksum(boolean checksum) {
        this.checksum = checksum;
    }

    public boolean isReturnAll() {
        return returnAll;
    }

    public void setReturnAll(boolean returnAll) {
        this.returnAll = returnAll;
    }
}
