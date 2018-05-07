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

package run.wallet.iota.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import run.wallet.R;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.QRCode;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.activity.MainActivity;
import run.wallet.iota.ui.fragment.AddressSecurityFragment;
import run.wallet.iota.ui.fragment.GenerateQRCodeFragment;


public class WalletAddressesItemDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private String address;
    private String addressChecksum;
    private boolean isAddressUsed;
    private boolean isAttached;
    private int isPig;
    public WalletAddressesItemDialog() {
    }
    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        address = bundle.getString("address");
        addressChecksum = bundle.getString("addressChecksum");
        isAttached = bundle.getInt("isAttached") != 0;
        isAddressUsed = bundle.getInt("isAddressUsed") != 0;
        isPig=bundle.getInt("pig");

        //Log.e("----",bundle.getString("address")+"=="+bundle.getInt("isAddressUsed")+"=="+bundle.getInt("isAttached")+"=="+isAttached+"--"+isAddressUsed+"--"+isPig);
        int list=R.array.listOnWalletAddressesRecyclerViewClickDialogNoPig;
        if(isAddressUsed) {
            list=R.array.listOnWalletAddressesRecyclerViewClickDialogUsed;
        } else if(isPig>0) {
            list=R.array.listOnWalletAddressesRecyclerViewClickDialogPig;
        } else
            list=R.array.listOnWalletAddressesRecyclerViewClickDialogNoPig;

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.address))
                .setItems(list, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        Fragment fragment;
        final Bundle bundle = new Bundle();
        if(address!=null) {
            int useWhich=which;
            if(isAddressUsed) {
                if(which==1)
                    useWhich=3;
            }
            switch (useWhich) {
                case 0:

                    ClipData clipAddress = ClipData.newPlainText(getActivity().getString(R.string.address), addressChecksum);
                    clipboard.setPrimaryClip(clipAddress);
                    break;
                case 1:
                    if (!isAddressUsed) {
                        QRCode qrCode = new QRCode();
                        qrCode.setAddress(addressChecksum);
                        bundle.putParcelable(Constants.QRCODE, qrCode);

                        fragment = new GenerateQRCodeFragment();
                        fragment.setArguments(bundle);

                        getActivity().getFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .replace(R.id.container, fragment, null)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_address_used), Snackbar.LENGTH_LONG).show();
                    }
                    break;
                case 2:
                    if(isPig==1) {
                        Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.help_aw_piglock_no_unlock), Snackbar.LENGTH_LONG).show();
                    } else {
                        if (isPig>1) {
                            //Log.e("SET-PIG","set to false");
                            Store.setCurrentAddressPig(getActivity(), address, false);
                        } else {
                            //Log.e("SET-PIG","set to true");
                            Store.setCurrentAddressPig(getActivity(), address, true);
                        }
                        AppService.refreshEvent();
                    }

                    break;
                case 3:
                    Address useaddress = Store.isAlreadyAddress(address, Store.getAddresses());
                    if (useaddress != null) {
                        Store.setCacheAddress(useaddress);
                        UiManager.openFragmentBackStack(getActivity(), AddressSecurityFragment.class);
                    }
                    break;
            }
        }
    }
}