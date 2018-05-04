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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Cal;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Nodes;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.fragment.NetworkNodesAddFragment;

public class AddNodesListAdapter extends RecyclerView.Adapter<AddNodesListAdapter.NodeViewHolder> {

    private final Context context;
    private List<Nodes.Node> nodes;
    NetworkNodesAddFragment fragment;

    public AddNodesListAdapter(Context context,NetworkNodesAddFragment frag) {
        this.context = context;
        this.fragment=frag;
        this.nodes = Store.getOtherNodes(context);
    }

    @Override
    public NodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nodelist, parent, false);
        return new NodeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NodeViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        Nodes.Node node = getItem(adapterPosition);
        if (node != null) {
            if(node.ip.contains(".runplay.com") || node.ip.contains(".runpg.com")) {
                holder.nodeAddress.setText(Constants.DISPLAY_RUNIOTA_NODE_NAME+position+Constants.DISPLAY_RUNIOTA_NODE_NAME_END);
            } else {
                holder.nodeAddress.setText(node.ip);
            }
            holder.extras.setVisibility(View.GONE);
            Nodes.Node currentnode=Store.getNode();
            NodeInfoResponse currentInfo=Store.getNodeInfo();
            holder.lastSyncStatus.setImageResource(R.drawable.node_off);
            holder.statusView.setImageResource(R.drawable.node_off);
            Cal cal = new Cal(node.lastused);
            holder.lastDate.setText(cal.friendlyReadDate());
            holder.itemView.setBackgroundColor(B.getColor(context,R.color.white));
            holder.nodePort.setText(""+node.port);
            holder.nodeProtocol.setText(node.protocol);
        }

    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    private Nodes.Node getItem(int position) {
        return nodes.get(position);
    }


    class NodeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_node_address)
        TextView nodeAddress;
        @BindView(R.id.item_node_port)
        TextView nodePort;
        @BindView(R.id.item_node_protocol)
        TextView nodeProtocol;
        @BindView(R.id.item_node_status)
        ImageView statusView;
        @BindView(R.id.item_node_last_date)
        TextView lastDate;
        @BindView(R.id.item_node_status_last_count)
        TextView syncValue;
        @BindView(R.id.item_node_status_last)
        ImageView lastSyncStatus;
        @BindView(R.id.item_node_extras)
        LinearLayout extras;

        private NodeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.setNodeDetails(nodeAddress.getText().toString(),nodePort.getText().toString(),nodeProtocol.getText().toString());
                }
            });
        }
    }
}