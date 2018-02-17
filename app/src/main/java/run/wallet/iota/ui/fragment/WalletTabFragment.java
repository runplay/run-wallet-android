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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Currency;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetAccountDataResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.api.responses.SendTransferResponse;
import run.wallet.iota.api.responses.WebGetExchangeRatesResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.helper.Cal;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Utils;

import run.wallet.iota.model.Store;
import run.wallet.iota.model.Ticker;
import run.wallet.iota.model.Wallet;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletPagerAdapter;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jota.utils.IotaToText;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;

public class WalletTabFragment extends LoggedInFragment {

    private static final String BALANCE = "balance";
    private static final String ALTERNATE_BALANCE = "alternateBalance";
    private static final String WALLET_FAB_STATE = "walletFabState";
    @BindView(R.id.wallet_toolbar)
    Toolbar walletToolbar;
    @BindView(R.id.wallet_tab_viewpager)
    ViewPager viewPager;
    @BindView(R.id.wallet_tabs)
    TabLayout tabLayout;
    @BindView(R.id.toolbar_title_layout_balance)
    TextView balanceTextView;
    @BindView(R.id.toolbar_title_button)
    LinearLayout titleButton;

    @BindView(R.id.toolbar_title_layout_alternate_balance)
    TextView alternateBalanceTextView;
    @BindView(R.id.fab_wallet)
    FloatingActionButton fabWallet;

    @BindView(R.id.toolbar_title_layout_alternate_currency)
    TextView altCurrency;

    @BindView(R.id.toolbar_title_layout_pending_out)
    TextView pendingOut;
    @BindView(R.id.toolbar_title_layout_pending_in)
    TextView pendingIn;
    @BindView(R.id.toolbar_title_layout_pending)
    LinearLayout pending;
    @BindView(R.id.toolbar_title_layout_balance_third)
    TextView thirdDecimal;
    @BindView(R.id.toolbar_title_layout_balance_unit)
    TextView balanceUnit;
    @BindView(R.id.balance_flipper)
    LinearLayout balanceFlipper;
    @BindView(R.id.tap_balance)
    TextView tapBalance;

    @BindView(R.id.tap_balance_type)
    ImageView balanceType;

