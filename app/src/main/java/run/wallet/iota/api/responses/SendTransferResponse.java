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
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


import jota.model.Transaction;
import run.wallet.iota.helper.Audit;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.TransferTransaction;

public class SendTransferResponse extends ApiResponse {

    private Boolean[] successfully;

    public SendTransferResponse(Context context, Seeds.Seed seed,jota.dto.response.SendTransferResponse apiResponse) {
        successfully = apiResponse.getSuccessfully();
        setDuration(apiResponse.getDuration());
        List<Address> alreadyAddress = Store.getAddresses(context,seed);
        List<Transfer> transfers = new ArrayList<>();
        //long tval=0;
        long ts=0;
        long totalValue=0;
        long timestamp=0;
        String address="";
        String hash="";
        String tag="";
        String destinationAddress="";
        Boolean persistence=false;
        String message = "";

        List<TransferTransaction> transactions=new ArrayList<>();
        for (Transaction trx : apiResponse.getTransactions()) {

            message=trx.getSignatureFragments();
            address = trx.getAddress();
            persistence = trx.getPersistence();
            if(persistence==null)
                persistence=Boolean.FALSE;
            long value=trx.getValue();
            totalValue += value;

            value+=totalValue;
            if (trx.getCurrentIndex() == 0) {
                timestamp = trx.getAttachmentTimestamp();
                tag = trx.getTag();
                destinationAddress = address;
                hash = trx.getHash();
            }
            Address hasAddress = Store.isAlreadyAddress(address, alreadyAddress);
            if (value != 0 && hasAddress != null) {
                transactions.add(new TransferTransaction(hasAddress.getAddress(), value));
            }


        }
        Transfer addtransfer=new Transfer(timestamp, destinationAddress, hash, persistence, totalValue, message, tag);
        if(Store.getNodeInfo()!=null) {
            addtransfer.setMilestone(Store.getNodeInfo().getLatestMilestoneIndex());
        }
        addtransfer.setTransactions(transactions);
        transfers.add(addtransfer);

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

    }

    public Boolean[] getSuccessfully() {
        return successfully;
    }

    public void setSuccessfully(Boolean[] successfully) {
        this.successfully = successfully;
    }
}