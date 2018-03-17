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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import run.wallet.R;
import run.wallet.iota.helper.AESCrypt;
import run.wallet.iota.helper.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import run.wallet.iota.model.MsgStore;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;

public class ShowNoDescDialog extends DialogFragment {

    @BindView(R.id.password_input_layout)
    TextInputLayout textInputLayoutPassword;
    @BindView(R.id.password)
    TextInputEditText textInputEditTextPassword;
    @BindView(R.id.decrypted_seed)
    TextView textViewSeed;

    private Seeds.Seed seed;
    private boolean isMessaging=false;

    public void setIsMessaging(boolean value) {
        this.isMessaging=value;
    }

    public ShowNoDescDialog() {
    }
    public void setSeed(Seeds.Seed seed) {
        this.seed=seed;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_show_seed, null, false);
        ButterKnife.bind(this, dialogView);
        int title=R.string.settings_show_seed_title;
        if(isMessaging)
            title=R.string.settings_show_msgseed_title;
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(title)
                .setPositiveButton(R.string.buttons_show, null)
                .setNegativeButton(R.string.buttons_cancel, null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            final Button bPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            final Button bNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            bPositive.setOnClickListener(view -> {
                showPassword();
                if (!textViewSeed.getText().toString().isEmpty()) {

                    textInputLayoutPassword.setVisibility(View.GONE);

                    bNegative.setText(R.string.buttons_ok);
                    bPositive.setEnabled(false);
                }
            });
        });

        alertDialog.show();
        return alertDialog;
    }

    @OnEditorAction(R.id.password)
    public boolean onPasswordEditorAction(int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            showPassword();
        }
        return true;
    }

    private void showPassword() {
        String password = textInputEditTextPassword.getText().toString();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //reset errors
        textInputLayoutPassword.setError(null);

        try {
            AESCrypt aes = new AESCrypt(password);
            String encSeed = prefs.getString(Constants.PREFERENCE_ENC_PASS, "");
            //Log.e("PASS-TEST","P: "+password+" -- "+encSeed+" -- "+aes.decrypt(encSeed)+" -- "+aes.encrypt(password));
            if(Store.verifyPassword(aes.decrypt(encSeed))) {
                textViewSeed.setText(String.valueOf(Store.getSeedRaw(getActivity(),seed)));
            };


        } catch (Exception e) {
            //Log.e("DIALOG",""+e.getMessage());
            textInputLayoutPassword.setError(getActivity().getString(R.string.messages_invalid_password));
            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            textInputEditTextPassword.startAnimation(shake);
            e.getStackTrace();
        }
    }

}
