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
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Sf;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.model.Nodes;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.adapter.AddNodesListAdapter;

public class NetworkNodesAddFragment extends Fragment {

    @BindView(R.id.node_add_choose_button)
    Button addNodeBtn;

    @BindView(R.id.node_add_toolbar)
    Toolbar addNodeToolbar;
    @BindView(R.id.node_add_address_layout)
    TextInputLayout addressLayout;
    @BindView(R.id.node_add_address_input)
    TextInputEditText nodeAddress;

    @BindView(R.id.node_add_protocol_input)
    TextView protocol;
    @BindView(R.id.node_add_port_input)
    TextInputEditText port;

    @BindView(R.id.node_add_list_all)
    RecyclerView listNodes;
    @BindView(R.id.enter_pod)
    View enterPod;

    //private boolean useHttp=true;
    private String[] protos = new String[]{"http","https"};

    private Unbinder unbinder;

    AddNodesListAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_node_add, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    public void setNodeDetails(String ip, String useport, String proto) {
        nodeAddress.setText(ip);
        protocol.setText(proto);
        port.setText(useport);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(addNodeToolbar);
        setHasOptionsMenu(false);
        enterPod.setBackgroundColor(B.getColor(getActivity(), AppTheme.getSecondary()));
        addNodeToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        addNodeToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("cek", "home selected");
                getActivity().onBackPressed();
            }
        });

        protocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changeProtocol();
            }
        });

        addNodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Nodes.Node node = new Nodes.Node();
                node.ip=nodeAddress.getText().toString();
                node.port= Sf.toInt(port.getText().toString());
                node.protocol=protocol.getText().toString();
                node.ip= Utils.stripHttp(node.ip);

                if(node.ip.length()>3 && node.ip.contains(".") && node.port>0) {

                    Store.addNode(getActivity(), node.ip, node.port, node.protocol);

                    getActivity().onBackPressed();
                } else {
                    addressLayout.setError(getString(R.string.messages_enter_neighbor_address));
                }
            }
        });
        nodeAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //nodeAddress.
                addressLayout.setError(null);
            }
        });
        adapter=new AddNodesListAdapter(getActivity(),this);
        listNodes.setAdapter(adapter);

    }
    @Override
    public void onResume() {
        super.onResume();
        protocol.setText("http");
    }
    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(nodeAddress.getWindowToken(), 0);
    }
    private void changeProtocol() {
        String use=protocol.getText().toString();
        if(use.equals("https")) {
            protocol.setText(protos[0]);
        } else {
            protocol.setText(protos[1]);
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


    @OnEditorAction(R.id.node_add_address_input)
    public boolean onSeedLoginSeedInputEditorAction(int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            //loginDialog();
        }
        return true;
    }


    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {

            addNode();
        }
        return true;
    }

    private void addNode() {
        //Store.addNode(getActivity(),editTextNewAddress.getText().toString(),14265,"http");

        //editTextNewAddress.getText().clear();
        //inputManager.hideSoftInputFromWindow(editTextNewAddress.getWindowToken(), 0);
        //hideReavelEditText(revealView);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString(SEED, nodeAddress.getText().toString());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //nodeAddress.setText(savedInstanceState.getString(SEED));
        }
    }
}
