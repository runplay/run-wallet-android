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
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

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
import jota.error.ArgumentException;

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
    @BindView(R.id.new_transfer_message_input)
    TextInputEditText messageEditText;
    @BindView(R.id.new_transfer_tag_input)
    TextInputEditText tagEditText;
    @BindView(R.id.new_transfer_address_text_input_layout)
    TextInputLayout addressEditTextInputLayout;
    @BindView(R.id.new_transfer_amount_text_input_layout)
    TextInputLayout amountEditTextInputLayout;
    @BindView(R.id.new_transfer_units_spinner)
    Spinner unitsSpinner;
    @BindView(R.id.new_transfer_get_qr)
    ImageView qrSelect;

    @BindView(R.id.new_transfer_to)
    View payToView;
    @BindView(R.id.new_transfer_message)
    View paySummary;
    @BindView(R.id.new_transfer_summary_value)
    TextView summaryValue;
    @BindView(R.id.new_transfer_summary_address)
    TextView summaryAddress;

    @BindView(R.id.new_transfer_from_addresses)
    LinearLayout fromAddresses;
    @BindView(R.id.new_transfer_remainder_address)
    LinearLayout remainderAddress;

    @BindView(R.id.new_transfer_send_fab_button)
    FloatingActionButton fab;
    @BindView(R.id.new_transfer_next)
    AppCompatButton next;

    @BindView(R.id.new_transfer_available_balance)
    TextView availableBalance;
    @BindView(R.id.new_transfer_pending_balance)
    TextView pendingBalance;
    @BindView(R.id.pig_locked)
    TextView pendingLabel;

    private PayPacket.AvailableBalances balances;


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
        newTransferToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        newTransferToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("cek", "home selected");
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
        qrSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openQRCodeScanner();
            }
        });
        fab.hide();
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });
        Wallet wallet=Store.getCurrentWallet();
        balances=PayPacket.calculateAvailableBalances();

        availableBalance.setText(IotaToText.convertRawIotaAmountToDisplayText(balances.available, false));
        //if(wallet.getAvailableBalance()>0) {
            availableBalance.setTextColor(B.getColor(getActivity(),R.color.green));
        //}
        pendingBalance.setText(IotaToText.convertRawIotaAmountToDisplayText(balances.locked, false));
        pendingBalance.setAlpha(0.4F);
        pendingLabel.setAlpha(0.5F);
        if(wallet.getBalancePendingIn()>0) {
            pendingBalance.setTextColor(B.getColor(getActivity(),R.color.green));
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        useSeed= Store.getCurrentSeed();
    }
    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    private void goNext() {
        if (!isValidAddress()) {
            addressEditTextInputLayout.setError(getString(R.string.messages_invalid_address));
        } else if (getAmount().isEmpty() || getAmount().equals("0")) {
            addressEditTextInputLayout.setError(getString(R.string.messages_enter_amount));

        } else if (balances!=null && balances.available < Long.parseLong(amountInSelectedUnit())) {
            amountEditTextInputLayout.setError(getString(R.string.messages_not_enough_balance));

        } else {

            PayPacket.createPayPacket(Long.parseLong(amountInSelectedUnit()),addressEditText.getText().toString());
            if(PayPacket.isValid()) {
                if(useSeed!=null) {
                    seedName.setText(useSeed.name);
                    seedAddress.setText(useSeed.getShortValue()+"***");
                }
                summaryValue.setText(IotaToText.convertRawIotaAmountToDisplayText(PayPacket.getValue(), false));
                Address payAddress = new Address(PayPacket.getPayTo().get(0),false);
                summaryAddress.setText(payAddress.getShortAddress()+"***");

                fillFromAddresses();
                fillBalanceAddress();


                paySummary.setVisibility(View.VISIBLE);
                payToView.setVisibility(View.GONE);
                fab.show();


            } else {
                amountEditTextInputLayout.setError(PayPacket.getError());
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
            addId.setPadding(2,2,2,2);
            addId.setTextColor(white);

            TextView addValue=new TextView(getActivity());
            addValue.setLayoutParams(param);
            addValue.setText(IotaToText.convertRawIotaAmountToDisplayText(address.getValue(), false));
            addValue.setTextColor(bgcolor);
            addValue.setTextSize(16F);
            addValue.setPadding(5,2,2,2);

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
        //for(Address address: PayPacket.getPayFrom()) {

            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.HORIZONTAL);

            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.setPadding(0,0,10,0);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            TextView addId = new TextView(getActivity());
            addId.setLayoutParams(param);
            addId.setText("a"+PayPacket.getRemainder().getIndexName());
            addId.setBackgroundColor(bgcolor);
            addId.setPadding(2,2,2,2);
            addId.setTextColor(white);

            TextView addValue=new TextView(getActivity());
            addValue.setLayoutParams(param);
            addValue.setText(IotaToText.convertRawIotaAmountToDisplayText(PayPacket.getRemainder() .getValue(), false));
            addValue.setTextColor(bgcolor);
            addValue.setTextSize(16F);
            addValue.setPadding(5,2,2,2);

            layout.addView(addId);
            layout.addView(addValue);
            remainderAddress.addView(layout);
            synchronized (remainderAddress) {
                remainderAddress.notifyAll();
            }

        //}
    }
    @OnClick(R.id.new_transfer_send_fab_button)
    public void onNewTransferSendFabClick(FloatingActionButton fab) {
        inputManager.hideSoftInputFromWindow(fab.getWindowToken(), 0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        addressEditTextInputLayout.setError(null);
        amountEditTextInputLayout.setError(null);

        //noinspection StatementWithEmptyBody
        if (!PayPacket.isValid()) {
            amountEditTextInputLayout.setError(PayPacket.getError());

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
                            AppService.sendNewTransfer(getActivity(),useSeed,getAddress(),
                                    amountInSelectedUnit()
                                    ,PayPacket.getPayFrom(),PayPacket.getRemainder()
                                    , getMessage(), getTaG());
                            //Store.addEmptyTempCurrentTransfer(getActivity(),getAddress(), Sf.toLong(amountInSelectedUnit()),getMessage(),getTag());
                            getActivity().onBackPressed();
                        }
                    });

            alertDialog.show();
        }
    }

    private String amountInSelectedUnit() {
        String inputAmount = amountEditText.getText().toString();
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
        /*
        switch (item.getItemId()) {
            case R.id.action_qr_code:
                openQRCodeScanner();
        }
        */
        return false;
    }

    private void openQRCodeScanner() {
        if (!PermissionRequestHelper.hasCameraPermission(getActivity())) {
            checkPermissionCamera();
        } else {
            //Fragment fragment = new QRScannerFragment();
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
        outState.putString(ADDRESS, getAddress());
        outState.putString(AMOUNT, getAmount());
        outState.putString(MESSAGE, getMessage());
        outState.putString(TAG, getTaG());
        outState.putInt(SPINNER_POISTION, unitsSpinner.getSelectedItemPosition());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            addressEditText.setText(savedInstanceState.getString(ADDRESS));
            amountEditText.setText(savedInstanceState.getString(AMOUNT));
            messageEditText.setText(savedInstanceState.getString(MESSAGE));
            tagEditText.setText(savedInstanceState.getString(TAG));
            unitsSpinner.setSelection(savedInstanceState.getInt(SPINNER_POISTION));
        }
    }

    private boolean isValidAddress() {
        String validAddress=addressEditText.getText().toString();
        try {
            if (Checksum.isAddressWithChecksum(validAddress)) {
                validAddress = Checksum.removeChecksum(validAddress);
            }
        } catch(Exception e) {}
        if(validAddress.length()==81 && validAddress.matches("^[A-Z9]+$")) {
            //Log.e("VALIDATE","GOOD: "+validAddress.length()+" -- "+validAddress.matches("^[A-Z9]+$"));
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
}