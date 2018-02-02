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

package run.wallet.iota.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;

import run.wallet.R;
import run.wallet.iota.helper.AESCrypt;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.dialog.ForgotPasswordDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;

public class PasswordLoginFragment extends Fragment {

    @BindView(R.id.password_login_toolbar)
    Toolbar passwordLoginToolbar;
    @BindView(R.id.password_forgot_text_input_layout)
    TextInputLayout textInputLayoutPassword;
    @BindView(R.id.password_login)
    TextInputEditText textInputEditTextPassword;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_login, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(passwordLoginToolbar);

    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    private void login(boolean warn) {
        String password = textInputEditTextPassword.getText().toString();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
            AESCrypt aes = new AESCrypt(password);
            String encSeed = prefs.getString(Constants.DUMMY_PASSWORD, "");
            String dec = aes.decrypt(encSeed);
            if(!Store.login(dec))
                throw new Exception();
             Intent intent = new Intent(getActivity().getIntent());
            getActivity().startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN);
        } catch (Exception e) {
            if(warn) {
                textInputLayoutPassword.setError(getString(R.string.messages_invalid_password));
                Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                textInputEditTextPassword.startAnimation(shake);
            }
        }
    }

    @OnClick(R.id.password_login_button)
    public void onPasswordLoginButtonClick() {
        login(true);
    }

    @OnClick(R.id.password_forgot)
    public void onPasswordForgotClick() {
        ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog();
        forgotPasswordDialog.show(getActivity().getFragmentManager(), null);
    }

    @OnEditorAction(R.id.password_login)
    public boolean onEditorAction(int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            login(true);
        } else {
            SharedPreferences prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getActivity());
            int pl=prefs.getInt(Constants.PREFERENCE_PASS_LENGTH,0);
            if(pl>0 && textInputEditTextPassword.getText().toString().length()==pl) {
                login(false);
            }
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
