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

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import run.wallet.iota.helper.Audit;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.Wallet;

public class ReplayBundleResponse extends ApiResponse {

    private Boolean[] successfully;

    public ReplayBundleResponse(Context context, Seeds.Seed seed, jota.dto.response.RunReplayBundleResponse apiResponse) {
        successfully = apiResponse.getSuccessfully();
        setDuration(apiResponse.getDuration());
        List<Address> alreadyAddress = Store.getAddresses(context,seed);
        List<Transfer> transfers = new ArrayList<>();
        List<Transfer> alreadyTransfers=Store.getTransfers(context,seed);
        Wallet wallet=Store.getWallet(context,seed);



        Audit.populateTxToTransfers(apiResponse.getTrxs(),Store.getNodeInfo(),transfers,alreadyAddress);
        Audit.setTransfersToAddresses(seed, transfers, alreadyAddress, wallet, alreadyTransfers);
        Audit.processNudgeAttempts(context, seed, transfers);
        Store.updateAccountData(context, seed, wallet, transfers, alreadyAddress);
    }

    public Boolean[] getSuccessfully() {
        return successfully;
    }

    public void setSuccessfully(Boolean[] successfully) {
        this.successfully = successfully;
    }
}