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
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import run.wallet.R;
import run.wallet.iota.helper.AESCrypt;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;

public class DeleteSeedDialog extends DialogFragment {



    private DialogInterface.OnDismissListener onDismissListener;
    private Seeds.Seed seed;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;

    }
    public DeleteSeedDialog() {
    }
    public void setSeed(Seeds.Seed seed) {
        this.seed=seed;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_delete_seed, null, false);
        ButterKnife.bind(this, dialogView);
        int title=R.string.title_remove_seed;
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setMessage(R.string.messages_remove_seed)
                .setTitle(title)
                .setPositiveButton(R.string.buttons_ok, null)
                .setNegativeButton(R.string.buttons_cancel, null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            final Button bPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            final Button bNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            bPositive.setOnClickListener(view -> {
                Store.removeSeed(getActivity(), seed.id);
                WalletAddressCardAdapter.clear();
                WalletTransfersCardAdapter.clear();
                Store.setCurrentSeed(getActivity(),Store.getSeedList().get(0));
                onDismissListener.onDismiss(getDialog());
                getDialog().dismiss();
            });

        });


        alertDialog.show();
        return alertDialog;
    }


}
