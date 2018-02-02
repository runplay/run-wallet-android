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

import java.util.ArrayList;
import java.util.List;

import run.wallet.iota.model.Seeds;

public class GetAccountDataRequest extends SeedApiRequest {


    private int security = 2;
    private int index = 0;
    private boolean checksum = false;
    private int total = 0;
    private boolean returnAll = true;
    private int start = 0;
    private int end = 0;
    private boolean inclusionState = true;
    private long threshold = 0;
    private boolean force;
    private List<String> presetAddresses=new ArrayList<>();


    public GetAccountDataRequest(Seeds.Seed seed) {

        super(seed);
    }
    public GetAccountDataRequest(Seeds.Seed seed,boolean force) {

        super(seed);
        this.setForce(force);
    }
    public void setSingleAddressRefresh(String forAddress) {
        if(forAddress!=null) {
            presetAddresses.clear();
            presetAddresses.add(forAddress);
        }
    }
    public String getIfSingleAddressOrNull() {
        if(!presetAddresses.isEmpty()) {
            return presetAddresses.get(0);
        }
        return null;
    }

    public Seeds.Seed getSeed() {
        return seed;
    }



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

    public boolean isChecksum() {
        return checksum;
    }

    public void setChecksum(boolean checksum) {
        this.checksum = checksum;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isReturnAll() {
        return returnAll;
    }

    public void setReturnAll(boolean returnAll) {
        this.returnAll = returnAll;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isInclusionState() {
        return inclusionState;
    }

    public void setInclusionState(boolean inclusionState) {
        this.inclusionState = inclusionState;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
