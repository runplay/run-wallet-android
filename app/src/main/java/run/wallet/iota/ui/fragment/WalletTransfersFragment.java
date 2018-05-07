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
import android.content.Context;
import android.os.AsyncTask;
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
import run.wallet.common.B;
import run.wallet.iota.api.handler.GetFirstLoadRequestHandler;
import run.wallet.iota.api.requests.ReplayBundleRequest;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetAccountDataResponse;
import run.wallet.iota.api.responses.GetFirstLoadResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.api.responses.NudgeResponse;
import run.wallet.iota.api.responses.RefreshEventResponse;
import run.wallet.iota.api.responses.SendTransferResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.RecyclerLayoutManager;
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
    @BindView(R.id.transfers_loading)
    RelativeLayout transferLoading;

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

    private static boolean shouldRefresh=false;
    public static void setShouldRefresh(boolean refresh)    {
        shouldRefresh=refresh;
    }
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
    @Override
    public void onRefresh() {
        super.onRefresh();
        if(AppService.canRunGetAccountData(Store.getCurrentSeed())) {
            AppService.getAccountData(getActivity(), Store.getCurrentSeed(), true);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
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
                        firstLoadPredict.setText(IotaToText.convertRawIotaAmountToDisplayText(holder.predictaddress, true));
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

            if(shouldRefresh) {
                shouldRefresh=false;
                setAdapter(true);
            }
            attachingHandler.postDelayed(runAttaching,1000);
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_transfers, container, false);
        view.setBackgroundColor(B.getColor(getActivity(),AppTheme.getSecondary()));
        unbinder = ButterKnife.bind(this, view);
        swipeRefreshLayout = view.findViewById(R.id.wallet_transfers_swipe_container);
        /*
        jota.model.Bundle b = new jota.model.Bundle();
        int[] bi=b.normalizedBundle("BRKTCAGBXOWOLIVKKAS9BGWQYJANAUJJRFSUHBEAASNKIGIGQRENVT9WEREYZPILZTLBQBIRBZJRQEGDA");
        for(int i=0; i<bi.length; i++) {
            if(bi[i]==13)
                Log.e("TEST",bi[i]+" = bad transaction.."));
        }
*/
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new RecyclerLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
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
            AppService.getNodeInfoSilent(getActivity());
        setAdapter(true);
        attachingHandler.postDelayed(runAttaching,500);

        if(Store.getCurrentWallet()==null) {
            firstLoad.postDelayed(runFirstLoad,500);
        } else {
            firstLoadPod.setVisibility(View.GONE);
            firstLoad.removeCallbacks(runFirstLoad);
            confirmPod.setVisibility(View.GONE);
            transferLoading.setVisibility(View.VISIBLE);
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

        AppService.getAccountData(getActivity(),Store.getCurrentSeed());
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

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
    public void onEvent(NudgeResponse str) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);
    }
    @Subscribe
    public void onEvent(NodeInfoResponse nodeInfoResponse) {
        if (nodeInfoResponse.isSyncOk()) {
            //getAccountData();

        } else {
            if(!nodeInfoResponse.isSilent()) {
                swipeRefreshLayout.setRefreshing(false);
                Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_not_fully_synced_yet), Snackbar.LENGTH_LONG).show();
            }
        }
    }
    @Subscribe
    public void onEvent(RefreshEventResponse gnar) {
        setAdapter(false);
    }

    private static int firstVis=0;
    public static void resetScroll() {
        firstVis=0;
    }

    private class AsyncCall extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {

            if(adapter==null) {
                firstVis=0;
                adapter = new WalletTransfersCardAdapter(getActivity(),params[0]);
            } else {
                if(recyclerView!=null) {
                    try {
                        firstVis = recyclerView.getVerticalScrollbarPosition();
                    } catch(Exception e) {}
                }
                if(params[0]) {
                    WalletTransfersCardAdapter.load(getActivity(),true);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(recyclerView!=null) {
                try {
                    transferLoading.setVisibility(View.GONE);
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

                        Address address = Store.isAlreadyAddress(WalletTransfersCardAdapter.getFilterAddress(), Store.getAddresses());
                        if (address != null) {
                            filterBar.setVisibility(View.VISIBLE);
                            filterBar.setBackgroundColor(AppTheme.getColorPrimaryDark(getActivity()));
                            IotaToText.IotaDisplayData data = IotaToText.getIotaDisplayData(address.getValue());
                            filterAddress.setText(address.getAddress());
                            filterId.setText(WalletTransfersCardAdapter.getFilterAddressId());
                            filterBalance.setText(data.value);
                            filterBalanceThird.setText(data.thirdDecimal);
                            filterBalanceUnit.setText(data.unit);

                        }
                    }
                    recyclerView.scrollToPosition(firstVis);
                } catch(Exception e) {}
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }
    private void setAdapter(boolean force) {
        if(getActivity()!=null && !getActivity().isDestroyed()) {
            AsyncCall task = new AsyncCall();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, force);
        }
    }



    @Override
    public void onFabClick() {
        NodeInfoResponse info=Store.getNodeInfo();
        if(info==null) {
            AppService.getNodeInfo(getActivity());
        } else {
            if (info.isSyncOk()) {
                if(AppService.countTransferRunningTasks(Store.getCurrentSeed())==0) {
                    UiManager.openFragmentBackStack(getActivity(),SnTrFragment.class);
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.drawer_layout), R.string.messages_wait_for_transfer, Snackbar.LENGTH_LONG).show();
                }

            }  else {
                AppService.getNodeInfo(getActivity());
                Snackbar.make(getActivity().findViewById(R.id.drawer_layout), R.string.messages_not_fully_synced_yet, Snackbar.LENGTH_LONG).show();
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



}
