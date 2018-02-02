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
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;

import jota.utils.IotaToText;
import run.wallet.R;
import run.wallet.iota.api.handler.GetFirstLoadRequestHandler;
import run.wallet.iota.api.requests.ReplayBundleRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetAccountDataResponse;
import run.wallet.iota.api.responses.GetFirstLoadResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.api.responses.RefreshEventResponse;
import run.wallet.iota.api.responses.SendTransferResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class WalletTransfersFragment extends BaseSwipeRefreshLayoutFragment implements DataRefreshListener,WalletTabFragment.OnFabClickListener {

    private static final String TRANSFERS_LIST = "transfers";
    private WalletTransfersCardAdapter adapter;
    //private List<Transfer> transfers;
    @BindView(R.id.wallet_transfers_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.transfers_empty)
    LinearLayout emptyTransfers;

    @BindView(R.id.first_load_pod)
    RelativeLayout firstLoadPod;
    @BindView(R.id.tv_first_load_predict)
    TextView firstLoadPredict;
    @BindView(R.id.tv_first_load_address)
    TextView firstLoadAddress;
    @BindView(R.id.tv_first_load_transfer)
    TextView firstLoadTransfer;
    @BindView(R.id.tv_first_load_relax)
    TextView firstLoadRelax;
    @BindView(R.id.info_bar_transfers)
    LinearLayout infoBar;
    @BindView(R.id.info_bar_filter)
    LinearLayout filterBar;
    @BindView(R.id.item_ta_address_id)
    TextView filterId;


    @BindView(R.id.filter_wt_balance)
    TextView filterBalance;
    @BindView(R.id.filter_wt_balance_third)
    TextView filterBalanceThird;
    @BindView(R.id.filter_wt_balance_unit)
    TextView filterBalanceUnit;

    @BindView(R.id.item_ta_address_value)
    TextView filterAddress;

    @BindView(R.id.first_load_pod_confirm)
    RelativeLayout confirmPod;
    @BindView(R.id.fl_confirm_yes)
    AppCompatButton yesButton;
    @BindView(R.id.fl_confirm_no)
    AppCompatButton noButton;



    private Unbinder unbinder;

    Handler firstLoad = new Handler();

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void refreshData() {

        setAdapter(true);

    }
    private Runnable runFirstLoad=new Runnable() {
        @Override
        public void run() {
            if(firstLoad!=null) {
                GetFirstLoadRequestHandler.FirstTimeHolder holder = GetFirstLoadRequestHandler.getHolder(Store.getCurrentSeed().id);
                if (holder != null) {
                    if (holder.userConfirmedBalance == null) {
                        confirmPod.setVisibility(View.VISIBLE);
                        firstLoadPod.setVisibility(View.GONE);
                    } else {
                        confirmPod.setVisibility(View.GONE);
                        firstLoadPod.setVisibility(View.VISIBLE);
                        firstLoadAddress.setText("" + holder.countaddress);
                        firstLoadPredict.setText(IotaToText.convertRawIotaAmountToDisplayText(holder.predictaddress, false));
                        firstLoadTransfer.setText("" + holder.counttransfers);
                        if (holder.showWaitMessage) {
                            firstLoadRelax.setVisibility(View.VISIBLE);
                        } else {
                            firstLoadRelax.setVisibility(View.GONE);
                        }
                    }

                }
                if (holder == null || !holder.isFinished)
                    firstLoad.postDelayed(runFirstLoad, 400);
            }
        }
    };
    Handler attachingHandler = new Handler();
    private int everyCycle=0;
    private Runnable runAttaching=new Runnable() {
        @Override
        public void run() {
            UiManager.displayInfoBar(getActivity(),infoBar);
            attachingHandler.postDelayed(runAttaching,1000);
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_transfers, container, false);
        unbinder = ButterKnife.bind(this, view);
        swipeRefreshLayout = view.findViewById(R.id.wallet_transfers_swipe_container);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_wallet).setChecked(true);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetFirstLoadRequestHandler.setUserConfirm(Store.getCurrentSeed().id,true);
                confirmPod.setVisibility(View.GONE);
                firstLoadPod.setVisibility(View.VISIBLE);
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GetFirstLoadRequestHandler.setUserConfirm(Store.getCurrentSeed().id,false);
                confirmPod.setVisibility(View.GONE);
                firstLoadPod.setVisibility(View.VISIBLE);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(Store.getNodeInfo()==null)
            AppService.getNodeInfo(getActivity());
        setAdapter(true);
        attachingHandler.postDelayed(runAttaching,500);

        if(Store.getCurrentWallet()==null) {
            //firstLoadPod.setVisibility(View.VISIBLE);
            firstLoad.postDelayed(runFirstLoad,500);
        } else {
            firstLoadPod.setVisibility(View.GONE);
            firstLoad.removeCallbacks(runFirstLoad);
            confirmPod.setVisibility(View.GONE);
            //tvEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);

        }
    }
    @Override
    public void onPause() {
        super.onPause();
        firstLoad.removeCallbacks(runFirstLoad);
        attachingHandler.removeCallbacks(runAttaching);

    }
    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        switch (error.getErrorType()) {
            case ACCESS_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                break;
            case REMOTE_NODE_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                setAdapter(true);
                break;
        }
    }

    private void getAccountData() {
        //if (!swipeRefreshLayout.isRefreshing()) {
            AppService.getAccountData(getActivity(),Store.getCurrentSeed());
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        //}
    }



    @Subscribe
    public void onEvent(SendTransferResponse str) {
        if (Arrays.asList(str.getSuccessfully()).contains(true))
            getAccountData();
        swipeRefreshLayout.setRefreshing(false);

        setAdapter(true);
    }
    @Subscribe
    public void onEvent(GetAccountDataResponse str) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);
    }
    @Subscribe
    public void onEvent(ReplayBundleRequest str) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);
    }
    @Subscribe
    public void onEvent(ApiResponse str) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(false);
    }
    @Subscribe
    public void onEvent(GetFirstLoadResponse str) {
        swipeRefreshLayout.setRefreshing(false);
        firstLoad.removeCallbacks(runFirstLoad);
        firstLoadPod.setVisibility(View.GONE);
        setAdapter(true);
    }
    @Subscribe
    public void onEvent(NodeInfoResponse nodeInfoResponse) {
        if (nodeInfoResponse.isSyncOk()) {
            //getAccountData();

        } else {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_not_fully_synced_yet), Snackbar.LENGTH_LONG).show();
        }
    }
    @Subscribe
    public void onEvent(RefreshEventResponse gnar) {
        setAdapter(false);
    }
    private void setAdapter(boolean force) {

        if(adapter==null) {
            adapter = new WalletTransfersCardAdapter(getActivity(),force);
        } else {
            if(force) {
                WalletTransfersCardAdapter.load(getActivity(),true);
            }
            adapter.notifyDataSetChanged();
        }
        if(recyclerView!=null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            if (adapter.getItemCount() != 0) {
                emptyTransfers.setVisibility(View.GONE);
                firstLoadPod.setVisibility(View.GONE);
            } else {
                if (Store.getCurrentWallet() != null) {
                    emptyTransfers.setVisibility(View.VISIBLE);

                }
            }
            filterBar.setVisibility(View.GONE);
            if (WalletTransfersCardAdapter.getFilterAddress() != null) {

                Address address = Store.isAlreadyAddress(WalletTransfersCardAdapter.getFilterAddress(),Store.getAddresses());
                if(address!=null) {
                    filterBar.setVisibility(View.VISIBLE);
                    IotaToText.IotaDisplayData data=IotaToText.getIotaDisplayData(address.getValue());
                    filterAddress.setText(address.getAddress());
                    filterId.setText(WalletTransfersCardAdapter.getFilterAddressId());
                    filterBalance.setText(data.value);
                    filterBalanceThird.setText(data.thirdDecimal);
                    filterBalanceUnit.setText(data.unit);

                }
            }
        }


    }


    @Override
    public void onFabClick() {
        NodeInfoResponse info=Store.getNodeInfo();
        boolean okgo=true;
        if(info==null) {
            AppService.getNodeInfo(getActivity());
        } else {
            if (info.isSyncOk()) {
                if(AppService.countTransferRunningTasks(Store.getCurrentSeed())==0) {
                    Fragment fragment = new SnTrFragment();
                    Fragment parentFragment = getParentFragment();
                    if (parentFragment != null) {
                        parentFragment.getFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .add(R.id.container, fragment, null)
                                .addToBackStack(null)
                                .commit();
                    }
                } else {
                    Snackbar.make(getView(), R.string.messages_wait_for_transfer, Snackbar.LENGTH_LONG).show();
                }

            }  else {
                Snackbar.make(getView(), R.string.messages_not_fully_synced_yet, Snackbar.LENGTH_LONG).show();
            }
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

    @Override
    public void onRefresh() {
        super.onRefresh();
        //Log.e("TRAN-FRAG","onRefresh()");
        AppService.getAccountData(getActivity(),Store.getCurrentSeed(),true);
        //AppService.getNodeInfo(getActivity());
        //AppService.getNodeInfo(getActivity());
    }

}
