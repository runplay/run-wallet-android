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

package run.wallet.iota.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;

import run.wallet.R;
import run.wallet.common.B;
import run.wallet.iota.api.TaskManager;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.NotificationHelper;
import run.wallet.iota.helper.RootDetector;
import run.wallet.iota.helper.TorHelper;
import run.wallet.iota.model.QRCode;
import run.wallet.iota.model.Store;
import run.wallet.iota.security.SignatureCheck;
import run.wallet.iota.security.Validator;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.dialog.RootDetectedDialog;
import run.wallet.iota.ui.fragment.AboutFragment;
import run.wallet.iota.ui.fragment.ChooseSeedFragment;
import run.wallet.iota.ui.fragment.ColorFragment;
import run.wallet.iota.ui.fragment.GenerateQRCodeFragment;
import run.wallet.iota.ui.fragment.HelpFragment;
import run.wallet.iota.ui.fragment.NetworkNeighborsFragment;
import run.wallet.iota.ui.fragment.NetworkNodeInfoFragment;
import run.wallet.iota.ui.fragment.NetworkNodesAddFragment;
import run.wallet.iota.ui.fragment.NetworkNodesFragment;
import run.wallet.iota.ui.fragment.NetworkTabFragment;
import run.wallet.iota.ui.fragment.SnTrFragment;
import run.wallet.iota.ui.fragment.PasswordLoginFragment;
import run.wallet.iota.ui.fragment.QRScannerFragment;
import run.wallet.iota.ui.fragment.SeedLoginFragment;
import run.wallet.iota.ui.fragment.SettingsFragment;

