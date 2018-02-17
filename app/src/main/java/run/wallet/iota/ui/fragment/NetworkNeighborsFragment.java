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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



import org.greenrobot.eventbus.Subscribe;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.iota.api.TaskManager;
import run.wallet.iota.api.requests.AddNeighborsRequest;
import run.wallet.iota.api.requests.GetNeighborsRequest;
import run.wallet.iota.api.responses.AddNeighborsResponse;
import run.wallet.iota.api.responses.GetNeighborsResponse;
import run.wallet.iota.api.responses.RemoveNeighborsResponse;
import run.wallet.iota.api.responses.error.NetworkError;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Neighbor;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.activity.MainActivity;
import run.wallet.iota.ui.adapter.NeighborsListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class NetworkNeighborsFragment extends BaseSwipeRefreshLayoutFragment
        implements MainActivity.OnBackPressedClickListener {

    private static final int AUTOMATICALLY_DISMISS_ITEM = 3000;
    private static final String SEARCH_TEXT = "searchText";
    private static final String NEIGHBORS_LIST = "neighbors";
    private static final String REAVEAL_VIEW_STATE = "revealViewState";
    private static final String NEW_ADDRESS_TEXT = "newAddress";


    @BindView(R.id.connected_neighbors)
    TextView neighborsHeaderTextView;
    @BindView(R.id.neighbor_recycler_view)
    RecyclerView recyclerView;
    //private List<Neighbor> neighbors;
    @BindView(R.id.neighborImageFrameLayout)
    FrameLayout frameLayout;
    @BindView(R.id.fab_add_neighbor)
    FloatingActionButton fabAddButton;
    @BindView(R.id.reavel_linearlayout)
    FrameLayout revealView;
    @BindView(R.id.neighbor_edit_text_new_ip)
    EditText editTextNewAddress;
    @BindView(R.id.neighbours_empty)
    RelativeLayout tvEmpty;
    private boolean isEditTextVisible;
    private InputMethodManager inputManager;
    //private SearchView searchView;
    //private String savedSearchText = "";

    private NeighborsListAdapter adapter;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neighbors, container, false);
        unbinder = ButterKnife.bind(this, view);
        swipeRefreshLayout = view.findViewById(R.id.neighbors_swipe_container);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabAddButton.setVisibility(View.VISIBLE);
        recyclerView.setBackgroundColor(B.getColor(getActivity(),R.color.whiteAlpha75));
        revealView.setVisibility(View.INVISIBLE);
        isEditTextVisible = false;

    }


    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onBackPressedClickListener() {
        if (revealView != null && revealView.isShown()) {
            editTextNewAddress.getText().clear();
            inputManager.hideSoftInputFromWindow(editTextNewAddress.getWindowToken(), 0);

            hideReavelEditText(revealView);
            fabAddButton.setImageResource(R.drawable.ic_add);
        } else
            getActivity().finish();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.neighbors, menu);

    }

    @Override
    public void onResume() {
        super.onResume();
        getNeighbors(true);
        getNeighbors();
        setAdapter();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        setAdapter();
    }

    private void showRevealEditText(FrameLayout view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = view.getRight() - 30;
            int cy = view.getBottom() - 60;
            int finalRadius = Math.max(view.getWidth(), view.getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
            view.setVisibility(View.VISIBLE);
            isEditTextVisible = true;
            anim.start();
        } else {
            view.setVisibility(View.VISIBLE);
            isEditTextVisible = true;
        }

    }

    private void hideReavelEditText(final FrameLayout view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = view.getRight() - 30;
            int cy = view.getBottom() - 60;
            int initialRadius = view.getWidth();

            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    view.setVisibility(View.INVISIBLE);
                }
            });
            isEditTextVisible = false;
            anim.start();
        } else {
            view.setVisibility(View.INVISIBLE);
            isEditTextVisible = false;
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    public void onEvent(GetNeighborsResponse gpr) {
        swipeRefreshLayout.setRefreshing(false);
        Store.setNeighbours(gpr);
        // clear all online states

        tvEmpty.setVisibility(Store.getNeighbours().size() == 0 ? View.VISIBLE : View.GONE);
        neighborsHeaderTextView.setText(""+gpr.getNeighbors().size());
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        switch (error.getErrorType()) {
            case ACCESS_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                if (Store.getNeighbours() != null)
                    Store.getNeighbours().clear();
                if (adapter != null)
                    adapter.notifyDataSetChanged();
                break;
            case REMOTE_NODE_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                if (Store.getNeighbours() != null)
                    Store.getNeighbours().clear();
                setAdapter();
                break;
        }
    }

    @Subscribe
    public void onEvent(AddNeighborsResponse anr) {
        getNeighbors();
    }

    @Subscribe
    public void onEvent(RemoveNeighborsResponse rnr) {
        getNeighbors();
    }
    private void getNeighbors() {
        getNeighbors(false);
    }
    private void getNeighbors(boolean force) {
        if(Store.getNeighbours()==null || force) {
            GetNeighborsRequest nar = new GetNeighborsRequest();
            TaskManager rt = new TaskManager(getActivity());
            rt.startNewRequestTask(nar);
            if (!swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
            }
        }
    }

    @Override
    public void onRefresh() {
        getNeighbors(true);

        setAdapter();
    }

    private void setAdapter() {
        if (adapter == null) {
            adapter = new NeighborsListAdapter(getActivity());
            recyclerView.setAdapter(adapter);

        } else {
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        if(adapter.getItemCount()==0) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {

            if (neighborsHeaderTextView != null && Store.getNeighbours()!=null)
                neighborsHeaderTextView.setText(""+ Store.getNeighbours().size());

        }
        //setAdapter();
    }
}
