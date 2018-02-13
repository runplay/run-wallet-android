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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import run.wallet.R;
import run.wallet.common.Sf;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.TorHelper;
import run.wallet.iota.model.QRCode;
import run.wallet.iota.model.Store;

public class TorFragment extends Fragment {

    @BindView(R.id.help_toolbar)
    Toolbar toolbar;

    @BindView(R.id.tor_install_show)
    CardView showInstall;
    @BindView(R.id.tor_enabled_show)
    LinearLayout showEnabled;

    @BindView(R.id.google_play_button)
    AppCompatButton googlePlay;

    @BindView(R.id.tor_switch_onoff)
    Switch switchOnOff;;
    @BindView(R.id.tor_switch_force)
    Switch switchForce;
    @BindView(R.id.tor_switch_nav)
    Switch switchNav;

    @BindView(R.id.tor_host)
    EditText textHost;
    @BindView(R.id.tor_port)
    EditText textPort;
    @BindView(R.id.tor_user)
    EditText textUser;
    @BindView(R.id.tor_password)
    EditText textPassword;

    private Unbinder unbinder;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tor, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(false);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        switchForce.setChecked(TorHelper.isForce());
        switchNav.setChecked(TorHelper.isTorNav());
        switchOnOff.setChecked(TorHelper.isEnabled());
        textHost.setText(TorHelper.getHost());
        textPassword.setText(TorHelper.getPassword());
        textPort.setText(TorHelper.getPort()+"");
        textUser.setText(TorHelper.getUser());
    }
    @Override
    public void onPause() {
        super.onPause();
        TorHelper.save();
    }
    @Override
    public void onResume() {
        super.onResume();

        if(TorHelper.isTorInstalled(getActivity())) {
            showEnabled.setVisibility(View.VISIBLE);
            showInstall.setVisibility(View.GONE);
            if(TorHelper.isForce()) {
                switchOnOff.setChecked(true);
                switchOnOff.setText(getString(R.string.on));
                switchOnOff.setEnabled(false);
            } else {
                //switchOnOff.setEnabled(true);
                switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            TorHelper.torEnable();
                        } else {
                            TorHelper.torDisable();
                        }
                    }
                });
            }
            switchForce.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    TorHelper.setForce(isChecked);
                    if(isChecked) {
                        switchOnOff.setChecked(true);
                        switchOnOff.setText(getString(R.string.on));
                        //switchOnOff.setEnabled(false);
                        TorHelper.torEnable();
                    } else {
                        TorHelper.torDisable();
                    }
                    goRestart();
                }
            });
            switchNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    TorHelper.setTorNav(isChecked);

                }
            });

            textHost.addTextChangedListener(SettingsWatcher);
            textPort.addTextChangedListener(SettingsWatcher);
            textUser.addTextChangedListener(SettingsWatcher);
            textPassword.addTextChangedListener(SettingsWatcher);

        } else {
            showEnabled.setVisibility(View.GONE);
            showInstall.setVisibility(View.VISIBLE);
            switchOnOff.setEnabled(false);
            switchOnOff.setText(getString(R.string.off));
            googlePlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TorHelper.openGooglePlayTor();
                }
            });
        }
    }

    private void goRestart() {
        Snackbar.make(getView(), getString(R.string.tor_message_restart), Snackbar.LENGTH_LONG).show();
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                TorHelper.save();
                TorHelper.restartApp();
            }
        },2000);
    }

    private TextWatcher SettingsWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            TorHelper.setHost(textHost.getText().toString());
            TorHelper.setPort(Sf.toInt(textPort.getText().toString()));
            TorHelper.setUser(textUser.getText().toString());
            TorHelper.setPassword(textPassword.getText().toString());

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start,
        int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start,
        int before, int count) {

        }
    };
    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }


}
