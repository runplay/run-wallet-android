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

package run.wallet.iota.api.handler;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jota.RunIotaAPI;
import jota.dto.response.FindTransactionResponse;
import jota.dto.response.GetNewAddressResponse;
import jota.dto.response.GetTransferResponse;
import jota.error.ArgumentException;
import jota.model.Bundle;
import jota.utils.SeedRandomGenerator;
import jota.utils.StopWatch;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.MessageFirstLoadRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.helper.Audit;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.MsgStore;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.Wallet;
import run.wallet.iota.service.AppService;

public class MessageFirstLoadRequestHandler extends IotaRequestHandler {
    public MessageFirstLoadRequestHandler(RunIotaAPI iotaApi, Context context) {
        super(iotaApi, context);
    }
    private static final String defmessage = "";
    @Override
    public Class<? extends ApiRequest> getType() {
        return MessageFirstLoadRequest.class;
    }


    @Override
    public ApiResponse handle(ApiRequest request) {


        try {
            //Log.e("FIRST-TIME-MSG","called");






            StopWatch stopWatch = new StopWatch();
            MessageFirstLoadRequest firstLoadRequest = (MessageFirstLoadRequest) request;


            GetTransferResponse gtr=null;
            List<Address> allAddresses = new ArrayList<>();

            int start=0;
            final int addcount=5;


            while(true) {
                GetNewAddressResponse gnr = apiProxy.getNewAddress(String.valueOf(Store.getSeedRaw(context,MsgStore.getSeed())),
                        firstLoadRequest.getSecurity(),
                        start,
                        false,
                        start+addcount,
                        true);
                for(String add: gnr.getAddresses()) {
                    //Log.e("FIRST-TIME-MSG","CALC ADDRESS: "+allAddresses+" -- "+stopWatch.getElapsedTimeSecs());
                    final FindTransactionResponse tr = apiProxy.findTransactionsByAddresses(add);
                    Address newaddress= new Address(add,false,true);
                    if (tr.getHashes().length == 0) {
                        newaddress.setAttached(false);
                    }
                    allAddresses.add(newaddress);

                }
                int countempty=0;
                for(int i=allAddresses.size()-1;i>=0 && i>=allAddresses.size()-4; i--) {
                    if(!allAddresses.get(i).isAttached()) {
                        countempty++;
                    }
                }
                if(countempty>=2) {

                    break;
                }
                start+=addcount;
            }
            List<String> transactionaddresses = new ArrayList<>();
            for(Address add: allAddresses) {
                if(add.isAttached())
                    transactionaddresses.add(add.getAddress());
            }
            if (!transactionaddresses.isEmpty()) {
                try {
                    Bundle[] bundles = apiProxy.bundlesFromAddresses(transactionaddresses.toArray(new String[transactionaddresses.size()]), true);
                    gtr = GetTransferResponse.create(bundles, stopWatch.getElapsedTimeMili());
                } catch (Exception e) {
                    Log.e("FIRST-LOAD-MSG","ex: "+e.getMessage());
                }

            } else {
                gtr=GetTransferResponse.create(new Bundle[]{}, stopWatch.getElapsedTimeMili());
            }
            List<Transfer> transfers=new ArrayList<>();
            long seedTotal=0;
            Wallet wallet = new Wallet(MsgStore.getSeed().id,seedTotal,System.currentTimeMillis());
            //Audit.setTransfersToAddresses(firstLoadRequest.getSeed(),gtr,transfers,allAddresses,wallet);

            MsgStore.updateMessageData(context,wallet,transfers,allAddresses);

            //AppService.generateMessageNewAddress(context);
                //if()

        } catch (ArgumentException e) {
            Log.e("FIRST-TIME-MSG","ex: "+e.getMessage());
            return new NetworkError();
        }
        return new ApiResponse();
    }
}
/*
if(gtr!=null) {

                Bundle[] transferBundle = gtr.getTransfers();
                if (transferBundle != null) {

                    for (Bundle aTransferBundle : transferBundle) {
                        long totalValue = 0;
                        long timestamp = 0;
                        String address = "";
                        String hash = "";
                        Boolean persistence = false;
                        long value = 0;
                        String tag = "";
                        String destinationAddress = "";

                        String message = defmessage;
                        for (Transaction trx : aTransferBundle.getTransactions()) {

                            message = trx.getSignatureFragments();
                            address = trx.getAddress();
                            persistence = trx.getPersistence();
                            value = trx.getValue();

                            Address hasAddress=getAddressIfInList(address,allAddresses);

                            if (value != 0 && hasAddress!=null)
                                totalValue += value;

                            if (trx.getCurrentIndex() == 0) {
                                timestamp = trx.getAttachmentTimestamp() / 1000;
                                tag = trx.getTag();
                                destinationAddress = address;
                                hash = trx.getHash();
                            }

                            // check if sent transaction
                            if (transactionaddresses.contains(Checksum.addChecksum(address))) {
                                //Log.e("RESP","addedTransaction inc: "+Checksum.addChecksum(address));
                                boolean isRemainder = (trx.getCurrentIndex() == trx.getLastIndex()) && trx.getLastIndex() != 0;
                                //Log.e("RESP","addedTransaction inc: "+Checksum.addChecksum(address)+" -- "+value+" -- "+isRemainder);
                                if (value < 0 && !isRemainder) {
                                    if (hasAddress!=null && !hasAddress.isUsed()) {
                                        hasAddress.setUsed(true);
                                        //Log.e("RESP","addedTransaction 1 ");
                                        //addresses.remove(new Address(Checksum.addChecksum(address), false));
                                    }
                                } else {
                                    if(hasAddress!=null && !hasAddress.isAttached())
                                        hasAddress.setAttached(true);
                                }
                            }
                        }
                        seedTotal+=totalValue;
                        transfers.add(new Transfer(timestamp, destinationAddress, hash, persistence, totalValue, message, tag));
                        Log.e("FIRST-TIME","TRANSFER: "+transfers.get(transfers.size()-1).toJson());
                    }
                    // sort the addresses

                }

            }
 */