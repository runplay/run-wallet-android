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

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import run.wallet.R;
import run.wallet.iota.api.TaskManager;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.MsgStore;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.dialog.ChangeNoDescDialog;
import run.wallet.iota.ui.dialog.ShowNoDescDialog;

public class SettingsPasswordFragment extends PreferenceFragment {


    private static final String PREFERENCE_CHANGE_PASSWORD = "preference_change_password";
    private static final String PREFERENCE_WIPE_WALLETS = "preference_wipe_out";
    private static final String PREFERENCE_SHOW_MSGSEED = "preference_show_msgseed";
    private static final String PREFERENCE_TOR = "preference_show_tor";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_password_protection);
        checkPreferencesDependencies();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(android.R.color.white));

        return view;
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case PREFERENCE_SHOW_MSGSEED:
                if(MsgStore.getSeed()!=null) {
                    ShowNoDescDialog msgSeedDialog = new ShowNoDescDialog();
                    msgSeedDialog.setSeed(MsgStore.getSeed());
                    msgSeedDialog.show(getActivity().getFragmentManager(), null);
                } else {

                    Snackbar.make(getView(),R.string.notification_no_messaging_setup,Snackbar.LENGTH_LONG).show();
                }
                break;
            case PREFERENCE_CHANGE_PASSWORD:
                if(Store.isLoggedIn()) {
                    ChangeNoDescDialog changeSeedPasswordDialog = new ChangeNoDescDialog();
                    changeSeedPasswordDialog.show(getActivity().getFragmentManager(), null);
                } else {
                    Snackbar.make(getView(),R.string.settings_change_password_snackbar,Snackbar.LENGTH_LONG).show();
                }
                break;
            case PREFERENCE_WIPE_WALLETS:
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.message_confirm_logout)
                        .setCancelable(false)
                        .setPositiveButton(R.string.buttons_ok, null)
                        .setNegativeButton(R.string.buttons_cancel, null)

                        .create();

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.buttons_ok), (dialog, which) -> {
                    //prefs.edit().remove(Constants.PREFERENCE_ENC_PASS).apply();
                    //IOTA.seed = null;
                    Store.wipeAllStoreSavedData(getActivity());
                    TaskManager.stopAndDestroyAllTasks(getActivity());

                    Store.logout();

                    getActivity().setResult(Constants.REQUEST_RESTART_KILL_APP);
                    getActivity().finish();

                });

                alertDialog.show();
                break;
            case PREFERENCE_TOR:
                getActivity().setResult(Constants.REQUEST_GO_TOR);
                getActivity().finish();

                break;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void checkPreferencesDependencies() {

    }
}