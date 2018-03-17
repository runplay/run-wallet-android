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

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Sf;
import run.wallet.iota.api.responses.AddressSecurityChangeResponse;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.dialog.ChooseSeedItemDialog;
import run.wallet.iota.ui.dialog.ShowNoDescDialog;
import run.wallet.iota.ui.dialog.WipeSeedDialog;

public class ChooseSeedEditFragment extends Fragment {


    private Unbinder unbinder;

    @BindView(R.id.address_toolbar)
    Toolbar Toolbar;
    @BindView(R.id.edit_wallet_name)
    EditText name;
    @BindView(R.id.edit_wallet_default)
    Switch setDefault;
    @BindView(R.id.edit_wallet_seed)
    TextView seed;
    @BindView(R.id.edit_wallet_view)
    Button btnViewSeed;
    @BindView(R.id.edit_wallet_reload)
    Button btnReload;
    @BindView(R.id.edit_wallet_check)
    Button checkUsed;

    private Seeds.Seed useSeed;
    private boolean hasChanges;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_edit, container, false);
        view.setBackgroundColor(B.getColor(getActivity(), AppTheme.getSecondary()));
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(Toolbar);
        setHasOptionsMenu(false);
        Toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        Toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();
            }
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(useSeed!=null) {
                    useSeed.name = s.toString();
                    hasChanges = true;
                }
            }
        });
        setDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    useSeed.isdefault=isChecked;
                    Store.setDefaultSeed(useSeed);
                    hasChanges=true;
                    setDefault.setEnabled(false);
                    setDefault.setText(getString(R.string.title_edit_isdefault));
                }
            }
        });
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WipeSeedDialog dialog = new WipeSeedDialog();
                dialog.setSeed(useSeed);

                dialog.show(getActivity().getFragmentManager(), null);
            }
        });
        checkUsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppService.checkUsedAddress(useSeed);
                Store.setCurrentSeed(getActivity(),useSeed);
                WalletTransfersFragment.resetScroll();

                WalletAddressesFragment.resetScroll();
                WalletTransfersCardAdapter.setFilterAddress(null,null);
                WalletAddressCardAdapter.load(getActivity(),true);
                WalletTransfersCardAdapter.load(getActivity(),true);
                UiManager.openFragment(getActivity(), WalletTabFragment.class);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(hasChanges) {
            Store.saveSeeds(getActivity());
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Store.setCurrentFragment(this.getClass());
        hasChanges=false;
        if(Store.getCacheSeed()==null)
            getActivity().onBackPressed();
        useSeed=Store.getCacheSeed();
        seed.setText(useSeed.getShortValue());
        name.setText(useSeed.name);
        if(useSeed.isdefault) {
            setDefault.setChecked(true);
            setDefault.setEnabled(false);
            setDefault.setText(getString(R.string.title_edit_isdefault));
        } else {
            setDefault.setChecked(false);
            setDefault.setEnabled(true);
            setDefault.setText(getString(R.string.title_edit_default));
        }
    }
    @OnClick(R.id.edit_wallet_view)
    public void onEditWalletViewClick() {
        ShowNoDescDialog showSeedDialog = new ShowNoDescDialog();
        showSeedDialog.setSeed(Store.getCacheSeed());
        showSeedDialog.show(getActivity().getFragmentManager(), null);
    }
    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

}
