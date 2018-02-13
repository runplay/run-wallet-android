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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jota.RunIotaAPI;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.AutoNudgeRequest;
import run.wallet.iota.api.requests.NudgeRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.NudgeResponse;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Sf;
import run.wallet.iota.model.Nodes;
import run.wallet.iota.model.NudgeTransfer;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.service.AppService;

public class AutoNudgeHandler extends IotaRequestHandler {
    public AutoNudgeHandler(RunIotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return AutoNudgeRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest inrequest) {
        return doNudge(apiProxy,context,inrequest);
    }

    public static ApiResponse doNudge(RunIotaAPI apiProxy, Context context,ApiRequest inrequest) {
        ApiResponse response=new ApiResponse();
        Store.loadNudgeTransfers(context);
        List<NudgeTransfer> nudgeTransfers=Store.getNudgeTransfers();
        Store.init(context,false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int nudgeAttempts= Sf.toInt(prefs.getString(Constants.PREF_TRANSFER_NUDGE_ATTEMPTS, ""+Constants.PREF_TRANSFER_NUDGE_ATTEMPTS_VALUE));

        if(nudgeAttempts>0) {
            Nodes.Node node = Store.getNode();
            if (node != null) {
                RunIotaAPI api = new RunIotaAPI.Builder().protocol(node.protocol).host(node.ip).port(((Integer) node.port).toString()).build();

                jota.dto.response.GetNodeInfoResponse nir = null;
                try {
                    nir = api.getNodeInfo();
                } catch (Exception e) {
                    node = Store.getNode();
                    try {
                        nir = api.getNodeInfo();
                    } catch (Exception e2) {
                    }
                }
                if (nir != null) {
                    //Log.e("AUTONUDGE", "Running nudge transfers, milestone: " + nir.getLatestMilestoneIndex());
                    if (nir.getLatestMilestoneIndex() == nir.getLatestSolidSubtangleMilestoneIndex()) {
                        Map<String, NudgeTransfer> refreshSeedShorts = new HashMap<>();
                        List<NudgeTransfer> removeFromNudges=new ArrayList<>();
                        int len = nudgeTransfers.size();

                        // strictly this way incase one gets added from another method
                        for (int i = 0; i < len; i++) {
                            NudgeTransfer ntran = nudgeTransfers.get(i);
                            int useval = Constants.PREF_TRANSFER_NUDGE_MILESTONES_VALUE;
                            if (ntran.transfer.getValue() > 0) {
                                useval = Constants.PREF_TRANSFER_NUDGE_MILESTONES_VALUE + 25;  // Redundant --- wait 2 more before trying receiving iota payments, let the other wallet get a chance
                            }
                            //Log.e("AUTONUDGE", "test: "+useval+" - -"+ntran.transfer.getValue()+" = " + ntran.transfer.getHash() + " ::::::::::::::::::::::::::::::: hash: " + ntran.transfer.getMilestone());

                            if (ntran.transfer.getMilestone() < nir.getLatestMilestoneIndex() - useval
                                    && ntran.transfer.getNudgeCount()<nudgeAttempts) {
                                //Log.e("AUTONUDGE", "run nudge: mstone: " + ntran.transfer.getMilestone() +"--"+nir.getLatestMilestoneIndex()+"--"+useval+ "-- val: " + ntran.transfer.getValue() + " -- hash: " + ntran.transfer.getHash());
                                try {
                                    Seeds.Seed seed=null;
                                    for(Seeds.Seed tseed: Store.getSeedList()) {
                                        if(tseed.getSystemShortValue().equals(ntran.seedShort)) {
                                            seed=tseed;
                                        }
                                    }
                                    if(seed!=null) {
                                        boolean hascompleted = false;
                                        List<Transfer> allTransfers = Store.getTransfers(context, seed);
                                        List<String> hashes=new ArrayList<>();
                                        for(Transfer transfer: allTransfers) {
                                            if(transfer.getValue()==ntran.transfer.getValue() && transfer.getAddress().equals(ntran.transfer.getAddress())) {
                                                if(transfer.isCompleted()
                                                        || transfer.isMarkDoubleSpend()) {
                                                    hascompleted = true;
                                                }
                                                hashes.add(transfer.getHash());
                                            }
                                        }
                                        List<jota.model.Transaction> transactions=new ArrayList<>();
                                        if(!hascompleted) {
                                            transactions = api.findTransactionsObjectsByHashes(hashes.toArray(new String[hashes.size()]));

                                            if (!transactions.isEmpty()) {
                                                for (jota.model.Transaction transaction : transactions) {
                                                    if (transaction.getPersistence() != null && transaction.getPersistence().booleanValue()) {
                                                        hascompleted = true;
                                                    }
                                                }
                                            }
                                        }
                                        if (hascompleted || hashes.isEmpty() || transactions.isEmpty()) {
                                            ntran.status = NudgeTransfer.NUDGE_CONFIRM;
                                            //refreshSeedShorts.put(ntran.seedShort, ntran);
                                            removeFromNudges.add(ntran);
                                            //refreshSeedShorts.put(ntran.seedShort, ntran);
                                        } else {
                                            //Log.e("NUDGE","Nudging now .............................");
                                            ApiResponse resp = NudgeRequestHandler.doNudge(api,context,new NudgeRequest(seed,ntran.transfer));
                                            if(resp instanceof NudgeResponse) {
                                                NudgeResponse nresponse = (NudgeResponse) resp;
                                                //ReplayBundleResponse replay = api.replayBundle(ntran.transfer.getHash(), Constants.PREF_TRANSFER_DEPTH_DEFAULT, Store.getMinWeightDefaultDefault());
                                                if (nresponse.getSuccessfully()) {
                                                    ntran.status = NudgeTransfer.NUDGE_CONFIRM;
                                                    refreshSeedShorts.put(ntran.seedShort, ntran);
                                                }
                                            }
                                            removeFromNudges.add(ntran);
                                        }
                                    } else {
                                        //refreshSeedShorts.put(ntran.seedShort, ntran);
                                        removeFromNudges.add(ntran);
                                    }
                                } catch (Exception e) {
                                    removeFromNudges.add(ntran);
                                }
                            }
                        }
                        if (!refreshSeedShorts.isEmpty()) {
                            List<Seeds.Seed> seeds = Store.getSeedList();
                            for (String seedShort : refreshSeedShorts.keySet()) {
                                for (Seeds.Seed seed : seeds) {
                                    if (String.valueOf(seed.value).startsWith(seedShort)) {
                                        AppService.getAccountData(context, seed);
                                        break;
                                    }
                                }

                            }
                            Store.saveNudgeTransfers(context);
                        }
                        if(!removeFromNudges.isEmpty()) {
                            Store.removeNudgeTransfer(context,removeFromNudges);
                        }


                    }
                }

            }
        }

        return response;
    }
}
