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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import run.wallet.R;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.QRCode;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.activity.MainActivity;
import run.wallet.iota.ui.activity.SettingsActivity;

public class WelcomeFragment extends Fragment {

    @BindView(R.id.welcome_toolbar)
    Toolbar toolbar;
    @BindView(R.id.welcome_screen1)
    View welcome1;
    @BindView(R.id.welcome_screen2)
    View welcome2;
    @BindView(R.id.welcome_next)
    Button btnNext;
    @BindView(R.id.welcome_aw_btn)
    Button btnGoSeed;
    @BindView(R.id.welcome_btn_tor)
    Button btnTor;
    @BindView(R.id.welcome_btn_settings)
    Button btnSettings;

    @BindView(R.id.choose_theme_brand)
    View btnBrand;
    @BindView(R.id.choose_theme_blue)
    View btnBlue;
    @BindView(R.id.choose_theme_pink)
    View btnPink;
    @BindView(R.id.choose_theme_dg)
    View btnDg;
    @BindView(R.id.choose_theme_green)
    View btnGreen;
    @BindView(R.id.choose_theme_dn)
    View btnDn;
    @BindView(R.id.choose_theme_snow)
    View btnSnow;
    @BindView(R.id.choose_theme_red)
    View btnRed;
    @BindView(R.id.choose_theme_bw)
    View btnBw;
    @BindView(R.id.choose_theme_purple)
    View btnPurple;
    @BindView(R.id.choose_theme_lgbota)
    View btnLgbt;

    @BindView(R.id.welcome_colors)
    HorizontalScrollView scrollColors;

    private Unbinder unbinder;
    private SharedPreferences prefs;
    private int STAGE=0;
    private static int pos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        unbinder = ButterKnife.bind(this, view);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        pos=scrollColors.getScrollX();
    }
    @Override
    public void onResume() {
        super.onResume();
        Store.setCurrentFragment(this.getClass());

        STAGE = prefs.getInt("pref_welcome_stage", 0);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                STAGE=1;
                showWelcome();
            }
        });
        btnGoSeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiManager.openFragment(getActivity(),ChooseSeedFragment.class);
            }
        });
        btnTor.setOnClickListener(v -> UiManager.openFragmentBackStack(getActivity(),TorFragment.class));
        btnSettings.setOnClickListener(v -> {                Intent settings = new Intent(getActivity(), SettingsActivity.class);
            startActivityForResult(settings,0);});
        showWelcome();

        btnBrand.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_DEFAULT); getActivity().recreate();});
        btnBlue.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_BLUE); getActivity().recreate();});
        btnPink.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_PINK); getActivity().recreate();});
        btnDg.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_DG); getActivity().recreate();});
        btnGreen.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_GREEN); getActivity().recreate();});
        btnDn.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_DN); getActivity().recreate();});
        btnSnow.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_SNOW); getActivity().recreate();});
        btnRed.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_RED); getActivity().recreate();});
        btnPurple.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_PURPLE); getActivity().recreate();});
        btnBw.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_BW); getActivity().recreate();});
        btnLgbt.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_LGBOTA); getActivity().recreate();});
        scrollColors.setScrollX(pos);
    }
    private void showWelcome() {
        welcome1.setVisibility(View.GONE);
        welcome2.setVisibility(View.GONE);
        switch (STAGE) {
            case 0:
                welcome1.setVisibility(View.VISIBLE);
                break;
            default:
                if(prefs.getInt("pref_welcome_stage", 0)==0) {
                    prefs.edit().putInt("pref_welcome_stage", 1).commit();
                }
                welcome2.setVisibility(View.VISIBLE);
                break;
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

}
