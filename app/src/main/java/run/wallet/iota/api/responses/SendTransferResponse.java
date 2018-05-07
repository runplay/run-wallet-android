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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


import jota.model.Bundle;
import jota.model.Transaction;
import run.wallet.iota.api.requests.SendTransferRequest;
import run.wallet.iota.helper.Audit;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Sf;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.TransferTransaction;
import run.wallet.iota.model.Wallet;
import run.wallet.iota.service.AppService;

public class SendTransferResponse extends ApiResponse {

    private Boolean[] successfully;

    public SendTransferResponse(Context context, Seeds.Seed seed,jota.dto.response.SendTransferResponse apiResponse) {
        successfully = apiResponse.getSuccessfully();
        setDuration(apiResponse.getDuration());
        List<Address> alreadyAddress = Store.getAddresses(context,seed);
        List<Transfer> transfers = new ArrayList<>();
        List<Transfer> alreadyTransfers=Store.getTransfers(context,seed);
        Wallet wallet=Store.getWallet(context,seed);


        Audit.populateTxToTransfers(apiResponse.getTransactions(),Store.getNodeInfo(),transfers,alreadyAddress);
        Audit.setTransfersToAddresses(seed, transfers, alreadyAddress, wallet, alreadyTransfers);
        Audit.processNudgeAttempts(context, seed, transfers);
        Store.updateAccountData(context, seed, wallet, transfers, alreadyAddress);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int nudgeAttempts = Sf.toInt(prefs.getString(Constants.PREF_TRANSFER_NUDGE_ATTEMPTS, "" + Constants.PREF_TRANSFER_NUDGE_ATTEMPTS_VALUE));
        if(nudgeAttempts>0) {
            String hash = apiResponse.getTransactions().get(0).getHash();

            Transfer quicknudge = Store.isAlreadyTransfer(hash, transfers);
            if (quicknudge != null) {
                AppService.nudgeTransactionInstant(context,
                        seed
                        , quicknudge, true);

            }
        }


        /*
        for(Transfer transfer: transfers) {
            if(transfer.getValue()==0) {
                Address already = null;

                try {
                    already=Store.isAlreadyAddress(new Address(transfer.getAddress(),false),alreadyAddress);
                } catch(Exception e) {
                    //Log.e("TRANS-RESP-BAD",""+e.getMessage());
                }
                if(already!=null) {
                    already.setAttached(true);
                    Store.updateAddress(context,seed,already);
                }
            }
        }
        */

    }

    public Boolean[] getSuccessfully() {
        return successfully;
    }

    public void setSuccessfully(Boolean[] successfully) {
        this.successfully = successfully;
    }
}