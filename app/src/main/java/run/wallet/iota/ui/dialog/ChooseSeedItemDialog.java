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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

import java.util.List;

import run.wallet.R;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.fragment.ChooseSeedEditFragment;
import run.wallet.iota.ui.fragment.WalletAddressesFragment;
import run.wallet.iota.ui.fragment.WalletTabFragment;
import run.wallet.iota.ui.fragment.WalletTransfersFragment;


public class ChooseSeedItemDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private Seeds.Seed seed;
    private Activity activity;

    public ChooseSeedItemDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        activity=getActivity();
        String seedid = bundle.getString("seedid");

        List<Seeds.Seed> seedlist = Store.getSeedList();
        for(Seeds.Seed s: seedlist) {
            if(s.id.equals(seedid))
                seed=s;
        }
        if(seed!=null) {

            return new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.address))
                    .setItems(R.array.listOnSeedClickDialog, this)
                    .create();
        }
        return null;
    }
    private DialogInterface.OnDismissListener onDismissListener;
    private DialogInterface.OnDismissListener onDismissListener2;


    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener,DialogInterface.OnDismissListener onDismissListenerDeletSeed) {
        this.onDismissListener = onDismissListener;
        onDismissListener2=onDismissListenerDeletSeed;
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        //ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        //Fragment fragment;
        //final Bundle bundle = new Bundle();
        switch (which) {
            case 0:
                Store.setCurrentSeed(getActivity(),seed);
                WalletTransfersFragment.resetScroll();
                WalletAddressesFragment.resetScroll();
                WalletAddressCardAdapter.load(getActivity(),true);
                WalletTransfersCardAdapter.load(getActivity(),true);
                getDialog().dismiss();
                UiManager.openFragment(getActivity(), WalletTabFragment.class);
                break;
            case 1:
                Store.setCacheSeed(seed);
                UiManager.openFragmentBackStack(getActivity(), ChooseSeedEditFragment.class);
                break;
            case 2:
                if(Store.getSeedList().size()>1) {
                    DeleteSeedDialog deletedDialog = new DeleteSeedDialog();
                    deletedDialog.setSeed(seed);
                    deletedDialog.setOnDismissListener(onDismissListener2);
                    getDialog().dismiss();
                    deletedDialog.show(getActivity().getFragmentManager(), null);
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.drawer_layout), R.string.min_seed_req, Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }
}