import run.wallet.iota.ui.fragment.TorFragment;
import run.wallet.iota.ui.fragment.WalletAddressesFragment;
import run.wallet.iota.ui.fragment.WalletTabFragment;
import run.wallet.iota.ui.fragment.WalletTransfersFragment;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import run.wallet.iota.ui.fragment.WelcomeFragment;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
/*
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    */
    private static final String STATE_CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT_TAG";
    private static final String STATE_IS_LOGGED_IN_TAG = "IS_LOGGED_IN_TAG";
    private static final String SHORTCUT_ID_GENERATE_QR_CODE = "generateQrCode";
    private static final String SHORTCUT_ID_SEND_TRANSFER = "sendTransfer";
    private static final int FRAGMENT_CONTAINER_ID = R.id.container;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;


    private SharedPreferences prefs;
    private InputMethodManager inputManager;
    private String currentFragmentTag = null;
    private boolean killFragments = false;
    private OnBackPressedClickListener onBackPressedClickListener;
    //private StartupDialog startDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppTheme.init(this);

        setTheme(AppTheme.getTheme());
        AppTheme.setNavColors(this);

        getWindow().setBackgroundDrawable(new ColorDrawable(B.getColor(this,AppTheme.getSecondary())));
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Store.init(this,false);
        TorHelper.init(this,null);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        if (!AppService.isAppServiceRunning(this)) {
            Intent service = new Intent(this, AppService.class);
            this.startService(service);
        }
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getHeaderView(0).setBackground(B.getDrawable(this,AppTheme.getNavDrawableId()));
        //navigationView.setBackground(B.getDrawable(this,AppTheme.getNavDrawableId()));

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (savedInstanceState == null || Store.getCurrentSeed() == null) {
            navigationView.getMenu().performIdentifierAction(R.id.nav_wallet, 0);
        } else {
            Class<? extends Fragment> currentFrag = Store.getCurrentFragment();
            if(currentFrag!=null) {
                UiManager.openFragment(this,currentFrag);
            }
        }
        if (!prefs.getBoolean(Constants.PREFERENCE_RUN_WITH_ROOT, false)) {
            if (RootDetector.isDeviceRooted()) {
                RootDetectedDialog dialog = new RootDetectedDialog();
                dialog.show(this.getFragmentManager(), null);
            }
        }


        drawer.addDrawerListener(drawerListener);
        AppService.getNodeInfo(this);
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        if (toolbar != null) {
            super.setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.SimpleDrawerListener() {
        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            inputManager.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
        }
    };


    @Override
    public void onBackPressed() {
        showLogoutNavigationItem();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (onBackPressedClickListener != null)
                onBackPressedClickListener.onBackPressedClickListener();
            else if (getFragmentManager().getBackStackEntryCount() > 0)
                getFragmentManager().popBackStack();
            else
                super.onBackPressed();
        }
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        String errorMessage = null;
        switch (error.getErrorType()) {
            case REMOTE_NODE_ERROR:
                AppService.getNodeInfo(this);
                break;
            case NETWORK_ERROR:
                errorMessage = getString(R.string.messages_network_error);
                break;
            case ACCESS_ERROR:
                if(error.getMessage()!=null && !error.getMessage().contains("getNeigh")) {
                    errorMessage = getString(R.string.messages_network_access_error);
                }
                break;
            case INVALID_HASH_ERROR:
                errorMessage = getString(R.string.messages_invalid_hash_error);
                break;
            case EXCHANGE_RATE_ERROR:
                errorMessage = getString(R.string.messages_exchange_rate_error);
                break;

        }

        if(errorMessage!=null) {
            Snackbar.make(findViewById(R.id.drawer_layout), errorMessage, Snackbar.LENGTH_LONG)
                    .setAction(null, null).show();
        }

    }

    private static final Class[] fragmentsToKill = {
            AboutFragment.class,
            GenerateQRCodeFragment.class,
            NetworkNeighborsFragment.class,
            NetworkNodeInfoFragment.class,
            NetworkNodesAddFragment.class,
            NetworkNodesFragment.class,
            PasswordLoginFragment.class,
            QRScannerFragment.class,
            SeedLoginFragment.class,
            SettingsFragment.class,

            GenerateQRCodeFragment.class,
            SnTrFragment.class,
            WalletAddressesFragment.class,
            WalletTabFragment.class,
            WalletTransfersFragment.class,
            ChooseSeedFragment.class
    };
    public static Class[] getKillFragments() {
        if(Validator.isValidCaller()) {
            return fragmentsToKill;
        }
        return null;
    }
    private void showFragment(Fragment fragment, boolean addToBackStack, boolean killFragments) {

        if (fragment == null) {
            // Do nothing
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag);

        if (currentFragment != null && currentFragment.getClass().getCanonicalName().equals(fragment.getClass().getCanonicalName())) {
            // Fragment already shown, do nothing
            return;
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        if (killFragments) {

            for (Class fragmentClass : fragmentsToKill) {
                String tag = fragmentClass.getCanonicalName();
                if (tag.equals(fragment.getClass().getCanonicalName())) {
                    continue;
                }
                Fragment fragmentToKill = fragmentManager.findFragmentByTag(tag);
                if (fragmentToKill != null) {
                    fragmentTransaction.remove(fragmentToKill);
                }
            }
        }

        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }

        String tag = fragment.getClass().getCanonicalName();
        Fragment cachedFragment = fragmentManager.findFragmentByTag(tag);
        if (cachedFragment != null) {
            // Cached fragment available
            fragmentTransaction.show(cachedFragment);
        } else {
            fragmentTransaction.replace(FRAGMENT_CONTAINER_ID, fragment, tag);
        }
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

        if (fragment instanceof OnBackPressedClickListener) {
            onBackPressedClickListener = (OnBackPressedClickListener) fragment;
        } else
            onBackPressedClickListener = null;


        currentFragmentTag = tag;
    }

    private void showFragment(Fragment fragment, boolean addToBackStack) {
        showFragment(fragment, addToBackStack, false);
    }

    public void showFragment(Fragment fragment) {
        showFragment(fragment, false);
    }

    private void showLogoutNavigationItem() {
        navigationView.getMenu().findItem(R.id.nav_tor).setVisible(TorHelper.isTorNav());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        getFragmentManager().popBackStack();

        switch (item.getItemId()) {
            case R.id.nav_wallet:

                showLogoutNavigationItem();
                if(Store.getCurrentSeed()!=null) {
                    if(!Store.isLoggedIn()) {
                        UiManager.checkPin(this);
                        killFragments = true;
                    } else {
                        killFragments = true;
                        fragment = new WalletTabFragment();
                    }
                } else {
                    killFragments = true;
                    fragment = new WelcomeFragment();
                }
                break;

            case R.id.nav_choose_wallet:
                fragment = new ChooseSeedFragment();
                killFragments = true;

                break;

            case R.id.nav_node_info:
                fragment = new NetworkTabFragment();
                break;
            case R.id.nav_help:
                UiManager.openFragmentBackStack(this,HelpFragment.class);
                break;
            case R.id.nav_tor:
                UiManager.openFragmentBackStack(this,TorFragment.class);
                break;
/*
            case R.id.nav_messaging:
                if (Store.isLoggedIn()) {
                    fragment = new MsgHomeFragment();
                    killFragments = true;
                }
                break;
                */
            case R.id.nav_settings:
                //UiManager.openFragmentBackStack(this,SettingsFragment.class);
                //fragment = new SettingsFragment();
                Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(settings,0);
                break;

        }

        if (fragment != null) {
            showFragment(fragment, false, killFragments);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Handler updateShortcuts;
    private UpdateShortRunnable updateShort;
    private void updateDynamicShortcuts() {
/*
        if (updateShortcuts != null)
            updateShortcuts.removeCallbacks(updateShort);
        updateShort= new UpdateShortRunnable(this);
        updateShortcuts=new Handler();
        updateShortcuts.postDelayed(updateShort,300);
    */
    }

    private class UpdateShortRunnable implements  Runnable {
        private AppCompatActivity activity;
        public UpdateShortRunnable(AppCompatActivity activity) {
            this.activity=activity;
        }

        @Override
        public void run() {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {

                Intent intentGenerateQrCode = new Intent(activity, MainActivity.class);
                intentGenerateQrCode.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                intentGenerateQrCode.setAction(Constants.ACTION_GENERATE_QR_CODE);

                ShortcutInfo shortcutGenerateQrCode = new ShortcutInfo.Builder(activity, SHORTCUT_ID_GENERATE_QR_CODE)
                        .setShortLabel(getString(R.string.shortcut_generate_qr_code))
                        .setLongLabel(getString(R.string.shortcut_generate_qr_code))
                        .setIcon(Icon.createWithResource(activity, R.drawable.ic_shortcut_qr))
                        .setIntent(intentGenerateQrCode)
                        .build();

                Intent intentTransferIotas = new Intent(activity, MainActivity.class);
                intentTransferIotas.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                intentTransferIotas.setAction(Constants.ACTION_SEND_TRANSFER);

                ShortcutInfo shortcutTransferIotas = new ShortcutInfo.Builder(activity, SHORTCUT_ID_SEND_TRANSFER)
                        .setShortLabel(getString(R.string.shortcut_send_transfer))
                        .setLongLabel(getString(R.string.shortcut_send_transfer))
                        .setIcon(Icon.createWithResource(activity, R.drawable.ic_shortcut_transaction))
                        .setIntent(intentTransferIotas)
                        .build();

                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

                if (shortcutManager != null) {
                    if (Store.getCurrentSeed() != null) {
                        shortcutManager.setDynamicShortcuts(Arrays.asList(shortcutGenerateQrCode, shortcutTransferIotas));
                        shortcutManager.enableShortcuts(Arrays.asList(SHORTCUT_ID_GENERATE_QR_CODE, SHORTCUT_ID_SEND_TRANSFER));
                    } else {
                        shortcutManager.disableShortcuts(Arrays.asList(SHORTCUT_ID_GENERATE_QR_CODE, SHORTCUT_ID_SEND_TRANSFER));
                        shortcutManager.removeAllDynamicShortcuts();
                    }
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA)) {
                        Snackbar.make(findViewById(R.id.drawer_layout), R.string.messages_permission_camera,
                                Snackbar.LENGTH_LONG)
                                .setAction(R.string.buttons_ok, view -> {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivityForResult(intent, Constants.REQUEST_CAMERA_PERMISSION);
                                })
                                .show();
                    }
                }
        }
    }

    private String hasPass=null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CODE_LOGIN:
                inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                navigationView.getMenu().performIdentifierAction(R.id.nav_wallet, 0);
                break;
        }
        switch (resultCode) {
            case Constants.REQUEST_CODE_LOGIN:
                inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                navigationView.getMenu().performIdentifierAction(R.id.nav_wallet, 0);
                break;

            case Constants.REQUEST_RESTART_KILL_APP:
                FragmentManager fragman=getFragmentManager();
                FragmentTransaction fragmentTransaction = fragman.beginTransaction();
                for (Class fragmentClass : fragmentsToKill) {
                    String tag = fragmentClass.getCanonicalName();
                    Fragment fragmentToKill = fragman.findFragmentByTag(tag);
                    if (fragmentToKill != null) {
                        fragmentTransaction.remove(fragmentToKill);
                    }

                }
                TaskManager.stopAndDestroyAllTasks(this);
                Store.wipeAllStoreSavedData(this);
                fragmentTransaction.commit();
                WalletAddressCardAdapter.clear();
                WalletTransfersCardAdapter.clear();
                inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                UiManager.openFragment(this,WelcomeFragment.class);
                break;
            case Constants.REQUEST_GO_TOR:
                UiManager.openFragmentBackStack(this, TorFragment.class);
                break;
            case Constants.REQUEST_GO_COLORS:
                UiManager.openFragmentBackStack(this, ColorFragment.class);
                break;
        }
        if (data != null) {
            if(resultCode==Constants.DONATE_NOW) {
                Bundle bundle = data.getExtras();
                if(bundle!=null) {
                    Fragment fragment = new SnTrFragment();
                    fragment.setArguments(bundle);
                    showFragment(fragment, true);
                }

            } else if (Intent.ACTION_VIEW.equals(data.getAction())) {
                QRCode qrCode = new QRCode();
                Uri uri = data.getData();
                if(uri!=null && uri.getQueryParameter("address:")!=null && !uri.getQueryParameter("address:").isEmpty()) {
                    qrCode.setAddress(uri.getQueryParameter("address:"));
                    qrCode.setAmount(uri.getQueryParameter("amount:"));
                    qrCode.setMessage(uri.getQueryParameter("message:"));

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.QRCODE, qrCode);

                    Fragment fragment = new SnTrFragment();
                    fragment.setArguments(bundle);
                    showFragment(fragment, true);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            EventBus.getDefault().register(this);
        } catch (EventBusException e) {}
        showLogoutNavigationItem();
        updateDynamicShortcuts();
        AppService.setIsAppStarted(this, true);
        NotificationHelper.clearAll(this);



/*
// code items for dev mode only if needed
        if(!PermissionRequestHelper.hasReadExternalStoragePermission(this)) {
            Log.e("FPEM","! NOT HAS FILE PERM");
            Handler hand=new Handler();
            hand.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PermissionRequestHelper.checkPermissionReadStorage(MainActivity.this);

                }
            },1000);
        } else {
            Log.e("FPEM","HAS FILE PERM");
        }

        // this call is for future security checking, not ready yet
        SignatureCheck.validateAppSignature(this);
        */
    }

    @Override
    public void onPause() {
        super.onPause();
        updateDynamicShortcuts();
        EventBus.getDefault().unregister(this);
        AppService.setIsAppStarted(this,false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        drawer.removeDrawerListener(drawerListener);
        AlertDialog ad = UiManager.getPasswordDialog();
        if(ad!=null) {
            ad.dismiss();
        }
        super.onDestroy();
    }

    public interface OnBackPressedClickListener {
        void onBackPressedClickListener();
    }
}
