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

import jota.model.Transfer;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;

public class GetBalanceAndFormatRequest extends SeedApiRequest {

    public List<String> addresses=new ArrayList<>();
    private Seeds.Seed seed;

    public GetBalanceAndFormatRequest(Seeds.Seed seed) {

        super(seed);
    }

    public GetBalanceAndFormatRequest(Seeds.Seed seed,List<String> addresses) {
        super(seed);
        this.addresses=addresses;
    }

}
