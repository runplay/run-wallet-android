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
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;

import java.util.List;

import run.wallet.R;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.QRCode;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.fragment.AddressSecurityFragment;
import run.wallet.iota.ui.fragment.GenerateQRCodeFragment;
import run.wallet.iota.ui.fragment.WalletAddressesFragment;
import run.wallet.iota.ui.fragment.WalletTabFragment;
import run.wallet.iota.ui.fragment.WalletTransfersFragment;


public class SwitchWalletDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private CharSequence[] names;
    public SwitchWalletDialog() {
    }
    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            List<Seeds.Seed> seeds = Store.getSeedList();
            names = new CharSequence[seeds.size()];
            for(int i=0; i<seeds.size(); i++) {
                names[i]=seeds.get(i).name;
            }
            return new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.switch_wallet))

                    .setItems(names, this)
                    .create();
        } catch(Exception e) {}
        this.dismiss();
        return null;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if(names!=null && names.length>which) {
            List<Seeds.Seed> seeds = Store.getSeedList();
            Seeds.Seed useSeed=null;
            try {
                useSeed = seeds.get(which);
            } catch (Exception e){}
            if(useSeed!=null) {
                Store.setCurrentSeed(getActivity(), useSeed);
                WalletTransfersFragment.resetScroll();

                WalletAddressesFragment.resetScroll();
                WalletTransfersCardAdapter.setFilterAddress(null, null);
                WalletAddressCardAdapter.load(getActivity(), true);
                WalletTransfersCardAdapter.load(getActivity(), true);
                UiManager.openFragment(getActivity(), WalletTabFragment.class);
            }
        }
    }
}