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
import android.content.DialogInterface;
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
import run.wallet.iota.model.Nodes;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.dialog.DialogHelper;

public class NodesListAdapter extends RecyclerView.Adapter<NodesListAdapter.NodeViewHolder> {

    private NodesListAdapter thisadapter;
    private final Activity context;
    private List<Nodes.Node> nodes;

    public NodesListAdapter(Activity context) {
        this.thisadapter=this;
        this.context = context;
        this.nodes = Store.getNodes();

        //Log.e("NODES","count: "+nodes.size());
    }
    private void reload() {
        this.nodes = Store.getNodes();
    }

    @Override
    public NodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nodelist, parent, false);
        //Log.e("NODESVIEW","create view holder");
        return new NodeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NodeViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        Nodes.Node node = getItem(adapterPosition);
        //Log.e("NODESVIEW","pos: "+adapterPosition);
        if (node != null) {


            Nodes.Node currentnode=Store.getNode();
            NodeInfoResponse currentInfo=Store.getNodeInfo();

            holder.nodeAddress.setText(node.getName());
            holder.lastSyncStatus.setImageResource(R.drawable.node_off);
            holder.statusView.setImageResource(R.drawable.node_off);
            Cal cal = new Cal(node.lastused);
            holder.lastDate.setText(cal.friendlyReadDate());
            holder.itemBackground.setBackgroundColor(B.getColor(context,R.color.white));
            holder.lastSyncStatus.setImageResource(R.drawable.node_off);
            if(node.deadcount>2) {
                holder.itemView.setBackgroundColor(B.getColor(context,R.color.grey));
                holder.lastDate.setText(R.string.menu_node_dead);
            } else {
                if (currentnode != null) {
                    if (currentnode.ip.equals(node.ip)) {
                        holder.itemBackground.setBackgroundColor(B.getColor(context,R.color.flatGreen));
                        holder.statusView.setImageResource(R.drawable.node_live);

                        if (currentInfo!=null && currentInfo.getSyncVal() >1) {

                            holder.lastDate.setText(context.getString(R.string.messages_not_fully_synced_yet));
                        }
                    }
                } else {
                    //Log.e("CNODe","is null");
                }
            }


            holder.syncValue.setText(node.syncVal+"");
            if(node.syncVal==0) {
                holder.lastSyncStatus.setImageResource(R.drawable.node_live);
            } else {
                holder.lastSyncStatus.setImageResource(R.drawable.node_tick_grey);
            }

            //holder.nodeName.setText(node.name);
            holder.nodePort.setText(""+node.port);
            holder.nodeProtocol.setText(node.protocol);
        }

    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    public Nodes.Node getItem(int position) {
        return nodes.get(position);
    }

    /*
    public void removeItem(Context context, int position) {
        TaskManager rt = new TaskManager(context);
        RemoveNeighborsRequest rnr = new RemoveNeighborsRequest(new String[]{Constants.UDP + neighbors.get(position).getAddress()});
        rt.startNewRequestTask(rnr);

        neighbors.remove(position);
        notifyItemRemoved(position);
    }
    */



    class NodeViewHolder extends RecyclerView.ViewHolder {
        //@BindView(R.id.item_node_name)
        //TextView nodeName;
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
        @BindView(R.id.item_node_background)
        LinearLayout itemBackground;

        private NodeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogHelper.showNodeListItemDialog(context,thisadapter,getAdapterPosition(),new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //Log.e("DISMISS","DIALOG");
                            reload();
                            AppService.refreshEvent();
                            notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }
}