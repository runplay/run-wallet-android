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

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import run.wallet.iota.api.responses.AddressSecurityChangeResponse;
import run.wallet.iota.api.responses.ApiResponse;
import run.wallet.iota.api.responses.GetAccountDataResponse;
import run.wallet.iota.api.responses.GetFirstLoadResponse;
import run.wallet.iota.api.responses.GetNewAddressResponse;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.api.responses.NudgeResponse;
import run.wallet.iota.api.responses.RefreshEventResponse;
import run.wallet.iota.api.responses.SendTransferResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class WalletAddressesFragment extends BaseSwipeRefreshLayoutFragment  {

    private static final String ADDRESSES_LIST = "addresses";
    private WalletAddressCardAdapter adapter;

    @BindView(R.id.wallet_addresses_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.info_bar_addresses)
    LinearLayout infoBar;
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
    @BindView(R.id.addresses_empty)
    LinearLayout emptyAddresses;
    @BindView(R.id.addresses_empty_create)
    TextView emptyCreate;
    @BindView(R.id.first_load_pod_confirm)
    RelativeLayout confirmPod;
    @BindView(R.id.fl_confirm_yes)
    AppCompatButton yesButton;
    @BindView(R.id.fl_confirm_no)
    AppCompatButton noButton;
    //private List<Address> addresses;
    private static boolean shouldRefresh=false;
    public static void setShouldRefresh(boolean refresh)    {
        shouldRefresh=refresh;
    }
    private Unbinder unbinder;
    Handler firstLoad = new Handler();

    private Runnable runFirstLoad=new Runnable() {
        @Override
        public void run() {
            GetFirstLoadRequestHandler.FirstTimeHolder holder=GetFirstLoadRequestHandler.getHolder(Store.getCurrentSeed().id);
            if(holder!=null) {
                if(holder.userConfirmedBalance==null) {
                    confirmPod.setVisibility(View.VISIBLE);
                    firstLoadPod.setVisibility(View.GONE);
                } else {
                    confirmPod.setVisibility(View.GONE);
                    firstLoadPod.setVisibility(View.VISIBLE);
                    firstLoadAddress.setText("" + holder.countaddress);
                    firstLoadPredict.setText(IotaToText.convertRawIotaAmountToDisplayText(holder.predictaddress,true));
                    firstLoadTransfer.setText("" + holder.counttransfers);
                    if (holder.showWaitMessage) {
                        firstLoadRelax.setVisibility(View.VISIBLE);
                    } else {
                        firstLoadRelax.setVisibility(View.GONE);
                    }
                }

            }
            if(holder==null || !holder.isFinished)
                firstLoad.postDelayed(runFirstLoad,400);
        }
    };

    Handler attachingHandler = new Handler();
    private Runnable runAttaching=new Runnable() {
        @Override
        public void run() {
            UiManager.displayInfoBar(getActivity(),infoBar);
            if(emptyAddresses.getVisibility()==View.VISIBLE && AppService.countAddressRunningTasks(Store.getCurrentSeed())>0) {
                emptyCreate.setVisibility(View.VISIBLE);
            }
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
        View view = inflater.inflate(R.layout.fragment_wallet_addresses, container, false);
        view.setBackgroundColor(B.getColor(getActivity(), AppTheme.getSecondary()));
        unbinder = ButterKnife.bind(this, view);
        swipeRefreshLayout = view.findViewById(R.id.wallet_addresses_swipe_container);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    @Override
    public void onPause() {
        super.onPause();
        attachingHandler.removeCallbacks(runAttaching);
        firstLoad.removeCallbacks(runFirstLoad);
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
            confirmPod.setVisibility(View.GONE);
            emptyAddresses.setVisibility(View.GONE);
        }
        if(Store.getCurrentWallet()!=null) {
            int countemptyattached = 0;
            for (Address address : Store.getAddresses()) {
                if (address.isAttached() && address.getValue() == 0 && !address.isUsed() && !address.isPig()) {
                    countemptyattached++;
                }
            }
            int countmin = Store.getAutoAttach();
            if (countemptyattached < countmin) {
                AppService.auditAddresses(getActivity(), Store.getCurrentSeed());
            }
        }
    }
    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }
    private static int firstVis=0;
    public static void resetScroll() {
        firstVis=0;
    }
    private void setAdapter(boolean force) {
        if(recyclerView!=null) {
            if (adapter == null) {
                firstVis = 0;
                adapter = new WalletAddressCardAdapter(getActivity());
            } else {
                firstVis = recyclerView.getVerticalScrollbarPosition();
                if (force) {
                    WalletAddressCardAdapter.load(getActivity(), true);
                }

            }
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            if (adapter.getItemCount() != 0) {
                //Log.e("HMMM","Adapter is not empty");
                emptyAddresses.setVisibility(View.GONE);
                firstLoadPod.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            } else {
                //Log.e("HMMM","Adapter is empty");
                if (Store.getCurrentWallet() != null) {
                    //Log.e("HMMM","Adapter is empty so is wallet");
                    emptyAddresses.setVisibility(View.VISIBLE);

                }
            }
            recyclerView.scrollToPosition(firstVis);
        }
    }

    @Subscribe
    public void onEvent(RefreshEventResponse gnar) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);
    }
    @Subscribe
    public void onEvent(GetNewAddressResponse gnar) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);

        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
        }
    }

    @Subscribe
    public void onEvent(SendTransferResponse str) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);

    }

    @Subscribe
    public void onEvent(GetAccountDataResponse str) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);
    }
    @Subscribe
    public void onEvent(NudgeResponse str) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);
    }
    @Subscribe
    public void onEvent(GetFirstLoadResponse str) {
        swipeRefreshLayout.setRefreshing(false);
        //Log.e("FL","FL response addresses");
        firstLoad.removeCallbacks(runFirstLoad);
        //.setVisibility(View.GONE);
        firstLoadPod.setVisibility(View.GONE);
        setAdapter(true);
    }



    @Subscribe
    public void onEvent(ApiResponse str) {
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);
    }
    @Subscribe
    public void onEvent(AddressSecurityChangeResponse gnar) {
        //Log.e("REF","Refresh Event called AddressSecurityChangeResponse.............................");
        swipeRefreshLayout.setRefreshing(false);
        setAdapter(true);
    }
    @Subscribe
    public void onEvent(NodeInfoResponse nodeInfoResponse) {
        //Log.e("IOTA-1","Mstone: "+nodeInfoResponse.getLatestMilestoneIndex() +"-"+ nodeInfoResponse.getLatestSolidSubtangleMilestoneIndex());
        if (nodeInfoResponse.getLatestMilestoneIndex() == (nodeInfoResponse.getLatestSolidSubtangleMilestoneIndex())) {
            //AppService.getAccountData(getActivity());
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_not_fully_synced_yet), Snackbar.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        swipeRefreshLayout.setRefreshing(false);
        switch (error.getErrorType()) {
            case ACCESS_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                //AppService.getNodeInfo(getActivity());
                break;
            case REMOTE_NODE_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                setAdapter(false);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }



    @Override
    public void onRefresh() {
        super.onRefresh();
        //AppService.getNodeInfo(getActivity());
        //Log.e("GETACC","REFRESH PUULLLLLL");
        //AppService.getAccountData(getActivity(),Store.getCurrentSeed(),true);
        AppService.auditAddresses(getActivity(),Store.getCurrentSeed());
        //getAccountData();
    }
    private void getAccountData() {
        if (!swipeRefreshLayout.isRefreshing()) {
            AppService.getAccountData(getActivity(),Store.getCurrentSeed());
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

}
