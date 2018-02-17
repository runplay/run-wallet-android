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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import run.wallet.R;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.activity.SettingsActivity;

public class ColorFragment extends Fragment {

    @BindView(R.id.welcome_toolbar)
    Toolbar toolbar;

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
    @BindView(R.id.welcome_colors)
    ScrollView scrollColors;
    @BindView(R.id.choose_theme_purple)
    View btnPurple;


    private Unbinder unbinder;
    private SharedPreferences prefs;
    private int STAGE=0;

    private static int pos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_colors, container, false);
        unbinder = ButterKnife.bind(this, view);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }
    @Override
    public void onPause() {
        super.onPause();
        pos=scrollColors.getScrollY();
    }
    @Override
    public void onResume() {
        super.onResume();
        Store.setCurrentFragment(this.getClass());

        btnBrand.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_DEFAULT); restart();});
        btnBlue.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_BLUE); restart();});
        btnPink.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_PINK); restart();});
        btnDg.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_DG); restart();});
        btnGreen.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_GREEN); restart();});
        btnDn.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_DN); restart();});
        btnSnow.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_SNOW); restart();});
        btnRed.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_RED); restart();});
        btnBw.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_BW); restart();});
        btnPurple.setOnClickListener(v-> {AppTheme.setTheme(getActivity(),AppTheme.THEME_PURPLE); restart();});
        scrollColors.setScrollY(pos);

    }
    public void restart() {
        Intent intent = getActivity().getIntent();
        getActivity().overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();
        getActivity().overridePendingTransition(0, 0);
        startActivity(intent);
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
