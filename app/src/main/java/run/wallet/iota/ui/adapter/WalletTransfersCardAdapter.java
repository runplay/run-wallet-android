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
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Cal;
import run.wallet.common.Sf;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Utils;

import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.TransferTransaction;
import run.wallet.iota.ui.TransferViewManager;
import run.wallet.iota.ui.dialog.WalletTransfersItemDialog;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jota.utils.IotaToText;

public class WalletTransfersCardAdapter extends RecyclerView.Adapter<TransferViewManager.ViewHolder> {

    private static WalletTransfersCardAdapter adapter;
    private final Context context;

    //private static WalletTransfersCardAdapter thisAdapter;
    private static List<Transfer> transfers=new ArrayList<>();
    private static Seeds.Seed seed;
    private static boolean isAutoNudge=false;
    public static String filterAddress;
    public static String filterAddressId;
    private static ViewPager viewPager;

    public static void goActions() {
        if(viewPager!=null) {
            viewPager.setCurrentItem(0);
        }
    }
    public static final String getFilterAddress() {
        return filterAddress;
    }
    public static final String getFilterAddressId() {
        return filterAddressId;
    }
    public static void setViewPager(ViewPager pager) {
        viewPager=pager;
    }
    public static void clear() {
        transfers.clear();
    }
    public WalletTransfersCardAdapter(Context context, boolean force) {
        adapter=this;
        this.context = context;
        load(context,force);
        //adapter.notifyDataSetChanged();

    }
    public static synchronized void load(Context context, boolean force) {
        if(force || seed==null || (Store.getCurrentSeed()!=null && !seed.id.equals(Store.getCurrentSeed().id)) || transfers.isEmpty()) {
            if(context!=null) {
                transfers.clear();
                transfers.addAll(Store.getTransfers());

                if (filterAddress != null) {
                    List<Transfer> filtered = new ArrayList<>();
                    for (Transfer t : transfers) {
                        if (t.getAddress().equals(filterAddress))
                            filtered.add(t);
                        else {
                            boolean add = false;
                            for (TransferTransaction tt : t.getTransactions()) {
                                if (tt.getAddress().equals(filterAddress))
                                    add = true;
                            }
                            if (add)
                                filtered.add(t);
                        }

                    }
                    transfers = filtered;
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

                if (!prefs.getBoolean(Constants.PREFERENCES_SHOW_CANCELLED, true)) {
                    List<Transfer> lesscancelled = new ArrayList<>();
                    for (Transfer transfer : transfers) {
                        if (!transfer.isMarkDoubleSpend())
                            lesscancelled.add(transfer);
                    }
                    transfers = lesscancelled;
                }
                if (!prefs.getBoolean(Constants.PREFERENCES_SHOW_ATTACH, true)) {
                    List<Transfer> lessattach = new ArrayList<>();
                    for (Transfer transfer : transfers) {
                        if (transfer.getValue() != 0
                                || (transfer.getValue() == 0 && !transfer.getTransactions().isEmpty()))
                            lessattach.add(transfer);
                    }
                    transfers = lessattach;
                }
                int nudges = Sf.toInt(prefs.getString(Constants.PREF_TRANSFER_NUDGE_ATTEMPTS, "" + Constants.PREF_TRANSFER_NUDGE_ATTEMPTS_VALUE));
                isAutoNudge = nudges == 0 ? false : true;
            }

        }
    }

    public static void setFilterAddress(String id,String address) {
        filterAddress=address;
        filterAddressId=id;

    }

    @Override
    public TransferViewManager.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_wallet_transfer, parent, false);
        return new TransferViewManager.ViewHolder(context,v);
    }

    private Transfer getItem(int position) {
        int index=position+1;
        if(index<transfers.size()) {
            return transfers.get(index);
        }
        return  null;
    }



    @Override
    public void onBindViewHolder(final TransferViewManager.ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        Transfer transfer = getItem(adapterPosition-1);
        if(transfer!=null) {
            boolean isFiltered=filterAddress==null?false:true;
            TransferViewManager.populateViewHolder(context,holder,transfer,isAutoNudge,adapterPosition,isFiltered);
            synchronized (holder) {
                holder.notifyAll();
            }
        } else {
            //Log.e("ADAPTER","has null value");
        }
    }

    @Override
    public int getItemCount() {
        return transfers.size();
    }


}