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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import jota.utils.Checksum;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Sf;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.PermissionRequestHelper;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.PayPacket;
import run.wallet.iota.model.QRCode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import jota.utils.IotaUnitConverter;
import jota.utils.IotaToText;
import jota.utils.IotaUnits;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.model.Wallet;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;

public class SnTrFragment extends Fragment {

    private Seeds.Seed useSeed;
    private static final String ADDRESS = "address";
    private static final String AMOUNT = "amount";
    private static final String MESSAGE = "message";
    private static final String TAG = "tag";
    private static final String SPINNER_POISTION = "spinnerPosition";
    private InputMethodManager inputManager;

    @BindView(R.id.new_transfer_toolbar)
    Toolbar newTransferToolbar;
    @BindView(R.id.new_transfer_seed_name)
    TextView seedName;
    @BindView(R.id.new_transfer_seed_quip)
    TextView seedAddress;
    @BindView(R.id.new_transfer_amount_input)
    TextInputEditText amountEditText;
    @BindView(R.id.new_transfer_address_input)
    TextInputEditText addressEditText;
    @BindView(R.id.new_transfer_scroll)
    ScrollView scrollView;
    @BindView(R.id.new_transfer_message_text_input_layout)
    TextInputLayout messageLayout;
    @BindView(R.id.new_transfer_tag_input_layout)
    TextInputLayout tagLayout;
    @BindView(R.id.remainder_pod)
    View remainderPod;
    @BindView(R.id.new_transfer_add_address_pod)
    View addAddressPod;
    @BindView(R.id.new_transfer_show_multi_balance)
    View multiBalance;
    @BindView(R.id.new_transfer_message_input)
    TextInputEditText messageEditText;
    @BindView(R.id.new_transfer_tag_input)
    TextInputEditText tagEditText;
    @BindView(R.id.new_transfer_address_text_input_layout)
    TextInputLayout addressEditTextInputLayout;
    @BindView(R.id.new_transfer_import_warn)
    TextView warnImport;
    @BindView(R.id.new_transfer_units_spinner)
    Spinner unitsSpinner;
    @BindView(R.id.new_transfer_get_qr)
    View qrSelect;
    @BindView(R.id.new_transfer_keyboard)
    View keyboard;

    @BindView(R.id.new_transfer_summary)
    View walletView;
    @BindView(R.id.new_transfer_show_details)
    View paySummary;
    @BindView(R.id.new_transfer_clear)
    Button btnClear;

    @BindView(R.id.new_transfer_from_addresses)
    LinearLayout fromAddresses;
    @BindView(R.id.new_transfer_remainder_address)
    LinearLayout remainderAddress;

    @BindView(R.id.new_transfer_next)
    AppCompatButton next;

    @BindView(R.id.new_transfer_available_balance)
    TextView availableBalance;
    @BindView(R.id.new_transfer_pending_balance)
    TextView pendingBalance;
    @BindView(R.id.pig_locked)
    TextView pendingLabel;

    @BindView(R.id.new_transfer_add)
    AppCompatButton addPayment;

    @BindView(R.id.new_transfer_address_list)
    LinearLayout listAddresses;
    @BindView(R.id.new_transfer_address_confirm)
    ImageView checkAddress;
    @BindView(R.id.new_transfer_value_confirm)
    ImageView checkValue;

    @BindView(R.id.new_transfer_sub_value)
    TextView subValue;
    @BindView(R.id.new_transfer_sub_third)
    TextView subThird;
    @BindView(R.id.new_transfer_sub_unit)
    TextView subUnit;

    @BindView(R.id.new_transfer_add_message)
    ImageView addMessage;
    @BindView(R.id.new_transfer_add_tag)
    TextView addTag;

    @BindView(R.id.new_transfer_details_pod)
    LinearLayout podDetails;
    @BindView(R.id.new_transfer_add_pod)
    LinearLayout podAdd;

    @BindView(R.id.new_transfer_back)
    AppCompatButton btnBack;
    @BindView(R.id.new_transfer_pay_now)
    AppCompatButton btnPayNow;
    @BindView(R.id.new_transfer_paste)
    View btnPaste;

