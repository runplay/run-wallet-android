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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import run.wallet.R;

public class KeyReuseDetectedDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public KeyReuseDetectedDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        String error = bundle.getString("error");
        String message = "";
        String title = "";

        if (error != null) {
            if (error.contains("Sending to a used address.")) {
                title = getString(R.string.title_spend_to_used_address);
                message = getResources().getString(R.string.message_spend_to_used_address);

            } else if (error.contains("Private key reuse detect!")) {
                title = getResources().getString(R.string.title_key_reuse_detect);
                message = getResources().getString(R.string.message_key_reuse_detect);
            }
        }

        return new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.buttons_ok, null)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case AlertDialog.BUTTON_NEGATIVE:
                getDialog().dismiss();
                break;
        }
    }

}
