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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import run.wallet.R;
import run.wallet.common.ActivityMan;
import run.wallet.iota.helper.Cal;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.TransferTransaction;
import run.wallet.iota.ui.TransferViewManager;

public class PendingCancelledFragment extends Fragment {

    private static final String PACKAGE_WEBVIEW = "com.google.android.webview";

    @BindView(R.id.cancelled_toolbar)
    Toolbar toolbar;
    @BindView(R.id.item_wt_balance)
    TextView balance;
    @BindView(R.id.item_wt_balance_third)
    TextView balanceThird;
    @BindView(R.id.item_wt_balance_unit)
    TextView balanceUnit;
    @BindView(R.id.item_wt_alternate_value)
    TextView alternativeValueLabel;
    @BindView(R.id.item_wt_alternate_time)
    TextView getAlternativeValueTime;
    @BindView(R.id.item_wt_address)
    TextView addressLabel;
    @BindView(R.id.item_wt_message)
    TextView messageLabel;
    @BindView(R.id.item_wt_tag)
    TextView tagLabel;
    @BindView(R.id.item_wt_time)
    TextView timeLabel;
    @BindView(R.id.item_wt_hash)
    TextView hashLabel;
    @BindView(R.id.item_wt_persistence)
    TextView persistenceLabel;
    @BindView(R.id.item_wt_expand_button)
    ImageButton expandButton;
    @BindView(R.id.item_wt_expand_layout)
    ExpandableRelativeLayout expandableLayout;
    @BindView(R.id.item_wt_image)
    ImageView imgTran;
    @BindView(R.id.item_wt_card)
    CardView card;
    @BindView(R.id.item_wt_confirmed)
    TextView confirmCheck;
    @BindView(R.id.item_wt_mstone)
    TextView mstoneCount;


    @BindView(R.id.item_wt_timestatus)
    View timestatus;
    @BindView(R.id.transferTransactions)
    LinearLayout transferTransactions;

    @BindView(R.id.cancelled_breakdown)
    LinearLayout breakdown;
    @BindView(R.id.cancelled_www)
    TextView cancelledSites;

    @BindView(R.id.cancelled_hash)
    TextView cancelledHash;

    @BindView(R.id.cancelled_copy_clip)
    TextView copyClip;
    @BindView(R.id.item_wt_filtered)
    LinearLayout filtered;
    @BindView(R.id.item_wt_filtered_balance)
    TextView fBalance;
    @BindView(R.id.item_wt_filtered_balance_unit)
    TextView fUnit;
    @BindView(R.id.item_wt_filtered_balance_third)
    TextView fThird;


    private Unbinder unbinder;
    private View view;

    public static final String[] sitesName={"iotasear.ch"};
    public static final String[] sitesAddress={"https://iotasear.ch/address/"};
    public static final String[] sitesHash={"https://iotasear.ch/hash/"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cancelled, container, false);
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
                //Log.d("cek", "home selected");
                getActivity().onBackPressed();
            }
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        if(Store.getCacheTransfer()==null) {
            getActivity().onBackPressed();
        } else {
            View v = view.findViewById(R.id.transfer_card);
            TransferViewManager.populateViewHolder(getActivity(),new TransferViewManager.ViewHolder(getActivity(),v),Store.getCacheTransfer(),false,-1,false);

            List<TransferTransaction> transactions = new ArrayList<>();
            transactions.addAll(Store.getCacheTransfer().getTransactions());
            transactions.addAll(Store.getCacheTransfer().getOtherTransactions());

            TransferViewManager.populateTransferTransactionOuts(getActivity(),breakdown,transactions,Store.getCacheTransfer());
            cancelledHash.setText(Store.getCacheTransfer().getHash());
            cancelledHash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityMan.openAndroidBrowserUrl(getActivity(),sitesHash[0]+Store.getCacheTransfer().getHash());
                }
            });
            copyClip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Transfer transfer=Store.getCacheTransfer();
                    if(transfer!=null) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

                        StringBuilder sb=new StringBuilder();

                        sb.append("IOTA Transfer\n\n");
                        sb.append("time: "+ Cal.getCal(transfer.getTimestamp()).getDatabaseDate());
                        sb.append("\nhash: "+transfer.getHash());
                        sb.append("\n\nTotal: "+transfer.getValue());
                        sb.append("\nStatus: "+(transfer.isCompleted()?"Completed":"Pending"));
                        sb.append("\nmilestone: "+transfer.getMilestone());
                        sb.append("\n\nAddress actions\n");
                        for(TransferTransaction tran: transfer.getTransactions()) {
                            sb.append(tran.getValue());
                            sb.append("    ");
                            sb.append(tran.getAddress());
                        }
                        for(TransferTransaction tran: transfer.getOtherTransactions()) {
                            sb.append(tran.getValue());
                            sb.append("    ");
                            sb.append(tran.getAddress());
                        }

                        sb.append("\n\nUsing run IOTA wallet, Android app http://iota.runplay.com");

                        ClipData clip = ClipData.newPlainText(getActivity().getString(R.string.seed), sb.toString());
                        clipboard.setPrimaryClip(clip);
                        Snackbar.make(getView(),getString(R.string.text_cancelled_copied),Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            cancelledSites.setText(" "+sitesName[0]);
        }

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
