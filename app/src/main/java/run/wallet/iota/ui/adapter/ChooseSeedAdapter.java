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
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jota.utils.IotaToText;
import run.wallet.R;
import run.wallet.common.Currency;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Ticker;
import run.wallet.iota.model.Wallet;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.dialog.ChooseSeedItemDialog;

public class ChooseSeedAdapter extends RecyclerView.Adapter<ChooseSeedAdapter.ViewHolder> {

    private final Context context;
    private List<Seeds.Seed> seeds;



    public ChooseSeedAdapter(Context context) {
        this.context = context;
        this.seeds = Store.getSeedList();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_seed, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        Seeds.Seed address = getItem(adapterPosition - 1);
        holder.isDefault = address.isdefault;
        holder.setIsRecyclable(false);
        holder.seed=address;
        holder.addressLabel.setText(address.getShortValue()+"***");
        holder.seedName.setText(address.name);

        Wallet wallet = Store.getWallet(address);
        if(wallet!=null) {
            holder.extraValue.setVisibility(View.VISIBLE);
            String balanceText = IotaToText.convertRawIotaAmountToDisplayText(wallet.getBalanceDisplay(), true);
            holder.seedValue.setText(balanceText);
            Currency defcur=Store.getDefaultCurrency(context);
            Ticker ticker = Store.getTicker("IOTA:"+defcur);
            if(ticker!=null) {
                holder.extraValue.setText(ticker.getIotaValString(wallet.getBalanceDisplay())+" "+defcur);
            } else {
                holder.extraValue.setVisibility(View.GONE);
            }
        } else {
            holder.extraValue.setVisibility(View.GONE);
        }

        if (address.id.equals(Store.getCurrentSeed().id)) {
            holder.addressImage.setImageDrawable(UiManager.getDrawable(context,R.drawable.check_green));
            //holder.addressLabel.setPaintFlags(holder.addressLabel.getPaintFlags() | Paint.);
        } else if (!address.isdefault) {
            holder.addressImage.setImageDrawable(UiManager.getDrawable(context,R.drawable.ic_address));
            holder.addressImage.setColorFilter(ContextCompat.getColor(context, R.color.flatGreen));
        }
    }

    private Seeds.Seed getItem(int position) {
        return seeds.get(position + 1);
    }


    @Override
    public int getItemCount() {
        return seeds.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        boolean isDefault;
        Seeds.Seed seed;
        @BindView(R.id.item_wa_address)
        TextView addressLabel;
        @BindView(R.id.item_wa_address_image)
        ImageView addressImage;
        @BindView(R.id.item_wa_name)
        TextView seedName;
        @BindView(R.id.item_wa_seed_value)
        TextView seedValue;
        @BindView(R.id.item_wa_seed_rawvalue)
        TextView extraValue;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v -> {
                openDialog();
            });

            itemView.setOnLongClickListener(v -> {
                openDialog();
                return true;
            });

        }
        private void openDialog() {
            Bundle bundle = new Bundle();
            bundle.putString("seedid", seed.id);

            ChooseSeedItemDialog dialog = new ChooseSeedItemDialog();
            dialog.setArguments(bundle);

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    seeds = Store.getSeedList();
                    notifyDataSetChanged();
                }
            },new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    seeds = Store.getSeedList();
                    notifyDataSetChanged();
                }
            });
            dialog.show(((Activity) context).getFragmentManager(), null);
        }
    }
}
