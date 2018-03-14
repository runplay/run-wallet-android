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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import run.wallet.R;
import run.wallet.common.B;
import run.wallet.iota.api.TaskManager;
import run.wallet.iota.api.requests.RemoveNeighborsRequest;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Neighbor;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import run.wallet.iota.model.Store;

public class NeighborsListAdapter extends RecyclerView.Adapter<NeighborsListAdapter.NeighborViewHolder> {

    private final Context context;
    private List<Neighbor> neighbors;

    public NeighborsListAdapter(Context context) {
        this.context = context;
        this.neighbors = Store.getNeighbours();
        if(neighbors==null)
            neighbors=new ArrayList<>();
    }

    @Override
    public NeighborViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_neighborlist, parent, false);
        return new NeighborViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NeighborViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        Neighbor neighbor = getItem(adapterPosition);
        if (neighbor != null) {
            holder.ipAddressTextView.setText(neighbor.getAddress());

            if (neighbor.getNumberOfAllTransactions() != null && neighbor.getNumberOfInvalidTransactions() != null && neighbor.getNumberOfNewTransactions() != null) {
                holder.numberOfAllTransactionsTextView.setText(context.getString(R.string.all_transactions) + " " + neighbor.getNumberOfAllTransactions());
                holder.numberOfInvalidTransactionsTextView.setText(context.getString(R.string.invalid_transactions) + " " + neighbor.getNumberOfInvalidTransactions());
                holder.numberOfNewTransactionsTextView.setText(context.getString(R.string.new_transactions) + " " + neighbor.getNumberOfNewTransactions());
                holder.numberOfRandomTransactionRequestsTextView.setText(context.getString(R.string.random_transaction_requests) + " " + neighbor.getNumberOfRandomTransactionRequests());
                holder.numberOfSentTransactionsTextView.setText(context.getString(R.string.sent_transactions) + " " + neighbor.getNumberOfSentTransactions());
                holder.connectionTypeTextView.setText(neighbor.getConnectionType());

            } else {
                holder.numberOfAllTransactionsTextView.setText(context.getString(R.string.all_transactions) + " " + context.getString(R.string.na));
                holder.numberOfInvalidTransactionsTextView.setText(context.getString(R.string.invalid_transactions) + " " + context.getString(R.string.na));
                holder.numberOfNewTransactionsTextView.setText(context.getString(R.string.new_transactions) + " " + context.getString(R.string.na));
                holder.numberOfRandomTransactionRequestsTextView.setText(context.getString(R.string.random_transaction_requests) + " " + context.getString(R.string.na));
                holder.numberOfSentTransactionsTextView.setText(context.getString(R.string.sent_transactions) + " " + context.getString(R.string.na));
                holder.connectionTypeTextView.setText( context.getString(R.string.na));
            }

        }

    }

    @Override
    public int getItemCount() {
        return neighbors.size();
    }

    private Neighbor getItem(int position) {
        return neighbors.get(position);
    }


    public void setAdapterList(List<Neighbor> neighbors) {
        this.neighbors = neighbors;
        //notifyDataSetChanged();
    }

    public void filter(final List<Neighbor> neighbors, String searchText) {
        final String sText = searchText.toLowerCase();

        new Thread(() -> {
            final List<Neighbor> filteredNeighborList = new ArrayList<>();
            for (Neighbor neighbor : neighbors) {
                final String text = neighbor.getAddress().toLowerCase();
                if (text.contains(sText)) {
                    filteredNeighborList.add(neighbor);
                }
            }
            ((Activity) context).runOnUiThread(() -> setAdapterList(filteredNeighborList));
        }).start();
    }

    class NeighborViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_neighbor_address)
        TextView ipAddressTextView;
        @BindView(R.id.item_neighbor_number_of_all_transactions)
        TextView numberOfAllTransactionsTextView;
        @BindView(R.id.item_neighbor_number_of_invalid_transactions)
        TextView numberOfInvalidTransactionsTextView;
        @BindView(R.id.item_neighbor_number_of_new_transactions)
        TextView numberOfNewTransactionsTextView;
        @BindView(R.id.item_neighbor_number_of_random_transaction_request)
        TextView numberOfRandomTransactionRequestsTextView;
        @BindView(R.id.item_neighbor_number_of_sent_transactions)
        TextView numberOfSentTransactionsTextView;
        @BindView(R.id.item_neighbor_connection_type)
        TextView connectionTypeTextView;
        @BindView(R.id.item_neighbor_status)
        ImageView statusView;
        @BindView(R.id.neighbour_head)
        View head;

        private NeighborViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}