    private int editPayToAddressIndex=-1;
    private PayPacket.AvailableBalances balances;
    //private List<PayPacket.PayTo> paytoAddresses=new ArrayList();

    private void removePayTo(String address) {
        PayPacket.PayTo rem=null;
        for(PayPacket.PayTo pk: PayPacket.getPayTo()) {
            if(pk.address.equals(address)) {
                rem=pk;
                break;
            }
        }
        if(rem!=null) {
            editPayToAddressIndex=-1;
            PayPacket.removePayTo(rem);
            PayPacket.updatePayPacket();
            populatePaytoAddresses(true);
            if(PayPacket.getPayTo().size()>4) {
                addAddressPod.setVisibility(View.GONE);
            } else {
                addAddressPod.setVisibility(View.VISIBLE);
            }
        }
    }
    private void editPayTo(String address) {
        for(int i=0; i<PayPacket.getPayTo().size(); i++) {
            PayPacket.PayTo pk= PayPacket.getPayTo().get(i);
            if(pk.address.equals(address)) {
                editPayToAddressIndex=i;
                addressEditText.setText(pk.address);
                amountEditText.setText(pk.value+"");
            }
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_transfer, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(newTransferToolbar);
        setHasOptionsMenu(false);

        //paytoAddresses.clear();
        newTransferToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        newTransferToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        initUnitsSpinner();

        Bundle bundle = getArguments();
        if (getArguments() != null) {
            QRCode qrCode = bundle.getParcelable(Constants.QRCODE);

            if (qrCode != null) {

                if (qrCode.getAddress() != null)
                    addressEditText.setText(qrCode.getAddress());


                if (qrCode.getAmount() != null && !qrCode.getAmount().isEmpty()) {
                    Long amount = Long.parseLong((qrCode.getAmount()));
                    IotaUnits unit = IotaUnitConverter.findOptimalIotaUnitToDisplay(amount);
                    String amountText = IotaUnitConverter.createAmountDisplayText(IotaUnitConverter.convertAmountTo(amount, unit), unit, false);
                    amountEditText.setText(amountText);
                    unitsSpinner.setSelection(toSpinnerItemIndex(unit));
                }

                if (qrCode.getMessage() != null)
                    messageEditText.setText(qrCode.getMessage());

                if (qrCode.getTag() != null)
                    tagEditText.setText(qrCode.getTag());

            }
        }
        addMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageLayout.getVisibility()==View.GONE) {
                    messageLayout.setVisibility(View.VISIBLE);
                } else {
                    messageLayout.setVisibility(View.GONE);
                }
            }
        });
        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tagLayout.getVisibility()==View.GONE) {
                    tagLayout.setVisibility(View.VISIBLE);
                } else {
                    tagLayout.setVisibility(View.GONE);
                }
            }
        });
        qrSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openQRCodeScanner();
            }
        });


        addPayment.setEnabled(false);
        addPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAdress();
            }
        });

        next.setEnabled(false);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });
        Wallet wallet=Store.getCurrentWallet();
        balances=PayPacket.calculateAvailableBalances();

        availableBalance.setText(IotaToText.convertRawIotaAmountToDisplayText(balances.available, true));
        //if(wallet.getAvailableBalance()>0) {
        availableBalance.setTextColor(B.getColor(getActivity(),R.color.green));
        //}
        pendingBalance.setText(IotaToText.convertRawIotaAmountToDisplayText(balances.locked, true));
        pendingBalance.setAlpha(0.4F);
        pendingLabel.setAlpha(0.5F);
        if(wallet.getBalancePendingIn()>0) {
            pendingBalance.setTextColor(B.getColor(getActivity(),R.color.green));
        }

        addressEditText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isLongClicked=true;
                return false;
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressEditText.setText("");
                amountEditText.setText("");
            }
        });
        addressEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isLongClicked) {
                    isLongClicked=false;
                    return false;
                }
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if(imm.isActive(addressEditText)) {
                    return false;
                }
                int inType = addressEditText.getInputType();
                addressEditText.setInputType(InputType.TYPE_NULL);
                addressEditText.onTouchEvent(event);
                addressEditText.setInputType(inType);

                return true; // consume touch event


            }

        });
        addressEditText.addTextChangedListener(inputWatcher);
        amountEditText.addTextChangedListener(inputWatcher);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackBtn();
            }
        });
        keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiManager.setKeyboard(getActivity(),addressEditText,true);
                scroller.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(View.FOCUS_DOWN);
                                addressEditText.requestFocus();
                            }
                        });
                    }
                }, 500);
            }
        });
        btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData cd=clipboard.getPrimaryClip();
                if (cd != null && cd.getItemCount() > 0) {
                    addressEditText.setText(cd.getItemAt(0).coerceToText(getActivity()));
                }
            }
        });

    }
    private boolean isLongClicked=false;
    private Handler scroller = new Handler();
    private boolean hasClipboardAddress() {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData cd=clipboard.getPrimaryClip();
        if (cd != null && cd.getItemCount() > 0) {
            String address=cd.getItemAt(0).coerceToText(getActivity()).toString();

            //Log.e("CLIPB","has add? : "+isValidAddress(address)+" -- "+address);
            return isValidAddress(address);
        }
        return false;
    }
    private String removeChecksum(String address) {
        try {
            if (Checksum.isAddressWithChecksum(address)) {
                address=Checksum.removeChecksum(address);
            }
        } catch (Exception e) {}
        return address;
    }
    private void goBackBtn() {
        paySummary.setVisibility(View.GONE);
        walletView.setVisibility(View.VISIBLE);
        podAdd.setVisibility(View.VISIBLE);
        podDetails.setVisibility(View.GONE);
        if(PayPacket.getPayTo().size()>4) {
            addAddressPod.setVisibility(View.GONE);
        } else {
            addAddressPod.setVisibility(View.VISIBLE);
        }
        populatePaytoAddresses(true);
    }
    private TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(checkSetMultiAddressImport(s)) {

            } else {
                long value=Long.valueOf(amountInSelectedUnit());
                if(value>0 && balances.available>=value && isValidAddress()) {
                    next.setEnabled(true);
                    next.setAlpha(1F);
                    addPayment.setEnabled(true);
                    addPayment.setAlpha(1F);
                } else {
                    if(PayPacket.getPayTo().isEmpty()) {
                        next.setEnabled(false);
                        next.setAlpha(0.5F);
                        addPayment.setEnabled(false);
                        addPayment.setAlpha(0.5F);
                    } else {
                        next.setEnabled(true);
                        next.setAlpha(1F);
                        addPayment.setEnabled(false);
                        addPayment.setAlpha(0.5F);
                    }
                }
                if(isValidAddress()) {
                    if(addressEditTextInputLayout.isFocused())
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    checkAddress.setAlpha(1F);
                    addressEditTextInputLayout.setError(null);
                } else {
                    checkAddress.setAlpha(0.2F);
                }
                if(value>0 && balances.available>=value) {
                    //amountEditText.setBackgroundColor(B.getColor(getActivity(),R.color.white));
                    checkValue.setAlpha(1F);
                } else {
                    checkValue.setAlpha(0.2F);
                }
            }
        }
    };
    private boolean checkSetMultiAddressImport(Editable s) {
        String use=s.toString();
        if(use.length()>90) {
            List<PayPacket.PayTo> imported=new ArrayList<>();
            // probably multi address paste
            use=use.replace("\r","");
            String delim=",";
            if(use.contains("|"))
                delim="|";
            else if(use.contains("-"))
                delim="-";
            String[] lines = use.split("\n");
            boolean didAdd=false;
            for(String line: lines) {
                long value=0L;
                String address=null;
                if(line.length()>90) {
                    String[] sp = line.split(delim);
                    for(String tmp: sp) {
                        tmp=tmp.replaceAll("\"","");
                        if(tmp.length()>80) {
                            address=removeChecksum(tmp);
                        } else if(tmp.length()<20) {
                            value=Sf.toLong(tmp);
                        }
                    }

                }
                if(address!=null && address.length()>80) {
                    PayPacket.PayTo pt = new PayPacket.PayTo(value,address);
                    PayPacket.addPayTo(pt);
                    didAdd=true;
                }
            }
            if(didAdd) {
                addressEditText.setText("");
                populatePaytoAddresses(true);
                PayPacket.updatePayPacket();
                if (!PayPacket.isValid()) {
                    warnImport.setVisibility(View.VISIBLE);
                    warnImport.setText(getString(R.string.message_fix_import));
                }
            }

        }
        return false;
    }
    @Override
    public void onResume() {
        super.onResume();
        Store.setCurrentFragment(this.getClass());
        useSeed= Store.getCurrentSeed();
        if(hasClipboardAddress()) {
            btnPaste.setAlpha(1f);
        } else {
            btnPaste.setAlpha(0.5F);
        }
        populatePaytoAddresses(true);
        /*
        if(PayPacket.getPayTo().isEmpty()) {
            amountEditText.setBackgroundColor(B.getColor(getActivity(),R.color.flatGreenAlpha));
            addressEditText.setBackgroundColor(B.getColor(getActivity(),R.color.flatGreenAlpha));
        } else {
            amountEditText.setBackgroundColor(B.getColor(getActivity(),R.color.white));
            addressEditText.setBackgroundColor(B.getColor(getActivity(),R.color.white));
        }
        */
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    private void addAdress() {
        addressEditTextInputLayout.setError(null);
        if (!isValidAddress()) {
            addressEditTextInputLayout.setError(getString(R.string.messages_invalid_address));
        } else if (getAmount().isEmpty() || getAmount().equals("0")) {
            addressEditTextInputLayout.setError(getString(R.string.messages_enter_amount));
        } else if (balances!=null && balances.available < Long.parseLong(amountInSelectedUnit())) {
            addressEditTextInputLayout.setError(getString(R.string.messages_not_enough_balance));
        } else {
            boolean has=false;
            String address=removeChecksum(addressEditText.getText().toString());
            if(editPayToAddressIndex==-1) {
                for(PayPacket.PayTo pt: PayPacket.getPayTo()) {
                    if(pt.address.equals(address)) {
                        has=true;
                        break;
                    }
                }
                if(!has) {
                    PayPacket.addPayTo(new PayPacket.PayTo(Long.parseLong(amountInSelectedUnit()), address));
                    PayPacket.updatePayPacket();
                    if (PayPacket.isValid()) {
                        if(PayPacket.getPayTo().size()>4) {
                            addAddressPod.setVisibility(View.GONE);
                        } else {
                            addAddressPod.setVisibility(View.VISIBLE);
                        }
                        addressEditText.setText("");
                    } else {
                        PayPacket.removePayTo(PayPacket.getPayTo().size()-1);
                        addressEditTextInputLayout.setError(PayPacket.getError());

                    }

                } else {
                    addressEditTextInputLayout.setError(getString(R.string.info_address_already));
                }
            } else {

                PayPacket.PayTo pt= PayPacket.getPayTo().get(editPayToAddressIndex);
                pt.address=removeChecksum(addressEditText.getText().toString());
                pt.value=Long.parseLong(amountInSelectedUnit());

                PayPacket.updatePayPacket();
                if (PayPacket.isValid()) {
                    addressEditText.setText("");
                    editPayToAddressIndex=-1;
                } else {
                    addressEditTextInputLayout.setError(PayPacket.getError());
                }


            }
            inputManager.hideSoftInputFromWindow(getView().getWindowToken(),0);
            populatePaytoAddresses(true);
        }
    }
    private void redrawTotalToPay() {
        IotaToText.IotaDisplayData dd=IotaToText.getIotaDisplayData(PayPacket.getTotalToPay());
        subValue.setText(dd.value);
        subThird.setText(dd.thirdDecimal);
        subUnit.setText(dd.unit);
        if(Sf.toLong(dd.value)>0) {
            multiBalance.setVisibility(View.VISIBLE);
        } else {
            multiBalance.setVisibility(View.GONE);
        }

    }
    private void goNext() {

        if(PayPacket.getPayTo().isEmpty()) {
            boolean validated=true;
            if (!isValidAddress()) {
                addressEditTextInputLayout.setError(getString(R.string.messages_invalid_address));
                validated=false;
            } else if (getAmount().isEmpty() || getAmount().equals("0")) {
                addressEditTextInputLayout.setError(getString(R.string.messages_enter_amount));
                validated=false;
            } else if (balances!=null && balances.available < Long.parseLong(amountInSelectedUnit())) {
                addressEditTextInputLayout.setError(getString(R.string.messages_not_enough_balance));
                validated=false;
            }
            if(validated) {
                addAdress();
                addressEditText.setText("");
                amountEditText.setText("0");
            }
        } else {
            if(isValidAddress() && !getAmount().isEmpty() && !getAmount().equals("0")) {
                addAdress();
                addressEditText.setText("");
                amountEditText.setText("0");
                populatePaytoAddresses(true);
            }

        }
        if(!PayPacket.getPayTo().isEmpty()) {
            inputManager.hideSoftInputFromWindow(getView().getWindowToken(),0);
            PayPacket.updatePayPacket();
            if(PayPacket.isValid()) {


                if(useSeed!=null) {
                    seedName.setText(useSeed.name);
                }
                paySummary.setVisibility(View.VISIBLE);
                walletView.setVisibility(View.GONE);
                podAdd.setVisibility(View.GONE);
                podDetails.setVisibility(View.VISIBLE);
                addAddressPod.setVisibility(View.GONE);

                populatePaytoAddresses(false);
                fillFromAddresses();
                fillBalanceAddress();

            } else {
                addressEditTextInputLayout.setError(PayPacket.getError());
            }
        }

    }
    private void fillFromAddresses() {
        fromAddresses.removeAllViews();
        int bgcolor=B.getColor(getActivity(),R.color.colorPrimary);
        int white=B.getColor(getActivity(),R.color.white);
        //Log.e("FROM-ADD","add: "+PayPacket.getPayFrom().size());
        for(Address address: PayPacket.getPayFrom()) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.setPadding(0,0,10,0);
            layout.canScrollHorizontally(View.LAYOUT_DIRECTION_LTR);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            TextView addId = new TextView(getActivity());
            addId.setLayoutParams(param);
            addId.setText("a"+address.getIndexName());
            addId.setBackgroundColor(bgcolor);
            addId.setTypeface(addId.getTypeface(), Typeface.BOLD);
            addId.setPadding(6,2,6,2);
            addId.setTextColor(white);

            TextView addValue=new TextView(getActivity());
            addValue.setLayoutParams(param);
            addValue.setText(IotaToText.convertRawIotaAmountToDisplayText(address.getValue(), true));
            addValue.setTextColor(bgcolor);
            addValue.setTextSize(16F);
            addValue.setTypeface(addId.getTypeface(), Typeface.BOLD);
            addValue.setPadding(10,2,2,2);

            layout.addView(addId);
            layout.addView(addValue);

            fromAddresses.addView(layout);

        }
        synchronized (fromAddresses) {
            fromAddresses.notifyAll();
        }

    }
    private void fillBalanceAddress() {
        remainderAddress.removeAllViews();
        int bgcolor=B.getColor(getActivity(),R.color.colorPrimary);
        int white=B.getColor(getActivity(),R.color.white);
        int green=B.getColor(getActivity(),R.color.green);
        long fromAddTotal=0L;
        for(Address add: PayPacket.getPayFrom()) {
            fromAddTotal+=add.getValue();
        }

        if(PayPacket.getTotalToPay()==fromAddTotal) {
            remainderPod.setVisibility(View.GONE);
        } else {
            //for(Address address: PayPacket.getPayFrom()) {
            remainderPod.setVisibility(View.VISIBLE);
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.HORIZONTAL);

            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.setPadding(0, 0, 10, 0);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            TextView addId = new TextView(getActivity());
            addId.setLayoutParams(param);
            addId.setText("a" + PayPacket.getRemainder().getIndexName());
            addId.setBackgroundColor(bgcolor);
            addId.setTypeface(addId.getTypeface(), Typeface.BOLD);
            addId.setPadding(6, 2, 6, 2);
            addId.setPadding(2, 2, 2, 2);
            addId.setTextColor(white);

            TextView remainderValue = new TextView(getActivity());
            remainderValue.setLayoutParams(param);
            remainderValue.setText("+"+IotaToText.convertRawIotaAmountToDisplayText(fromAddTotal-PayPacket.getTotalToPay(), true));
            remainderValue.setTextColor(green);
            remainderValue.setTextSize(16F);
            remainderValue.setTypeface(addId.getTypeface(), Typeface.BOLD);
            remainderValue.setPadding(10, 2, 2, 2);

            TextView text = new TextView(getActivity());
            text.setLayoutParams(param);
            text.setText(" > ");
            text.setTextSize(16F);
            text.setPadding(10, 2, 10, 2);

            layout.addView(remainderValue);
            layout.addView(text);
            layout.addView(addId);
            remainderAddress.addView(layout);
            synchronized (remainderAddress) {
                remainderAddress.notifyAll();
            }
        }
        //}
    }
    @OnClick(R.id.new_transfer_pay_now)
    public void onBtnPayNowClick() {
        addressEditTextInputLayout.setError(null);
        addressEditTextInputLayout.setError(null);

        //noinspection StatementWithEmptyBody
        if (!PayPacket.isValid()) {
            addressEditTextInputLayout.setError(PayPacket.getError());

        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.message_confirm_transfer)
                    .setCancelable(false)
                    .setPositiveButton(R.string.buttons_ok, null)
                    .setNegativeButton(R.string.buttons_cancel, null)
                    .create();

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.buttons_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AppService.sendNewTransfer(getActivity(),useSeed,PayPacket.getPayTo()
                                    ,PayPacket.getPayFrom(),PayPacket.getRemainder()
                                    , getMessage(), getTaG());
                            PayPacket.clear();
                            //Store.addEmptyTempCurrentTransfer(getActivity(),getAddress(), Sf.toLong(amountInSelectedUnit()),getMessage(),getTag());
                            getActivity().onBackPressed();
                        }
                    });

            alertDialog.show();
        }
    }

    private String amountInSelectedUnit() {
        String inputAmount = amountEditText.getText().toString();
        if(inputAmount.isEmpty())
            inputAmount="0";
        IotaUnits unit = toIotaUnit(unitsSpinner.getSelectedItemPosition());
        Long iota = Long.parseLong(inputAmount) * (long) Math.pow(10, unit.getValue());
        return iota.toString();
    }

    private IotaUnits toIotaUnit(int unitSpinnerItemIndex) {
        IotaUnits iotaUnits;

        switch (unitSpinnerItemIndex) {
            case 0:
                iotaUnits = IotaUnits.IOTA;
                break;
            case 1:
                iotaUnits = IotaUnits.KILO_IOTA;
                break;
            case 2:
                iotaUnits = IotaUnits.MEGA_IOTA;
                break;
            case 3:
                iotaUnits = IotaUnits.GIGA_IOTA;
                break;
            case 4:
                iotaUnits = IotaUnits.TERA_IOTA;
                break;
            case 5:
                iotaUnits = IotaUnits.PETA_IOTA;
                break;
            default:
                iotaUnits = IotaUnits.IOTA;
                break;
        }

        return iotaUnits;
    }

    private int toSpinnerItemIndex(IotaUnits unit) {
        int iotaUnits;

        if (unit == IotaUnits.IOTA) {
            iotaUnits = 0;
        } else if (unit == IotaUnits.KILO_IOTA) {
            iotaUnits = 1;
        } else if (unit == IotaUnits.MEGA_IOTA) {
            iotaUnits = 2;
        } else if (unit == IotaUnits.GIGA_IOTA) {
            iotaUnits = 3;
        } else if (unit == IotaUnits.TERA_IOTA) {
            iotaUnits = 4;
        } else if (unit == IotaUnits.PETA_IOTA) {
            iotaUnits = 5;
        } else {
            iotaUnits = 0;
        }

        return iotaUnits;
    }

    private void initUnitsSpinner() {


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_iota, getResources().getStringArray(R.array.listIotaUnits));
        adapter.setDropDownViewResource(R.layout.spinner_iota_open);
        unitsSpinner.setAdapter(adapter);
        unitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.new_transfer_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return false;
    }

    private void openQRCodeScanner() {
        if (!PermissionRequestHelper.hasCameraPermission(getActivity())) {
            checkPermissionCamera();
        } else {
            UiManager.openFragment(getActivity(),QRScannerFragment.class);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_CAMERA_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openQRCodeScanner();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissionCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {

                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        Constants.REQUEST_CAMERA_PERMISSION);

            } else {

                //Camera permissions have not been granted yet so request them directly
                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        Constants.REQUEST_CAMERA_PERMISSION);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    private boolean isValidAddress() {
        return isValidAddress(addressEditText.getText().toString());
    }
    private boolean isValidAddress(String address) {
        try {
            if (Checksum.isAddressWithChecksum(address)) {
                address = Checksum.removeChecksum(address);
            }
        } catch(Exception e) {}
        if(address.length()==81 && address.matches("^[A-Z9]+$")) {
            //Log.e("VALIDATE","GOOD: "+validAddress.length()+" -- "+validAddress.matches("^[A-Z9]+$"));
            //addressEditText.setBackgroundColor(B.getColor(getActivity(),R.color.white));
            return true;
        }
        return false;
    }

    private String getAddress() {
        return addressEditText.getText().toString();
    }

    private String getAmount() {
        return amountEditText.getText().toString();
    }

    private String getMessage() {
        String msg=messageEditText.getText().toString();
        msg=msg.replace(" ","9");
        msg=msg.toUpperCase();

        return msg;
    }

    private String getTaG() {
        String tag=tagEditText.getText().toString();
        if (tag.isEmpty()) {
            return Constants.NEW_TRANSFER_TAG;
        } else if (tag.length() < 27) {
            tag=tag.toUpperCase();
            return StringUtils.rightPad(tag, 27, '9');
        } else {
            tag=tag.toUpperCase();
            return tag;
        }
    }
    private static final LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams main = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams mainouts = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);

    private static final LinearLayout.LayoutParams param4 = new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static final LinearLayout.LayoutParams param5 = new LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT);

    private void populatePaytoAddresses(boolean showButtons) {

        listAddresses.removeAllViews();
        Context context=getActivity();
        int bgcolor= B.getColor(context, R.color.colorPrimary);
        int bglight=B.getColor(context,R.color.colorLight);
        int green=B.getColor(context,R.color.green);

        param.setMargins(16,6,16,6);
        param2.setMargins(8,4,8,4);
        param3.setMargins(0,4,8,4);
        param3.weight=1;
        for(PayPacket.PayTo payto: PayPacket.getPayTo()) {

            LinearLayout layout = new LinearLayout(context);
            layout.setBackgroundColor(bglight);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(main);
            layout.setPadding(20, 2, 20, 2);

            TextView addValue = new TextView(context);
            addValue.setLayoutParams(param2);
            addValue.setText(IotaToText.convertRawIotaAmountToDisplayText(payto.value, true));
            addValue.setTextSize(16F);
            addValue.setTypeface(null, Typeface.BOLD);
            addValue.setTextColor(green);

            addValue.setPadding(5, 2, 2, 2);
            addValue.setSingleLine();

            TextView addAddress = new TextView(context);
            addAddress.setLayoutParams(param3);
            addAddress.setText(payto.address);
            addAddress.setTextColor(bgcolor);
            addAddress.setTextSize(12F);
            addAddress.setPadding(5, 2, 2, 2);
            addAddress.setSingleLine();

            layout.addView(addValue);
            layout.addView(addAddress);
            if(showButtons) {
                ImageView edit = new ImageView(context);
                edit.setLayoutParams(param);

                edit.setImageResource(R.drawable.edit);
                edit.setTag(payto.address);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editPayTo((String) v.getTag());
                    }
                });

                ImageView remove = new ImageView(context);
                remove.setLayoutParams(param);
                remove.setImageResource(R.drawable.ic_remove_circle);
                remove.setTag(payto.address);
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removePayTo((String) v.getTag());
                        populatePaytoAddresses(true);
                    }
                });
                layout.addView(remove);
                layout.addView(edit);
            }



            listAddresses.addView(layout);

        }
        synchronized (listAddresses) {
            listAddresses.notifyAll();
        }
        redrawTotalToPay();
        //uselayout.notify();
    }

}