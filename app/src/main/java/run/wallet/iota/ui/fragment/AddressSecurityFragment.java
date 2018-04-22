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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import jota.utils.SeedRandomGenerator;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Sf;
import run.wallet.iota.api.responses.AddressSecurityChangeResponse;
import run.wallet.iota.api.responses.RefreshEventResponse;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.SeedValidator;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.dialog.CopySeedDialog;

public class AddressSecurityFragment extends Fragment {


    private Unbinder unbinder;

    @BindView(R.id.address_toolbar)
    Toolbar Toolbar;
    @BindView(R.id.address_security_spinner)
    Spinner spinner;
    @BindView(R.id.address_security_button)
    AppCompatButton button;

    @BindView(R.id.address_security_non_zero)
    RelativeLayout viewNonZero;
    @BindView(R.id.address_security_change)
    LinearLayout viewChange;
    @BindView(R.id.address_security_completed)
    RelativeLayout viewCompleted;
    @BindView(R.id.address_security_generating)
    RelativeLayout  viewGenerating;
    @BindView(R.id.address_security_show)
    TextView showAddress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_security, container, false);
        view.setBackgroundColor(B.getColor(getActivity(), AppTheme.getSecondary()));
        unbinder = ButterKnife.bind(this, view);

        //UiManager.setActionBarBackOnly(getActivity(),getString(R.string.seed_add),null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(Store.getCacheAddress()==null) {
            getActivity().getFragmentManager().popBackStack();
            return;
        }

        ((AppCompatActivity) getActivity()).setSupportActionBar(Toolbar);
        setHasOptionsMenu(false);
        Toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        Toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();
            }
        });
        String[] options=new String[]{"1","2","3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_security, options);
        adapter.setDropDownViewResource(R.layout.spinner_security_open);
        showAddress.setText(Store.getCacheAddress().getAddress());
        spinner.setAdapter(adapter);
        spinner.setSelection(Store.getCacheAddress().getSecurity()-1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshButton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }
    private void refreshButton() {
        int security = Store.getCacheAddress().getSecurity();
        if(Sf.toInt((String) spinner.getSelectedItem())==security) {
            button.setEnabled(false);
            button.setVisibility(View.GONE);
        } else {
            button.setEnabled(true);
            button.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        //|| Store.getCacheAddress().isUsed()
        if(Store.getCacheAddress()==null
                || Store.getCacheAddress().getValue()>0 || Store.getCacheAddress().getPendingValue()>0) {
            viewCompleted.setVisibility(View.GONE);
            viewChange.setVisibility(View.GONE);
            viewGenerating.setVisibility(View.GONE);
            viewNonZero.setVisibility(View.VISIBLE);
        } else {
            viewCompleted.setVisibility(View.GONE);
            viewChange.setVisibility(View.VISIBLE);
            viewGenerating.setVisibility(View.GONE);
            viewNonZero.setVisibility(View.GONE);
        }

        //UiManager.setActionBarBackOnly(getActivity(),getString(R.string.seed_add),null);

    }
    @OnClick(R.id.address_security_button)
    public void onAddressSecurityButtonClick() {
        int security=Sf.toInt((String) spinner.getSelectedItem());
        viewCompleted.setVisibility(View.GONE);
        viewChange.setVisibility(View.GONE);
        viewGenerating.setVisibility(View.VISIBLE);

        AppService.addressSecurityChange(getActivity(),Store.getCurrentSeed(), Store.getCacheAddress(),security);
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
    public void onEvent(AddressSecurityChangeResponse gnar) {
        //Log.e("REF","Refresh Event called AddressSecurityChangeResponse.............................");
        viewCompleted.setVisibility(View.VISIBLE);
        viewChange.setVisibility(View.GONE);
        viewGenerating.setVisibility(View.GONE);
        WalletAddressCardAdapter.load(getActivity(),true);
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
