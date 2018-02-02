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

import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.NodeInfoRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;

import jota.RunIotaAPI;
import run.wallet.iota.model.Nodes;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;

public class NodeInfoRequestHandler extends IotaRequestHandler {
    public NodeInfoRequestHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return NodeInfoRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        NodeInfoResponse info=null;
        Nodes.Node cnode=Store.getNode();
        boolean trynew=false;
        try {
            //Log.e("GETNODE",cnode.ip);
            info=new NodeInfoResponse(this.apiProxy.getNodeInfo());
        } catch(Exception e) {}
        if(info==null) {
            cnode.deadcount+=1;
            Store.updateNode(context,cnode);
            trynew=true;
            Store.addFailedNodeAttempt();
        } else {
            cnode.deadcount=0;
            if(info.isSyncOk()) {
                long sval=info.getLatestMilestoneIndex()-info.getLatestSolidSubtangleMilestoneIndex();
                if(sval>1) {
                    cnode.syncVal=sval;
                    trynew=true;
                } else {
                    cnode.syncVal=0;
                }

            } else {
                if(info.isSyncLoading()) {
                    cnode.syncVal=100;
                } else {
                    cnode.syncVal=info.getLatestMilestoneIndex()-info.getLatestSolidSubtangleMilestoneIndex();
                }

                trynew=true;
            }
            cnode.lastused=System.currentTimeMillis();
            //Log.e("NODE","TRY SYNC: "+cnode.ip+" - "+cnode.syncVal);
            Store.updateNode(context,cnode);
        }


        //Log.e("NODE","sync val: "+trynew+" - "+Store.getFailedNodeAttempt());
        if(trynew && Store.getFailedNodeAttempt()<5) {
            Nodes.Node trynode = Store.getNextNode();
            //Log.e("NODE","CHECK ANOTHER: "+trynode.ip);
            if(!trynode.ip.equals(cnode.ip)) {
                Store.changeNode(trynode);
                //Log.e("NODE","NOT SYNCHED TRY ANOTHER: "+trynode.ip);
                AppService.getNodeInfo(context);
            }
        }
        return info;
    }
}
