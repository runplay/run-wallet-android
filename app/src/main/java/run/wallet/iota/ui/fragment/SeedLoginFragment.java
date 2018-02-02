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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.TextView;

import run.wallet.R;
import run.wallet.iota.helper.SeedValidator;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.dialog.DialogHelper;
import run.wallet.iota.ui.dialog.NoDescDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import jota.utils.SeedRandomGenerator;

public class SeedLoginFragment extends Fragment {

    private static final String SEED = "seed";
    @BindView(R.id.login_toolbar)
    Toolbar loginToolbar;
    @BindView(R.id.seed_login_seed_text_input_layout)
    TextInputLayout seedEditTextLayout;
    @BindView(R.id.seed_login_seed_input)
    TextInputEditText seedEditText;
    @BindView(R.id.seed_login_security)
    TextView securityText;
    @BindView(R.id.seed_login_security_value)
    TextView securityValue;


    private String generatedSeed;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seed_login, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(loginToolbar);
        securityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showAddressSecurityDialog(getActivity(), new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        refreshSecurity();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @OnClick(R.id.seed_login_button)
    public void onSeedLoginClick() {
        loginDialog();
    }

    @OnClick(R.id.seed_login_generate_seed)
    public void onSeedLoginGenerateSeedClick() {
        generatedSeed = SeedRandomGenerator.generateNewSeed();
        seedEditText.setText(generatedSeed);

        //Bundle bundle = new Bundle();
        //bundle.putString("generatedSeed", generatedSeed);
        //CopySeedDialog dialog = new CopySeedDialog();
        //dialog.setArguments(bundle);
        //dialog.show(getFragmentManager(), null);
    }
    private void refreshSecurity() {
        securityValue.setText(Store.getAddressSecurityDefault()+"");
    }

    @OnEditorAction(R.id.seed_login_seed_input)
    public boolean onSeedLoginSeedInputEditorAction(int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            loginDialog();
        }
        return true;
    }

    private void loginDialog() {

        //TODO Only accept seeds that are longer than SeedValidator.SEED_LENGTH_MIN ?

        String seed = seedEditText.getText().toString();

        if (seed.isEmpty()) {
            seedEditTextLayout.setError(getString(R.string.messages_empty_seed));
            if (seedEditTextLayout.getError() != null)
                return;
        }

        if (SeedValidator.isSeedValid(getActivity(), seed) == null) {
            login();

        } else {
            AlertDialog loginDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(SeedValidator.isSeedValid(getActivity(), seed))
                    .setCancelable(false)
                    .setPositiveButton(R.string.buttons_ok, null)
                    .setNegativeButton(R.string.buttons_cancel, null)
                    .create();

            loginDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.buttons_login), (dialog, which) -> login());

            loginDialog.show();
        }
    }

    private void login() {
        String seed = SeedValidator.getSeed(seedEditText.getText().toString());
        UiManager.setKeyboard(getActivity(),seedEditText,false);

        Bundle bundle = new Bundle();
        bundle.putString("seed", seed);
        if(generatedSeed!=null && generatedSeed.equals(seed)) {
            bundle.putBoolean("isgen", true);
        } else {
            bundle.putBoolean("isgen", false);
        }
        NoDescDialog encryptSeedDialog = new NoDescDialog();
        encryptSeedDialog.setArguments(bundle);
        encryptSeedDialog.show(getActivity().getFragmentManager(), null);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEED, seedEditText.getText().toString());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            seedEditText.setText(savedInstanceState.getString(SEED));
        }
    }
}
