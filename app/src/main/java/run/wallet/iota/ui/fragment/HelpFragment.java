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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import run.wallet.R;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.QRCode;
import run.wallet.iota.model.Store;

public class HelpFragment extends Fragment {

    @BindView(R.id.help_toolbar)
    Toolbar toolbar;
    @BindView(R.id.about_donation_iota)
    TextView iotaNow;
    @BindView(R.id.about_donation_iota_address)
    TextView iotaAddress;

    private Unbinder unbinder;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(false);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        iotaAddress.setText(Constants.DONATION_ADDRESS);
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @OnClick(R.id.about_donation_iota)
    public void onAboutDonationIotaClick() {
        if (Store.getCurrentWallet() != null) {

            QRCode qrCode = new QRCode();
            qrCode.setAddress(Constants.DONATION_ADDRESS);
            qrCode.setTag(Constants.DONATION_TAG);

            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.QRCODE, qrCode);

            FragmentManager fragmentManager=getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SnTrFragment sendTransfer = new SnTrFragment();
            sendTransfer.setArguments(bundle);

            fragmentTransaction.replace(R.id.container,sendTransfer,SnTrFragment.class.getCanonicalName());
            fragmentTransaction.commit();

        } else
            onAboutDonationIotaAddressClick();
    }
    @OnClick(R.id.about_donation_iota_address)
    public void onAboutDonationIotaAddressClick() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getActivity().getString(R.string.seed), Constants.DONATION_ADDRESS);
        clipboard.setPrimaryClip(clip);
        Snackbar.make(getView(), getString(R.string.messages_iota_donation), Snackbar.LENGTH_LONG).show();
    }

}
