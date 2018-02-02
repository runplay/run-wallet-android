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

import java.util.List;

import jota.model.Input;

public class GetBalanceAndFormatResponse extends ApiResponse {

    //private Boolean[] successfully;
    private long balance;
    List<Input> inputs;

    public GetBalanceAndFormatResponse(jota.dto.response.GetBalancesAndFormatResponse apiResponse) {
        //successfully = apiResponse..getSuccessfully();
        balance=apiResponse.getTotalBalance();
        inputs=apiResponse.getInputs();

        setDuration(apiResponse.getDuration());
    }

    //public Boolean[] getSuccessfully() {
   //     return successfully;
    //}
    public long getBalance() {
        return balance;
    }
    public List<Input> getInputs() {
        return inputs;
    }
}