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
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import run.wallet.R;
import run.wallet.iota.helper.AESCrypt;
import run.wallet.iota.helper.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.UiManager;

public class NoDescDialog extends DialogFragment {

    @BindView(R.id.password_input_layout)
    TextInputLayout textInputLayoutPassword;
    @BindView(R.id.password_confirm_input_layout)
    TextInputLayout textInputLayoutPasswordConfirm;
    @BindView(R.id.password)
    TextInputEditText textInputEditTextPassword;
    @BindView(R.id.password_confirm)
    TextInputEditText textInputEditTextPasswordConfirm;
    private String seed;
    private boolean isgen=false;

    public NoDescDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_encrypt_seed_password, null, false);
        ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        seed = bundle.getString("seed");
        isgen=bundle.getBoolean("isgen");

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.title_enter_password)
                //.setMessage(R.string.message_enter_password)
                .setCancelable(false)
                .setPositiveButton(R.string.buttons_save, null)
                .setNegativeButton(R.string.buttons_cancel, null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> encryptSeed());
        });

        alertDialog.show();
        UiManager.setKeyboard(getActivity(),view.findViewById(R.id.password),true);
        //InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        //inputManager.showSoftInput(view.findViewById(R.id.password), 0);
        //inputManager
        return alertDialog;

    }

    @OnEditorAction(R.id.password_confirm)
    public boolean onPasswordConfirmEditorAction(int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            encryptSeed();
        }
        return true;
    }

    private void encryptSeed() {
        String password = textInputEditTextPassword.getText().toString();
        String passwordConfirm = textInputEditTextPasswordConfirm.getText().toString();

        //reset errors
        textInputLayoutPassword.setError(null);
        textInputLayoutPasswordConfirm.setError(null);

        if (password.isEmpty())
            textInputLayoutPassword.setError(getActivity().getString(R.string.messages_empty_password));
        else if (!password.equals(passwordConfirm))
            textInputLayoutPasswordConfirm.setError(getActivity().getString(R.string.messages_match_password));
        else if (passwordConfirm.length()<4 || passwordConfirm.length()>20)
            textInputLayoutPasswordConfirm.setError(getActivity().getString(R.string.messages_minmax_password));
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            try {
                Store.addSeed(getActivity(),seed.toCharArray(),getString(R.string.wallet)+"-0",true,isgen);

                AESCrypt aes = new AESCrypt(passwordConfirm);
                String rawpass=Utils.getPasswordId();
                prefs.edit().putString(Constants.PREFERENCE_ENC_PASS, aes.encrypt(rawpass)).apply();

                prefs.edit().putInt(Constants.PREFERENCE_PASS_LENGTH,passwordConfirm.length()).commit();
                boolean islog=Store.login(rawpass);
                //Log.e("ENC-SEEd","is loggedin: "+passwordConfirm.length());
                //prefs.edit().putString(Constants.PREFERENCE_ENC_PASS, aes.encrypt(seed)).apply();
                //IOTA.seed = seed.toCharArray();

                getDialog().dismiss();
                getActivity().onBackPressed();
                //Intent intent = new Intent(getActivity().getIntent());
                //intent.getExtras().putString("haspass",passwordConfirm);
                //getActivity().startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN); //REQUEST_CODE_LOGIN
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

}
