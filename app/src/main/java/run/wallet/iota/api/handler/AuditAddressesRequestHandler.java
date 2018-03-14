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
import jota.dto.response.GetNodeInfoResponse;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.AuditAddressesRequest;
import run.wallet.iota.api.requests.GetNewAddressRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;

public class AuditAddressesRequestHandler extends IotaRequestHandler {
    public AuditAddressesRequestHandler(RunIotaAPI iotaApi, Context context) {
        super(iotaApi, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return AuditAddressesRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest inrequest) {
        AuditAddressesRequest request = (AuditAddressesRequest) inrequest;

        // check less than 2, 1..iteself, and no others
        if(AppService.countSeedRunningTasks(request.getSeed())<2) {
            List<Address> addresses = Store.getAddresses(context,request.getSeed());


            List<Address> getaddresses =new ArrayList<>();
            if(!addresses.isEmpty()) {
                for(Address address: addresses) {
                    if(!address.isAttached()) {
                        getaddresses.add(address);
                    }
                }
            }

            if(!getaddresses.isEmpty()) {
                GetNodeInfoResponse info=apiProxy.getNodeInfo();
                if(info!=null && info.getLatestMilestoneIndex()==info.getLatestSolidSubtangleMilestoneIndex()) {
                    //List<String> addstr = new ArrayList<>();
                    int count=0;

                    for (Address add : getaddresses) {
                        FindTransactionResponse tr1 = null;
                        try {
                            tr1 = apiProxy.findTransactionsByAddresses(add.getAddress());
                        } catch (Exception e){}
                        if(tr1!=null) {
                            if (tr1.getHashes().length == 0) {
                                AppService.attachNewAddress(context, request.getSeed(), add.getAddress());
                            } else {
                                add.setAttached(true);
                                Store.updateAddress(context, request.getSeed(), add);
                                AppService.refreshEvent();
                            }
                            //addstr.add(add.getAddress());
                        }
                        if (++count > 1 || tr1==null) ;
                            break;
                    }

                }

            } else {

                int countemptyattached=0;
                for(Address address: addresses) {
                    if(address.isAttached()&& address.getValue()==0 && !address.isUsed() && !address.isPig()) {
                        countemptyattached++;
                    }
                }
                Store.loadDefaults(context);
                int countmin=Store.getAutoAttach();
                //Log.e("AUDIT","Gen, have already empty attached: "+countemptyattached+", min: "+countmin);

                if(countemptyattached<countmin) {
                    //Log.e("AUDIT","Gen new address");
                    try {
                        GetNewAddressRequest gnr = new GetNewAddressRequest(request.getSeed());
                        gnr.setIndex(addresses.size());

                        final GetNewAddressResponse gna = apiProxy.getNewAddress(String.valueOf(request.getSeed().value), gnr.getSecurity(),
                                addresses.size(), gnr.isChecksum(), 1, gnr.isReturnAll());
                        run.wallet.iota.api.responses.GetNewAddressResponse gnar = new run.wallet.iota.api.responses.GetNewAddressResponse(request.getSeed(),gna);
                        Store.addAddress(context,gnr,gnar);
                        AppService.auditAddressesWithDelay(context, request.getSeed());
                    } catch (Exception e) {}

                } else {
                    //Log.e("AUDIT","no address need creating");
                }

            }

        } else {
            //Log.e("AUDIT","Called but skipped.. hopefully good");
        }

        return new ApiResponse();
    }
}
