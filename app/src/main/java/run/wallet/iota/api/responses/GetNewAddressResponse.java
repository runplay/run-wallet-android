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

import android.util.Log;

import java.util.ArrayList;
import java.util.List;


import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;

public class GetNewAddressResponse extends ApiResponse {

    private List<String> addresses = new ArrayList<>();
    private Seeds.Seed seed;

    public final Seeds.Seed getSeed() {
        return seed;
    }

    public GetNewAddressResponse(Seeds.Seed seed,jota.dto.response.GetNewAddressResponse apiResponse) {
        this.seed=seed;
        //if(apiResponse.getAddresses().isEmpty())
        //    Log.e("GNA","new address EMPTY !!!!!!!!!!!!!!!!!!!!!!!!1");
        for(String address: apiResponse.getAddresses()) {
            //Log.e("GNA","new addres: "+address);
            try {
                addresses.add(address);
            } catch(Exception e) {
                Log.e("GNA001","error: "+e.getMessage());
            }
        }


        //for(String )
        setDuration(apiResponse.getDuration());
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

}
