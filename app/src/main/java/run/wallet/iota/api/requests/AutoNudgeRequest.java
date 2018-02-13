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

import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Transfer;

public class AutoNudgeRequest extends ApiRequest {

    private int minWeightMagnitude = Constants.PREF_MIN_WEIGHT_DEFAULT;
    private int depth = Constants.PREF_TRANSFER_DEPTH_DEFAULT;


    public AutoNudgeRequest() {
        super();

    }

}
