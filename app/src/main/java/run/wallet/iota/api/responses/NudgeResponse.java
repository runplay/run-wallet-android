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

package run.wallet.iota.api.responses;

import java.util.ArrayList;
import java.util.List;

public class NudgeResponse extends ApiResponse {

    private boolean successfully;
    private List<String> hashes=new ArrayList();

    public NudgeResponse(jota.dto.response.RunSendTransferResponse apiResponse) {
        setSuccessfully(apiResponse.getSuccessfully());
        setDuration(apiResponse.getDuration());
        getHashes().addAll(apiResponse.getHashes());
    }

    public boolean getSuccessfully() {
        return successfully;
    }

    public void setSuccessfully(Boolean[] successfully) {
        this.successfully = successfully!=null && successfully.length>0?successfully[0]:false;
    }

    public List<String> getHashes() {
        return hashes;
    }

    public void setHashes(List<String> hashes) {
        this.hashes = hashes;
    }
}