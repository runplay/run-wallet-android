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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import run.wallet.R;
import run.wallet.iota.api.TaskManager;
import run.wallet.iota.api.requests.ReplayBundleRequest;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.activity.MainActivity;
import run.wallet.iota.ui.fragment.PendingCancelledFragment;


public class WalletTransfersItemDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private String address;
    private boolean isConfirmed;
    private boolean isAddressDouble;
    private boolean isCancelled;
    private boolean isAttachment;
    private String hash;

    public WalletTransfersItemDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        address = bundle.getString("address");
        isConfirmed = bundle.getInt("isConfirmed") != 0;
        isCancelled = bundle.getInt("isCancelled") != 0;
        isAttachment = bundle.getInt("isAttachment") != 0;
        isAddressDouble=bundle.getInt("isAddressDouble") != 0;
        hash = bundle.getString("hash");
        int useR = R.array.listOnWalletTransfersRecyclerViewClickDialog;
        if(isAddressDouble)
            useR = R.array.listOnWalletTransfersRecyclerViewClickDialogCancelled;
        else if(isConfirmed || isCancelled || isAttachment)
            useR = R.array.listOnWalletTransfersRecyclerViewClickDialogCompleted;

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
                ClipData clipAddress = ClipData.newPlainText(getActivity().getString(R.string.address), address);
                clipboard.setPrimaryClip(clipAddress);
                break;
            case 1:
                ClipData clipHash = ClipData.newPlainText(getActivity().getString(R.string.hash), hash);
                clipboard.setPrimaryClip(clipHash);
                break;

            case 2:
                if(isAddressDouble) {
                    Transfer gottransfer=Store.isAlreadyTransfer(hash,Store.getTransfers());
                    if(gottransfer!=null) {
                        Store.setCacheTransfer(gottransfer);
                        UiManager.openFragmentBackStack(getActivity(), PendingCancelledFragment.class);
                    }

                } else if (!isConfirmed) {
                    AppService.replayBundleTransaction(getActivity(),Store.getCurrentSeed(),hash,address);
                } else  {
                    Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_transaction_already_confirmed), Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }
}