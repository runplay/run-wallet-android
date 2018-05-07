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

package run.wallet.iota.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jota.dto.response.GetNodeInfoResponse;
import jota.utils.Checksum;
import run.wallet.R;
import run.wallet.common.Cal;
import run.wallet.iota.api.TaskManager;
import run.wallet.iota.api.requests.ReplayBundleRequest;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Sf;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.activity.MainActivity;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.fragment.PendingCancelledFragment;
import run.wallet.iota.ui.fragment.WalletAddressesFragment;
import run.wallet.iota.ui.fragment.WalletTabFragment;
import run.wallet.iota.ui.fragment.WalletTransfersFragment;


public class WalletTransfersItemDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private Transfer transfer;
    private String hash;
    private int maxNudgeAttempts;
    private int cancelTransferHours;
    private boolean canCancel=false;
    private List<Integer> options=new ArrayList<>();
    private List<CharSequence> items=new ArrayList<>();
    //private static final int OPT_COPY_ADD=0;
    private static final int OPT_COPY_HASH=1;
    private static final int OPT_NUDGE=2;
    private static final int OPT_RESEND=3;
    private static final int OPT_CANCEL_WHY=4;
    private static final int OPT_CANCEL=5;
    private static final int OPT_INCLUDE=6;


    public WalletTransfersItemDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        hash = bundle.getString("hash");

        //final String copyAdd=getString(R.string.copy_address);
        final String copyHash=getString(R.string.copy_hash);
        final String nudge=getString(R.string.replay_bundle);
        final String resend=getString(R.string.resend_bundle);
        final String cancelWhy=getString(R.string.transfer_cancelled_why);
        final String cancel=getString(R.string.settings_cancel_transfer);
        final String include=getString(R.string.transfer_include);

        transfer = Store.isAlreadyTransfer(hash,Store.getTransfers());
        if(transfer==null)
            dismiss();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        boolean isCancellable=false;
        cancelTransferHours= Sf.toInt(prefs.getString(Constants.PREF_CANCEL_TRANSFER, ""+Constants.PREF_CANCEL_TRANSFER_DEFAULT));
        long asSeconds = cancelTransferHours*60*60*1000;
        long nowSeconds = System.currentTimeMillis();
        //Log.e("TRAN",transfer.getTimestamp()+"<"+nowSeconds+"-"+asSeconds);
        if(transfer.getTimestamp()<nowSeconds-asSeconds) {
            //Log.e("TRAN","can cancel transaction");
            canCancel=true;
        }
        maxNudgeAttempts= Sf.toInt(prefs.getString(Constants.PREF_TRANSFER_NUDGE_ATTEMPTS, ""+Constants.PREF_TRANSFER_NUDGE_ATTEMPTS_VALUE));

        //int useR = R.array.listOnWalletTransfersRecyclerViewClickDialog;
        //items.add(copyAdd);
        items.add(copyHash);
        //options.add(OPT_COPY_ADD);
        options.add(OPT_COPY_HASH);
        //if(!transfer.isIgnore()) {
            if (transfer.isMarkDoubleAddress()) {
                items.add(cancelWhy);
                options.add(OPT_CANCEL_WHY);
                //useR = R.array.listOnWalletTransfersRecyclerViewClickDialogCancelled;
            } else if (transfer.isCompleted() || transfer.isMarkDoubleSpend() || transfer.isAttachment()) {
                //useR = R.array.listOnWalletTransfersRecyclerViewClickDialogCompleted;
            } else {
                if (transfer.getTimestamp() < System.currentTimeMillis() - (Cal.MINUTES_1_IN_MILLIS * 10)) {
                    items.add(nudge);
                    items.add(resend);
                    options.add(OPT_NUDGE);
                    options.add(OPT_RESEND);
                    //useR = R.array.listOnWalletTransfersRecyclerViewClickDialogResend;
                } else {
                    items.add(nudge);
                    options.add(OPT_NUDGE);
                }
                if (canCancel) {
                    if(transfer.isIgnore()) {
                        items.add(include);
                        options.add(OPT_INCLUDE);
                    } else {
                        items.add(cancel);
                        options.add(OPT_CANCEL);
                    }
                }
            }
        //}
        CharSequence[] goitems = new String[items.size()];
        goitems = items.toArray(goitems);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.transfer)).setItems(goitems,this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        Fragment fragment;
        final Bundle bundle = new Bundle();
        final int selected = options.get(which);
        switch(selected) {
            /*
            case OPT_COPY_ADD:
                String useAddress=transfer.getAddress();
                try {
                    useAddress=Checksum.addChecksum(transfer.getAddress());
                } catch(Exception e){}
                ClipData clipAddress = ClipData.newPlainText(getActivity().getString(R.string.address), useAddress);
                clipboard.setPrimaryClip(clipAddress);
                break;
                */
            case OPT_COPY_HASH:
                ClipData clipHash = ClipData.newPlainText(getActivity().getString(R.string.hash), hash);
                clipboard.setPrimaryClip(clipHash);
                break;
            case OPT_NUDGE:
                Transfer transfer = Store.getCurrentTransferFromHash(hash);
                if(transfer!=null) {
                    AppService.nudgeTransaction(getActivity(),Store.getCurrentSeed(),transfer);
                }
                break;
            case OPT_RESEND:
                Transfer atransfer = Store.getCurrentTransferFromHash(hash);
                if(atransfer!=null) {
                    AppService.replayBundleTransaction(getActivity(),Store.getCurrentSeed(),hash,atransfer.getAddress());
                }
                break;
            case OPT_CANCEL_WHY:
                Transfer gottransfer=Store.isAlreadyTransfer(hash,Store.getTransfers());
                if(gottransfer!=null) {
                    Store.setCacheTransfer(gottransfer);
                    UiManager.openFragmentBackStack(getActivity(), PendingCancelledFragment.class);
                }
                break;
            case OPT_CANCEL:
            case OPT_INCLUDE:
                Transfer btransfer = Store.getCurrentTransferFromHash(hash);
                if(btransfer!=null) {
                    Store.markCurrentTransferIgnoreFromHash(getActivity(),hash,!btransfer.isIgnore());
                    WalletTransfersFragment.resetScroll();

                    WalletAddressesFragment.resetScroll();
                    WalletTransfersCardAdapter.setFilterAddress(null, null);
                    WalletAddressCardAdapter.load(getActivity(), true);
                    WalletTransfersCardAdapter.load(getActivity(), true);
                    UiManager.openFragment(getActivity(), WalletTabFragment.class);
                }

                break;
        }

/*
        switch (which) {
            case 0:
                String useAddress=transfer.getAddress();
                try {
                    useAddress=Checksum.addChecksum(transfer.getAddress());
                } catch(Exception e){}
                ClipData clipAddress = ClipData.newPlainText(getActivity().getString(R.string.address), useAddress);
                clipboard.setPrimaryClip(clipAddress);
                break;
            case 1:
                ClipData clipHash = ClipData.newPlainText(getActivity().getString(R.string.hash), hash);
                clipboard.setPrimaryClip(clipHash);
                break;

            case 2:
                //Log.e("DIALOG","GO");
                if(transfer.isMarkDoubleAddress()) {
                    Transfer gottransfer=Store.isAlreadyTransfer(hash,Store.getTransfers());
                    if(gottransfer!=null) {
                        Store.setCacheTransfer(gottransfer);
                        UiManager.openFragmentBackStack(getActivity(), PendingCancelledFragment.class);
                    }

                } else if (!transfer.isCompleted()) {
                    Transfer transfer = Store.getCurrentTransferFromHash(hash);
                    if(transfer!=null) {
                        AppService.nudgeTransaction(getActivity(),Store.getCurrentSeed(),transfer);
                    }

                } else  {
                    Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_transaction_already_confirmed), Snackbar.LENGTH_LONG).show();
                }
                break;
            case 3:
                if (!transfer.isCompleted()) {
                    Transfer transfer = Store.getCurrentTransferFromHash(hash);
                    if(transfer!=null) {
                        AppService.replayBundleTransaction(getActivity(),Store.getCurrentSeed(),hash,transfer.getAddress());
                    }
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_transaction_already_confirmed), Snackbar.LENGTH_LONG).show();
                }
        }
        */
    }
}