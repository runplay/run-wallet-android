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

import jota.model.Transaction;
import run.wallet.iota.helper.Audit;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.MsgStore;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.TransferTransaction;

public class MessageSendResponse extends ApiResponse {

    private Boolean[] successfully;

    public MessageSendResponse(Context context, Seeds.Seed seed, jota.dto.response.SendTransferResponse apiResponse) {
        successfully = apiResponse.getSuccessfully();
        setDuration(apiResponse.getDuration());
        List<Address> alreadyAddress = MsgStore.getAddresses();
        List<Transfer> transfers = new ArrayList<>();
        long tval=0;
        long ts=0;
        long totalValue=0;
        long timestamp=0;
        String address="";
        String hash="";
        String tag="";
        String destinationAddress="";
        Boolean persistence=false;
        String message = "";
        long value=0;
        List<TransferTransaction> transactions=new ArrayList<>();
        for (Transaction trx : apiResponse.getTransactions()) {



            message=trx.getSignatureFragments();
            address = trx.getAddress();
            persistence = trx.getPersistence();
            if(persistence==null)
                persistence=Boolean.FALSE;
            totalValue = trx.getValue();

            tval+=totalValue;
            if (trx.getCurrentIndex() == 0) {
                timestamp = trx.getAttachmentTimestamp();
                tag = trx.getTag();
                destinationAddress = address;
                hash = trx.getHash();
            }
            ts=timestamp;
            long totalin=0;
            long totalout=0;
            if(totalValue<0) {
                totalout=totalValue;
            } else {
                totalin=totalValue;
            }
            Address hasAddress = Store.isAlreadyAddress(address, alreadyAddress);
//persistence!=null && persistence.booleanValue() &&
            if (value != 0 && hasAddress != null) {
                transactions.add(new TransferTransaction(hasAddress.getAddress(), value));
            }


        }
        Transfer addtransfer=new Transfer(timestamp, destinationAddress, hash, persistence, totalValue, message, tag);
        addtransfer.setTransactions(transactions);
        transfers.add(addtransfer);
        MsgStore.addTransfers(context,seed,transfers);
    }

    public Boolean[] getSuccessfully() {
        return successfully;
    }

    public void setSuccessfully(Boolean[] successfully) {
        this.successfully = successfully;
    }
}