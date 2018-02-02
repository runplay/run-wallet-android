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
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import run.wallet.R;
import run.wallet.common.B;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final String PREFERENCE_SCREEN_PASSWORD = "preference_screen_password_protection";
    private static final String PREFERENCE_SCREEN_MISC = "preference_screen_misc";
    private static final String PREFERENCE_SCREEN_ABOUT = "preference_screen_about";
    private static final String PREFERENCE_SCREEN_DISPLAY = "preference_display_settings";
    private static final String[] ALL_PREFERENCES = {PREFERENCE_SCREEN_DISPLAY,PREFERENCE_SCREEN_ABOUT,
            PREFERENCE_SCREEN_PASSWORD, PREFERENCE_SCREEN_MISC};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        for (String preference : ALL_PREFERENCES) {
            findPreference(preference)
                    .setOnPreferenceClickListener(this);
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Fragment fragment = null;
        switch (preference.getKey()) {
            case PREFERENCE_SCREEN_PASSWORD:
                fragment = new SettingsPasswordFragment();
                break;
            case PREFERENCE_SCREEN_MISC:
                fragment = new SettingsMiscFragment();
                break;
            case PREFERENCE_SCREEN_ABOUT:
                fragment = new AboutFragment();
                break;
            case PREFERENCE_SCREEN_DISPLAY:
                fragment = new SettingsDisplayFragment();
                break;

        }
        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out,
                            R.animator.fade_in, R.animator.fade_out)
                    .add(R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;
    }
}