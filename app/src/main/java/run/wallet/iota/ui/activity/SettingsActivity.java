/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package run.wallet.iota.ui.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import run.wallet.R;
import run.wallet.common.B;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppTheme.init(this);
        AppTheme.setNavColors(this);
        setContentView(R.layout.activity_settings);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.content, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(B.getColor(this,AppTheme.getPrimary())));
        }
        AppTheme.setNavColors(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        AppService.setIsSettingsAppStarted(this,true);
    }
    @Override
    public void onPause() {
        super.onPause();
        AppService.setIsSettingsAppStarted(this,false);
    }
    @Override
    public void onBackPressed() {
        finishUp();
        this.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }
    private void finishUp() {
        if(Store.getCurrentSeed()!=null) {
            Store.init(this, true);
            AppService.refreshEvent();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home) {
            finishUp();
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
