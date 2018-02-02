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

package run.wallet.iota.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import jota.utils.Checksum;
import jota.utils.IotaToText;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.iota.api.requests.SendTransferRequest;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.dialog.WalletAddressesItemDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WalletAddressCardAdapter extends RecyclerView.Adapter<WalletAddressCardAdapter.ViewHolder> {

    private final Activity context;
    private static List<Address> addresses=new ArrayList<>();
    private static Seeds.Seed seed;
    //private List<Address> notliveaddresses;
    private static final DecimalFormat df = new DecimalFormat("#,###,###,###,###,###,###.##");

    public List<Address> getAddresses() {
        return addresses;
    }
    public static void clear() {
        addresses.clear();
    }

    public WalletAddressCardAdapter(Activity context) {
        this.context = context;
        load(context,false);
    }
    public WalletAddressCardAdapter(Activity context, boolean force) {
        this.context = context;
        load(context,force);
    }
    public static synchronized void load(Context context,boolean force) {
        //Log.e("TRANS-ADAPT","Called for refresh of addresses");
        if(force || seed==null || !seed.id.equals(Store.getCurrentSeed().id) || addresses.isEmpty()) {
            //Log.e("ADD-ADAPT","*******************loading addresses for list");
            seed=Store.getCurrentSeed();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean showused=prefs.getBoolean(Constants.PREFERENCES_SHOW_USED,true);

            addresses.clear();
            for(Address add: Store.getAddresses()) {
                if(add.isAttached()) {
                //Log.e("ADDU",add.getAddress()+" -- "+showused +"="+ add.isUsed());
                    if(showused)
                        addresses.add(add);
                    else if(!showused && !add.isUsed())
                        addresses.add(add);
                    else if(!showused && add.isUsed() && (add.getValue()>0|| add.getPendingValue()!=0))
                        addresses.add(add);
                }
            }
            //Collections.reverse(addresses);
            //this.notifyDataSetChanged();
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_wallet_address, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        Address address = getItem(adapterPosition-1);
        if(address!=null) {
            holder.address=address.getAddress();
            try {
                holder.addressChecksum = Checksum.addChecksum(holder.address);
            } catch(Exception e) {}
            if(holder.addressChecksum!=null) {
                holder.isUsed = address.isUsed();
                holder.isAttached = address.isAttached();
                holder.isPig = address.isPig();
                holder.pigInt = address.getPigInt();
                holder.setIsRecyclable(false);

                IotaToText.IotaDisplayData data = IotaToText.getIotaDisplayData(address.getValue());
                holder.addressId.setText("a" + address.getIndexName());
                holder.addressValue.setText(data.value);
                holder.addressValueThird.setText(data.thirdDecimal);
                holder.addressValueUnit.setText(data.unit);

                holder.rawValue.setText(" ");
                if (address.getValue() > 0) {
                    holder.addressValue.setTextColor(B.getColor(context, R.color.green));
                    holder.addressValueThird.setTextColor(B.getColor(context, R.color.green));
                    holder.addressValueUnit.setTextColor(B.getColor(context, R.color.colorPrimary));
                } else {
                    holder.addressValue.setTextColor(B.getColor(context, R.color.grey));
                    holder.addressValueThird.setTextColor(B.getColor(context, R.color.grey));
                    holder.addressValueUnit.setTextColor(B.getColor(context, R.color.grey));
                    //holder.rawValue.setVisibility(View.GONE);
                }
                holder.securityValue.setText("s" + address.getSecurity());
                holder.rawValue.setTextColor(B.getColor(context, R.color.greyDark));
                holder.rawValue.setCompoundDrawables(null, null, null, null);

                if (address.getPendingValue() != 0) {
                    //holder.rawValue.setVisibility(View.VISIBLE);
                    holder.rawValue.setCompoundDrawables(B.getDrawable(context, R.drawable.navigation_refresh), null, null, null);

                    if (address.getPendingValue() < 0) {

                        holder.rawValue.setText(IotaToText.convertRawIotaAmountToDisplayText(address.getPendingValue(), false));
                        holder.rawValue.setTextColor(B.getColor(context, R.color.flatRed));
                    } else {
                        holder.rawValue.setText(IotaToText.convertRawIotaAmountToDisplayText(address.getPendingValue(), false));
                        holder.rawValue.setTextColor(B.getColor(context, R.color.green));
                    }
                }
                if (address.isUsed()) {
                    holder.addressLabel.setText(address.getShortAddress() + "***");
                } else {
                    holder.addressLabel.setText(holder.addressChecksum);
                }
                holder.addressImage.setImageResource(R.drawable.ic_address);
                holder.addressLabel.setPaintFlags(0);
                holder.addressValue.setPaintFlags(0);
                holder.addressValue.setAlpha(1F);
                holder.cardView.setCardBackgroundColor(B.getColor(context, R.color.white));
                holder.cardView.setAlpha(1F);
                if (address.isUsed()) {
                    holder.addressImage.setColorFilter(ContextCompat.getColor(context, R.color.flatRed));
                    holder.addressLabel.setPaintFlags(holder.addressLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.addressLabel.setTextColor(B.getColor(context, R.color.flatRed));
                    holder.addressValue.setAlpha(0.5F);
                    holder.cardView.setAlpha(0.6F);
                    holder.addressValue.setPaintFlags(holder.addressLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else if (address.isPig()) {
                    holder.cardView.setCardBackgroundColor(B.getColor(context, R.color.whiteAlpha));
                    holder.addressImage.setImageResource(R.drawable.pig);
                    holder.addressLabel.setTextColor(B.getColor(context, R.color.colorPrimaryDark));
                } else if (!address.isAttached()) {
                    holder.addressImage.setColorFilter(ContextCompat.getColor(context, R.color.flatGreen));
                    holder.addressLabel.setTextColor(B.getColor(context, R.color.grey));
                } else if (!address.isUsed()) {
                    holder.addressImage.setColorFilter(ContextCompat.getColor(context, R.color.greyDark));
                    holder.addressLabel.setTextColor(B.getColor(context, R.color.textDark));
                }
            }
        }
    }

    private Address getItem(int position) {
        return addresses.get(position+1);
    }

    public void setAdapterList(List<Address> addresses) {
        this.addresses = addresses;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        boolean isUsed;
        boolean isAttached;
        boolean isPig;
        int pigInt=0;
        String address;
        String addressChecksum;
        @BindView(R.id.item_wa_address)
        TextView addressLabel;
        @BindView(R.id.item_wa_address_image)
        ImageView addressImage;
        @BindView(R.id.item_wa_address_balance)
        TextView addressValue;
        @BindView(R.id.item_wa_address_third)
        TextView addressValueThird;
        @BindView(R.id.item_wa_address_unit)
        TextView addressValueUnit;

        @BindView(R.id.item_wa_address_rawvalue)
        TextView rawValue;
        @BindView(R.id.item_wa_address_id)
        TextView addressId;
        @BindView(R.id.card_address)
        CardView cardView;
        @BindView(R.id.item_wa_address_security)
        TextView securityValue;


        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v -> {
                WalletTransfersCardAdapter.setFilterAddress(addressId.getText().toString(),address);
                WalletTransfersCardAdapter.load(context,true);
                WalletTransfersCardAdapter.goActions();
            });

            itemView.setOnLongClickListener(v -> {
                if(isUsed) {
                    Snackbar.make(context.findViewById(R.id.drawer_layout), context.getString(R.string.used_address), Snackbar.LENGTH_LONG);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("address", address);
                    bundle.putString("addressChecksum", addressChecksum);
                    bundle.putInt("isAddressUsed", (isUsed ? 1 : 0));
                    bundle.putInt("isAttached", (isAttached ? 1 : 0));
                    bundle.putInt("pig", pigInt);
                    WalletAddressesItemDialog dialog = new WalletAddressesItemDialog();
                    dialog.setArguments(bundle);
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            load(context,true);
                        }
                    });
                    dialog.show(context.getFragmentManager(), null);
                }
                return true;
            });

        }
    }
}