    private int tapCount=0;
    private static final int TAP_MAX=3;
    private boolean isConnected = false;
    private WalletPagerAdapter adapter;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //alternateValueManager = new AlternateValueManager(getActivity());
        adapter = new WalletPagerAdapter(getActivity(), getChildFragmentManager());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_tab, container, false);
        unbinder = ButterKnife.bind(this, view);
        titleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiManager.openFragmentBackStack(getActivity(),ChooseSeedFragment.class);
            }
        });
        AppService.updateExchangeRates(getActivity());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(walletToolbar);
        WalletTransfersCardAdapter.setViewPager(viewPager);
        viewPager.setAdapter(adapter);
        goCreate();

    }
    private void goCreate() {


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment currentFragment = adapter.getItem(tab.getPosition());
                if(tab.getPosition()==1 && viewPager.getCurrentItem()==0) {
                    WalletTransfersCardAdapter.setFilterAddress(null, null);
                    WalletTransfersCardAdapter.load(getActivity(),true);
                }
                if (currentFragment != null && currentFragment instanceof DataRefreshListener) {
                    if(WalletTransfersCardAdapter.getFilterAddress()!=null) {
                        ((DataRefreshListener) currentFragment).refreshData();

                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Fragment currentFragment = adapter.getItem(tab.getPosition());
                if (currentFragment != null && currentFragment instanceof DataRefreshListener) {
                    WalletTransfersCardAdapter.setFilterAddress(null, null);
                    WalletTransfersCardAdapter.load(getActivity(),true);
                    ((DataRefreshListener) currentFragment).refreshData();
                    //AppService.refreshEvent();
                }
            }
        });
        viewPager.addOnPageChangeListener(onPageChangeListener);

        balanceFlipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Wallet cwallet=Store.getCurrentWallet();
                if(cwallet!=null && (cwallet.getBalancePendingIn()!=0 || cwallet.getBalancePendingOut()!=0)) {

                    Store.setBalanceDisplayType(getActivity(), Store.getBalanceDisplayType() + 1);
                    updateBalance();
                    int msg=R.string.snackbar_balance_live;
                    switch(Store.getBalanceDisplayType()) {
                        case 1:
                            msg=R.string.snackbar_balance_out;
                            break;
                        case 2:
                            msg=R.string.snackbar_balance_pending;
                            break;
                    }
                    Snackbar.make(getActivity().findViewById(R.id.drawer_layout), msg, Snackbar.LENGTH_SHORT).show();

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    tapCount= prefs.getInt(Constants.PREF_MSG_TAP_BALANCE,0);
                    if(tapCount<TAP_MAX) {
                        tapBalance.setVisibility(View.VISIBLE);
                        prefs.edit().putInt(Constants.PREF_MSG_TAP_BALANCE,++tapCount).commit();
                    } else {
                        tapBalance.setVisibility(View.GONE);

                        balanceType.setVisibility(View.VISIBLE);

                    }
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        viewPager.removeOnPageChangeListener(onPageChangeListener);
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @OnClick(R.id.fab_wallet)
    public void onFabWalletClick() {
        //if (isConnected && adapter != null) {
            Fragment currentFragment = adapter.getItem(viewPager.getCurrentItem());
            if (currentFragment != null && currentFragment instanceof WalletTabFragment.OnFabClickListener) {
                ((OnFabClickListener) currentFragment).onFabClick();
            } else {
                Log.e("BAD","MR BADDY BAD");
            }
        //}
    }
    private void updateBalance() {
        long walletBalanceIota = 0;
        long pendingIotaIn=0;
        long pendingIotaOut=0;
        Wallet wallet = Store.getCurrentWallet();
        if(wallet!=null) {

            walletBalanceIota=wallet.getBalanceDisplay();
            //Log.e("WTF","run.wallet bal: "+walletBalanceIota+" - pendingin: "+wallet.getBalancePendingIn()+" - penignout: "+wallet.getBalancePendingOut());
            pendingIotaIn=wallet.getBalancePendingIn();
            pendingIotaOut=wallet.getBalancePendingOut();

        }

        IotaToText.IotaDisplayData data = IotaToText.getIotaDisplayData(walletBalanceIota);

        String balanceText = data.value;
        tapBalance.setVisibility(View.GONE);
        if(tapCount<TAP_MAX) {
            if(wallet!=null && (wallet.getBalancePendingIn()!=0 || wallet.getBalancePendingOut()!=0)) {
                tapBalance.setVisibility(View.VISIBLE);
            }
        }
        thirdDecimal.setText(data.thirdDecimal);
        balanceUnit.setText(data.unit);
        if(pendingIotaOut==0) {
            pendingOut.setVisibility(View.GONE);
            pendingOut.setText(" ");
        } else {
            pendingOut.setText(IotaToText.convertRawIotaAmountToDisplayText(pendingIotaOut, true));
            pendingOut.setVisibility(View.VISIBLE);
        }
        if(pendingIotaIn==0) {
            pendingIn.setVisibility(View.GONE);
            pendingIn.setText(" ");
        } else {
            pendingIn.setText(IotaToText.convertRawIotaAmountToDisplayText(pendingIotaIn, true));
            pendingIn.setVisibility(View.VISIBLE);
        }
        if(pendingIotaOut==0 && pendingIotaIn==0) {
            pending.setVisibility(View.GONE);
        } else {
            pending.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(balanceText)) {
            balanceTextView.setText(balanceText);
        } else {
            balanceTextView.setText(R.string.account_balance_default);
        }
        updateAlternateBalance();
        updateFab();

        if(pendingIotaIn!=0 || pendingIotaOut!=0) {
            balanceType.setVisibility(View.VISIBLE);
            balanceType.setImageResource(R.drawable.bal_live);

            switch (Store.getBalanceDisplayType()) {
                case 1:
                    balanceType.setImageResource(R.drawable.bal_avail);
                    break;
                case 2:
                    balanceType.setImageResource(R.drawable.bal_future);
                    break;
            }
        } else {
            balanceType.setVisibility(View.GONE);
        }
    }
    @Subscribe
    public void onEvent(ApiResponse str) {
        isConnected = true;
        updateFab();
        updateBalance();
    }

    @Subscribe
    public void onEvent(GetAccountDataResponse str) {
        AppService.auditAddressesWithDelay(getActivity(), Store.getCurrentSeed());
    }


    private void updateFab() {
        //fabWallet.show();
        fabWallet.setEnabled(true);

        switch (viewPager.getCurrentItem()) {
            case 0:
                fabWallet.setImageResource(R.drawable.ic_fab_send);
                break;
            case 1:
                fabWallet.setImageResource(R.drawable.ic_fab_send);
                break;
        }
        NodeInfoResponse info=Store.getNodeInfo();
        if(Store.getCurrentWallet()!=null && viewPager.getCurrentItem()==0) {
            fabWallet.show();
        } else {
            fabWallet.hide();
        }

    }

    @Subscribe
    public void onEvent(NetworkError error) {
        switch (error.getErrorType()) {
            case REMOTE_NODE_ERROR:
            case NETWORK_ERROR:
                isConnected = false;
                updateFab();
                break;
        }
        updateFab();
    }

    @Subscribe
    public void onEvent(WebGetExchangeRatesResponse response) {
        updateAlternateBalance();
    }

    private void requestExchangeRateUpdate() {
        //alternateValueManager.updateExchangeRatesAsync(false);
    }

    private void updateAlternateBalance() {
        Currency alternateCurrency = Utils.getConfiguredAlternateCurrency(getActivity());

        Ticker useticker= Store.getTicker("IOTA:"+alternateCurrency);
        if(useticker!=null && Store.getCurrentWallet()!=null) {
            //Log.e("RATE",walletBalanceIota+"-- IOTA:"+alternateCurrency+" = "+useticker.getLast());
            alternateBalanceTextView.setText(useticker.getIotaValString(Store.getCurrentWallet().getBalanceDisplay()));
            altCurrency.setText(alternateCurrency.getSymbol());
        } else {
            alternateBalanceTextView.setText("");
            altCurrency.setText("");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void getAccountData() {
        AppService.getAccountData(getActivity(),Store.getCurrentSeed());
        requestExchangeRateUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();
        tabLayout.setupWithViewPager(viewPager);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        tapCount= prefs.getInt(Constants.PREF_MSG_TAP_BALANCE,0);
        Store.setCurrentFragment(this.getClass());
        EventBus.getDefault().register(this);
        updateBalance();
        if(Store.getCurrentWallet()==null) {
            if(!AppService.isFirstTimeLoadRunning(getActivity()))
                AppService.getFirstTimeLoad(getActivity());
        } else if(Store.getCurrentWallet()!=null) {
            if(Store.getCurrentWallet().getLastUpdate()<System.currentTimeMillis()- (Cal.MINUTES_1_IN_MILLIS*20)) {
                AppService.getAccountData(getActivity(),Store.getCurrentSeed());
            }
        }
    }
    @Subscribe
    public void onEvent(SendTransferResponse str) {
        updateBalance();
    }
    @Subscribe
    public void onEvent(NodeInfoResponse nodeInfoResponse) {
        updateFab();
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (isConnected) {
                //getAccountData();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public interface OnFabClickListener {
        void onFabClick();
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            updateFab();
            //AppService.refreshEvent();

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}