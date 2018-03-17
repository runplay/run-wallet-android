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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import run.wallet.R;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.fragment.WalletTabFragment;

public class WipeSeedDialog extends DialogFragment {

    private Seeds.Seed seed;

    public WipeSeedDialog() {
    }
    public void setSeed(Seeds.Seed seed) {
        this.seed=seed;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        int title=R.string.title_reload_seed;
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.messages_wipe_seed)
                .setTitle(title)
                .setPositiveButton(R.string.buttons_ok, null)
                .setNegativeButton(R.string.buttons_cancel, null)
                .create();

        alertDialog.setOnShowListener(dialog -> {

            final Button bPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            bPositive.setOnClickListener(view -> {
                Store.wipeSeed(getActivity(), seed);
                WalletAddressCardAdapter.clear();
                WalletTransfersCardAdapter.clear();
                Store.setCurrentSeed(getActivity(),seed);

                getDialog().dismiss();
                UiManager.openFragment(getActivity(), WalletTabFragment.class);
            });

        });


        alertDialog.show();
        return alertDialog;
    }


}
