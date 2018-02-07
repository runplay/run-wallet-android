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
import run.wallet.iota.ui.fragment.PendingCancelledFragment;


public class WalletTransfersItemDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private Transfer transfer;
    private String hash;
    private int maxNudgeAttempts;


    public WalletTransfersItemDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        hash = bundle.getString("hash");


        transfer = Store.isAlreadyTransfer(hash,Store.getTransfers());
        if(transfer==null)
            dismiss();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        maxNudgeAttempts= Sf.toInt(prefs.getString(Constants.PREF_TRANSFER_NUDGE_ATTEMPTS, ""+Constants.PREF_TRANSFER_NUDGE_ATTEMPTS_VALUE));

        int useR = R.array.listOnWalletTransfersRecyclerViewClickDialog;
        if(transfer.isMarkDoubleAddress()) {
            useR = R.array.listOnWalletTransfersRecyclerViewClickDialogCancelled;
        } else if(transfer.isCompleted() || transfer.isMarkDoubleSpend() || transfer.isAttachment()) {
            useR = R.array.listOnWalletTransfersRecyclerViewClickDialogCompleted;
        } else {
            if(maxNudgeAttempts==0 || maxNudgeAttempts>=transfer.getNudgeCount() || transfer.getTimestamp()<System.currentTimeMillis()- (Cal.HOURS_1_IN_MILLIS)) {
                useR = R.array.listOnWalletTransfersRecyclerViewClickDialogResend;
            }
        }


        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.transfer))
                .setItems(useR, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        Fragment fragment;
        final Bundle bundle = new Bundle();
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
                    if(transfer!=null && maxNudgeAttempts!=0) {
                        AppService.nudgeTransaction(getActivity(),Store.getCurrentSeed(),transfer);
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_transaction_already_confirmed), Snackbar.LENGTH_LONG).show();
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
    }
}