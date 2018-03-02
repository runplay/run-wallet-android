package run.wallet.iota.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jota.utils.IotaToText;
import run.wallet.R;
import run.wallet.common.ActivityMan;
import run.wallet.common.B;
import run.wallet.common.Cal;
import run.wallet.iota.api.responses.NodeInfoResponse;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.Utils;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Transfer;
import run.wallet.iota.model.TransferTransaction;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.dialog.WalletTransfersItemDialog;
import run.wallet.iota.ui.fragment.PendingCancelledFragment;

/**
 * Created by coops on 27/01/18.
 */

public class TransferViewManager {

    private static final LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams main = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams mainouts = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams param4 = new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams param5 = new LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT);
    //private static final LinearLayout.LayoutParams balance = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private static final SparseBooleanArray expandState = new SparseBooleanArray();

    public static  class ViewHolder extends RecyclerView.ViewHolder {


        Transfer transfer;
        boolean isConfirmed;
        boolean isCancelled;
        boolean isAddressDouble;
        long value;
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
        @BindView(R.id.transferTransactionsOtherView)
        View otherAddressView;
        @BindView(R.id.transferTransactionsOther)
        LinearLayout otherAddressLayout;

        @BindView(R.id.item_wt_filtered_balance)
        TextView fBalance;
        @BindView(R.id.item_wt_filtered_balance_unit)
        TextView fUnit;
        @BindView(R.id.item_wt_filtered_balance_third)
        TextView fThird;
        @BindView(R.id.item_wt_timestatus)
        View timestatus;
        @BindView(R.id.transferTransactions)
        LinearLayout transferTransactions;

        @BindView(R.id.item_wt_filtered)
        LinearLayout filtered;

        public ViewHolder(Context context,View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            expandableLayout.collapse();
            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();

                bundle.putString("hash", transfer.getHash());

                WalletTransfersItemDialog dialog = new WalletTransfersItemDialog();
                dialog.setArguments(bundle);
                dialog.show(((Activity) context).getFragmentManager(), null);
            });

            itemView.setOnLongClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("hash", transfer.getHash());
                WalletTransfersItemDialog dialog = new WalletTransfersItemDialog();
                dialog.setArguments(bundle);
                dialog.show(((Activity) context).getFragmentManager(), null);
                /*
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Toast.makeText(v.getContext(), context.getString(R.string.messages_not_yet_implemented), Toast.LENGTH_SHORT).show();

                }
                */
                return true;
            });

            expandButton.setOnClickListener(view -> expandableLayout.toggle());

            expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
                @Override
                public void onPreOpen() {
                    expandButton.setImageResource(R.drawable.ic_expand_less);
                    expandState.put(getAdapterPosition(), true);
                }

                @Override
                public void onPreClose() {
                    expandButton.setImageResource(R.drawable.ic_expand_more);
                    expandState.put(getAdapterPosition(), false);
                }
            });
        }

    }

    public static final void populateViewHolder(Context context, ViewHolder holder, Transfer transfer, boolean isAutoNudge, int adapterPosition,boolean filtered) {
        String faddress=WalletTransfersCardAdapter.getFilterAddress();

        if(faddress!=null && transfer.isCompleted()) {
            ((LinearLayout.LayoutParams)(holder.card).getLayoutParams()).setMarginStart(70);

            long value = 0;
            for(TransferTransaction t: transfer.getTransactions()) {
                if(faddress.equals(t.getAddress()))
                    value+=t.getValue();
            }
            if(transfer.getTransactions().isEmpty()) {

            } else {
                IotaToText.IotaDisplayData fdata = IotaToText.getIotaDisplayData(value);
                holder.filtered.setVisibility(View.VISIBLE);
                holder.fBalance.setText((value > 0 ? "+" : "") + fdata.value);
                holder.fUnit.setText(fdata.unit);
                holder.fThird.setText(fdata.thirdDecimal);
                holder.fUnit.setTextColor(ContextCompat.getColor(context, AppTheme.getPrimary()));
                if (value > 0) {
                    holder.fBalance.setTextColor(ContextCompat.getColor(context, R.color.green));
                    holder.fThird.setTextColor(ContextCompat.getColor(context, R.color.green));
                } else {
                    holder.fBalance.setTextColor(ContextCompat.getColor(context, R.color.flatRed));
                    holder.fThird.setTextColor(ContextCompat.getColor(context, R.color.flatRed));
                }
            }
        } else {
            holder.filtered.setVisibility(View.GONE);
        }
        IotaToText.IotaDisplayData data = IotaToText.getIotaDisplayData(transfer.getValue());
        holder.transfer=transfer;
        holder.balance.setText(data.value);
        holder.balanceThird.setText(data.thirdDecimal);
        holder.balanceUnit.setText(data.unit);
        holder.addressLabel.setText(transfer.getAddress());
        holder.messageLabel.setText(TextUtils.isEmpty(transfer.getMessage()) ? "" : formatMessage(context,transfer.getMessage()));
        holder.tagLabel.setText(transfer.getTag());
        holder.timeLabel.setText(Utils.timeStampToDate(transfer.getTimestamp()));
        holder.hashLabel.setText(transfer.getHash());
        holder.isCancelled=transfer.isMarkDoubleSpend();
        holder.isAddressDouble=transfer.isMarkDoubleAddress();
        holder.value=transfer.getValue();
        int persist = R.string.card_label_persistence_no;
        if(transfer.isCompleted())
            persist=R.string.card_label_persistence_yes;


        holder.persistenceLabel.setText(context.getResources().getString(persist));
        holder.isConfirmed = transfer.getPersistence()!=null?transfer.getPersistence():false;
        holder.confirmCheck.setVisibility(View.GONE);
        holder.alternativeValueLabel.setText("");
        holder.timestatus.setVisibility(View.VISIBLE);
        holder.balance.setTextColor(ContextCompat.getColor(context, R.color.grey));
        holder.balanceThird.setTextColor(ContextCompat.getColor(context, R.color.grey));
        holder.balanceUnit.setTextColor(ContextCompat.getColor(context, R.color.grey));
        holder.alternativeValueLabel.setTextColor(ContextCompat.getColor(context, R.color.grey));
        holder.balance.setPaintFlags(0);
        if(transfer.getPersistence()!=null && transfer.getPersistence().booleanValue()) {
            //holder.card.setCardBackgroundColor(B.getColor(context,R.color.cardview_light_background));
            holder.card.setAlpha(1F);
        } else {
            if((System.currentTimeMillis()-600000)>transfer.getTimestamp()) {
                holder.card.setAlpha(0.6F);
            } else {
                holder.card.setAlpha(0.8F);
            }
            //holder.card.setCardBackgroundColor(B.getColor(context,R.color.cardview_dark_background));

        }
        holder.mstoneCount.setVisibility(View.GONE);
        holder.getAlternativeValueTime.setText(Cal.friendlyReadDate(new Cal(transfer.getTimestamp())));
        holder.getAlternativeValueTime.setCompoundDrawables(null,null,null,null);
        if (transfer.getValue() == 0 && transfer.getTransactions().isEmpty()) {

            if(transfer.getTag().endsWith("NUDGE9")) {
                holder.alternativeValueLabel.setText(context.getString(R.string.info_nudge));
                holder.imgTran.setImageResource(R.drawable.nudge_orange);
            } else {
                holder.alternativeValueLabel.setText(context.getString(R.string.attached_address));
                holder.imgTran.setImageResource(R.drawable.tran_orange);
            }
        } else if (!transfer.isCompleted()) {
            if(isAutoNudge) {
                holder.imgTran.setImageResource(R.drawable.ic_replay_orange);
            } else {
                holder.imgTran.setImageResource(R.drawable.ic_replay_grey);
            }

            if(transfer.getValue()<0) {
                holder.balance.setTextColor(ContextCompat.getColor(context, R.color.flatRed));
                holder.balanceThird.setTextColor(ContextCompat.getColor(context, R.color.flatRed));
                holder.balanceUnit.setTextColor(ContextCompat.getColor(context, AppTheme.getPrimary()));
            } else if(transfer.isInternal()) {
                holder.alternativeValueLabel.setText(context.getString(R.string.card_label_internal));
                holder.imgTran.setImageResource(R.drawable.tran_green);
            }
            holder.confirmCheck.setText(transfer.getNudgeCount()+"");
            if(transfer.isMarkDoubleSpend()) {
                holder.imgTran.setImageResource(R.drawable.ic_replay_grey);
                holder.alternativeValueLabel.setText(context.getString(R.string.label_cancelled));
                holder.balance.setPaintFlags(holder.addressLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else if(transfer.isMarkDoubleAddress()) {
                holder.imgTran.setImageResource(R.drawable.ic_replay_grey);
                holder.alternativeValueLabel.setText(context.getString(R.string.label_address_used));
                holder.balance.setPaintFlags(holder.addressLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {

                if(transfer.getValue()<0) {
                    if(isAutoNudge) {
                        holder.confirmCheck.setVisibility(View.VISIBLE);
                        holder.confirmCheck.setBackgroundResource(R.drawable.ic_replay_orange_alpha);
                    }
                    holder.alternativeValueLabel.setText(context.getString(R.string.card_label_pending_out));
                } else {
                    if(isAutoNudge) {
                        holder.confirmCheck.setVisibility(View.VISIBLE);
                        holder.confirmCheck.setBackgroundResource(R.drawable.ic_replay_orange_alpha);
                    }
                    holder.alternativeValueLabel.setText(context.getString(R.string.card_label_pending_in));
                }

            }
        } else if(transfer.getValue()==0) {

            holder.alternativeValueLabel.setText(context.getString(R.string.card_label_internal));
            holder.imgTran.setImageResource(R.drawable.tran_green);
            if(transfer.isCompleted()) {
                holder.confirmCheck.setBackgroundResource(R.drawable.check_green);
                holder.confirmCheck.setVisibility(View.VISIBLE);
            }

        }  else if (transfer.getValue() > 0) {
            holder.confirmCheck.setVisibility(View.VISIBLE);
            holder.confirmCheck.setText("");
            holder.confirmCheck.setBackgroundResource(R.drawable.check_green);
            holder.alternativeValueLabel.setText(context.getString(R.string.card_label_persistence_yes));
            holder.getAlternativeValueTime.setCompoundDrawables(B.getDrawable(context,R.drawable.check_green),null,null,null);
            holder.imgTran.setImageResource(R.drawable.ic_iota_in);
            holder.balance.setTextColor(ContextCompat.getColor(context, R.color.green));
            holder.balanceThird.setTextColor(ContextCompat.getColor(context, R.color.green));
            holder.balanceUnit.setTextColor(ContextCompat.getColor(context, AppTheme.getPrimary()));
        } else {
            holder.confirmCheck.setVisibility(View.VISIBLE);
            holder.confirmCheck.setText("");
            holder.confirmCheck.setBackgroundResource(R.drawable.check_green);
            holder.alternativeValueLabel.setText(context.getString(R.string.card_label_persistence_yes));
            holder.getAlternativeValueTime.setCompoundDrawables(B.getDrawable(context,R.drawable.check),null,null,null);
            holder.imgTran.setImageResource(R.drawable.ic_iota_out);
            holder.balance.setTextColor(ContextCompat.getColor(context, R.color.flatRed));
            holder.balanceThird.setTextColor(ContextCompat.getColor(context, R.color.flatRed));
            holder.balanceUnit.setTextColor(ContextCompat.getColor(context, AppTheme.getPrimary()));
        }

        TransferViewManager.populateTransferTransactions(context,holder.transferTransactions,transfer.getTransactions(),false);

        if(!transfer.getOtherTransactions().isEmpty()) {
            holder.otherAddressView.setVisibility(View.VISIBLE);
            holder.otherAddressLayout.setVisibility(View.VISIBLE);
            TransferViewManager.populateTransferTransactions(context,holder.otherAddressLayout,transfer.getOtherTransactions(),true);
        } else {
            //holder.otherAddressLayout.removeAllViews();
            holder.otherAddressView.setVisibility(View.GONE);
        }

        if(adapterPosition<0) {

        } else {
            holder.expandableLayout.setExpanded(expandState.get(adapterPosition));
            holder.expandableLayout.invalidate();
        }

    }

    private static final String TXT_NUDGE_START="RUN9NUDGE9HASH9";
    private static final String TXT_NUDGE_END="9END";
    public static String formatMessage(Context context,String message) {
        if(message!=null) {
            if(message.startsWith(TXT_NUDGE_START)) {
                message=message.replace(TXT_NUDGE_START,"");
                message=context.getString(R.string.info_nudge)+" ("+context.getString(R.string.hash)+"):\n"+message.substring(0,message.length()-TXT_NUDGE_END.length());

            }
        }
        return message;
    }

    private static void populateTransferTransactions(Context context, LinearLayout uselayout, List<TransferTransaction> transactions, boolean isOtherTransactions) {
        uselayout.removeAllViews();

        int bgcolor= B.getColor(context, AppTheme.getPrimary());
        int bglight=B.getColor(context,R.color.colorLight);
        int white=B.getColor(context,R.color.white);
        int red=B.getColor(context,R.color.flatRed);
        int green=B.getColor(context,R.color.green);

        main.weight=1;
        param.setMargins(8,4,8,4);
        param2.setMargins(8,4,8,4);
        param3.setMargins(0,4,8,4);
        for(TransferTransaction trans: transactions) {

            LinearLayout layout = new LinearLayout(context);
            layout.setBackgroundColor(bglight);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(main);
            layout.setPadding(2, 2, 2, 2);
            layout.canScrollHorizontally(View.LAYOUT_DIRECTION_LTR);

            TextView addValue = new TextView(context);
            addValue.setLayoutParams(param2);

            addValue.setText(IotaToText.convertRawIotaAmountToDisplayText(trans.getValue(), true));
            addValue.setTextSize(16F);
            addValue.setTypeface(null, Typeface.BOLD);
            if (trans.getValue() < 0) {
                addValue.setTextColor(red);
            } else {
                addValue.setTextColor(green);
            }
            addValue.setPadding(5, 2, 2, 2);
            addValue.setSingleLine();

            TextView addAddress = new TextView(context);
            addAddress.setLayoutParams(param3);
            addAddress.setText(trans.getAddress());
            addAddress.setTextColor(bgcolor);
            addAddress.setTextSize(12F);
            addAddress.setPadding(5, 2, 2, 2);
            addAddress.setSingleLine();

            if (!isOtherTransactions) {
                List<Address> allAddresses = Store.getAddresses();
                Address address = Store.isAlreadyAddress(trans.getAddress(), allAddresses);
                if (address != null) {
                    TextView addId = new TextView(context);
                    addId.setLayoutParams(param);
                    addId.setText("a" + address.getIndexName());
                    addId.setBackgroundColor(bgcolor);
                    addId.setPadding(2, 2, 2, 2);
                    addId.setTextColor(white);
                    layout.addView(addId);
                }
            }

            layout.addView(addValue);
            layout.addView(addAddress);

            uselayout.addView(layout);
        }
    }

    public static void populateTransferTransactionOuts(Activity context, LinearLayout uselayout, List<TransferTransaction> transactions, Transfer transfer) {
        uselayout.removeAllViews();

        int bgcolor = B.getColor(context, AppTheme.getPrimary());
        int bglight = B.getColor(context, R.color.colorLight);
        int white = B.getColor(context, R.color.white);
        int red = B.getColor(context, R.color.flatRed);
        int green = B.getColor(context, R.color.green);
        List<Address> allAddresses = Store.getAddresses();

        main.weight = 1;
        param.setMargins(8, 4, 8, 4);
        param2.setMargins(8, 4, 8, 4);
        param3.setMargins(0, 4, 8, 4);
        for (TransferTransaction trans : transactions) {

            Address address = Store.isAlreadyAddress(trans.getAddress(), allAddresses);
            //if (address != null) {
                LinearLayout layout = new LinearLayout(context);
                layout.setBackgroundColor(bglight);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setLayoutParams(mainouts);
                layout.setPadding(2, 6, 2, 6);
                layout.canScrollHorizontally(View.LAYOUT_DIRECTION_LTR);

                TextView balance = new TextView(context);
                balance.setLayoutParams(param5);
                balance.setText(IotaToText.convertRawIotaAmountToDisplayText(trans.getPayFromAddressZero(),true));
                balance.setPadding(2, 2, 2, 2);

                if(address==null) {
                    if(trans.getPayFromAddressZero()==0 && trans.getValue()<0) {
                        balance.setBackgroundColor(red);
                        balance.setTextColor(white);
                    } else {
                        balance.setTextColor(white);
                    }
                } else {
                    if(trans.getValue()<0 && trans.getPayFromAddressZero()==0) {
                        balance.setBackgroundColor(red);
                        balance.setTextColor(white);
                    }
                    if(trans.getPayFromAddressZero()>0)
                        balance.setTextColor(green);
                    else
                        balance.setTextColor(white);
                }


                TextView addId = new TextView(context);
                addId.setLayoutParams(param4);
                if(address!=null) {
                    addId.setBackgroundColor(bgcolor);
                    addId.setText("a" + address.getIndexName());
                } else {
                    addId.setText(" ");
                }

                addId.setPadding(2, 2, 2, 2);
                addId.setTextColor(white);
                //addId.setMa

                TextView addValue = new TextView(context);
                addValue.setLayoutParams(param3);
                addValue.setText(IotaToText.convertRawIotaAmountToDisplayText(trans.getValue(), true));
                addValue.setTextSize(20F);
                addValue.setGravity(Gravity.RIGHT);
                addValue.setTypeface(null, Typeface.BOLD);
                if (trans.getValue() < 0) {
                    addValue.setTextColor(red);
                } else {
                    addValue.setTextColor(green);
                }
                addValue.setPadding(5, 2, 2, 2);
                addValue.setSingleLine();

                TextView addAddress = new TextView(context);
                addAddress.setLayoutParams(param2);
                addAddress.setText(trans.getAddress());
                addAddress.setTextColor(bgcolor);
                addAddress.setTextSize(12F);
                addAddress.setPadding(5, 2, 2, 2);
                addAddress.setSingleLine();

                addAddress.setOnClickListener(new OpenClick(context,trans.getAddress()));

                layout.addView(balance);
                layout.addView(addId);
                layout.addView(addValue);
                layout.addView(addAddress);

                uselayout.addView(layout);
            //} else {
                //Log.e("NULL-ADD","Null adrress: "+trans.getAddress()+" - "+transfer.getValue()+" - hash: "+transfer.getHash());
            //}
            //uselayout.notify();
        }
    }
    private static class OpenClick implements View.OnClickListener {
        Activity context;
        String address;
        private OpenClick(Activity context, String address) {
            this.address=address;
            this.context=context;
        }
        @Override
        public void onClick(View v) {
            ActivityMan.openAndroidBrowserUrl(context, PendingCancelledFragment.sitesAddress[0]+address);
        }

    }
}
