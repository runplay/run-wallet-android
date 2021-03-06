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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Sf;
import run.wallet.common.json.JSONArray;
import run.wallet.common.json.JSONObject;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.api.responses.RefreshEventResponse;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.JSONUrlReader;
import run.wallet.iota.model.Nodes;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.RecyclerLayoutManager;
import run.wallet.iota.ui.activity.MainActivity;
import run.wallet.iota.ui.adapter.NodesListAdapter;

public class NetworkNodesFragment extends Fragment
        implements MainActivity.OnBackPressedClickListener {


    @BindView(R.id.nodes_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.network_nodes_text_pod)
    View nodesTextPod;


    private NodesListAdapter adapter;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nodes, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new RecyclerLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        nodesTextPod.setBackgroundColor(B.getColor(getActivity(), AppTheme.getSecondary()));
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

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    @Override
    public void onResume() {
        super.onResume();
        setAdapter();

    }
    @Subscribe
    public void onEvent(NodeInfoResponse nir) {

        setAdapter();
    }
    @Subscribe
    public void onEvent(RefreshEventResponse nir) {

        setAdapter();
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

    }

    private void setAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NodesListAdapter(getActivity());
        recyclerView.setAdapter(null);
        recyclerView.setAdapter(adapter);
    }


}
