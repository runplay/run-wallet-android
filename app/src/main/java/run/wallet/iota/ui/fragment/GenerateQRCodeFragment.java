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
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import run.wallet.iota.IOTA;
import run.wallet.R;
import run.wallet.iota.api.TaskManager;
import run.wallet.iota.api.requests.GetNewAddressRequest;
import run.wallet.iota.api.requests.SendTransferRequest;
import run.wallet.iota.api.responses.GetNewAddressResponse;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.QR;
import run.wallet.iota.model.QRCode;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.dialog.GeneratedQRCodeDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jota.error.ArgumentException;

import jota.utils.IotaUnits;

public class GenerateQRCodeFragment extends Fragment {

    private static final String ADDRESS = "address";
    private static final String AMOUNT = "amount";
    private static final String MESSAGE = "message";
    private static final String TAG = "tag";
    private static final String SPINNER_POISTION = "spinnerPosition";

    private InputMethodManager inputManager;

    @BindView(R.id.generate_qr_code_toolbar)
    Toolbar generateQrCodeToolbar;
    @BindView(R.id.generate_qr_code_address_input)
    TextInputEditText addressEditText;
    @BindView(R.id.generate_qr_code_amount_input)
    TextInputEditText amountEditText;
    @BindView(R.id.generate_qr_code_message_input)
    TextInputEditText messageEditText;
    @BindView(R.id.generate_qr_code_tag_input)
    TextInputEditText tagEditText;
    @BindView(R.id.generate_qr_code_address_input_layout)
    TextInputLayout addressEditTextInputLayout;
    @BindView(R.id.generate_qr_code_units_spinner)
    Spinner unitsSpinner;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generate_qr, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(generateQrCodeToolbar);

        Bundle bundle = getArguments();
        if (bundle != null) {
            QRCode qrCode = bundle.getParcelable(Constants.QRCODE);
            if (qrCode != null) {
                if (qrCode.getAddress() != null)
                    addressEditText.setText(qrCode.getAddress());
            }
        }
        //else
          //  generateNewAddress();

        initUnitsSpinner();
    }

    @OnClick(R.id.generate_qr_code_fab_button)
    public void onGenerateQrCodeClick(FloatingActionButton fab) {
        inputManager.hideSoftInputFromWindow(fab.getWindowToken(), 0);
        //reset errors
        addressEditTextInputLayout.setError(null);

        //noinspection StatementWithEmptyBody
        if (!isValidAddress()) {

        } else {

            QRCode qrCode = new QRCode();
            qrCode.setAddress(addressEditText.getText().toString());

            if (getAmount().isEmpty()) {
                qrCode.setAmount("");
            } else {
                qrCode.setAmount(amountInSelectedUnit());
            }

            qrCode.setMessage(messageEditText.getText().toString());
            qrCode.setTag(tagEditText.getText().toString());

            String json = new Gson().toJson(qrCode);

            //Bitmap bitmap = net.glxn.qrgen.android.QRCode.from(json).withSize(500, 500).bitmap();

            Bitmap bitmap = QR.generateImage(json,getActivity());

            Bundle bundle = new Bundle();
            bundle.putParcelable("bitmap", bitmap);
            GeneratedQRCodeDialog fragment = new GeneratedQRCodeDialog();
            fragment.setArguments(bundle);
            fragment.show(getActivity().getFragmentManager(), null);
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
/*
    private void generateNewAddress() {
        AppService.generateNewAddress(getActivity());
    }
*/
    private void attachNewAddress(String address) {
        AppService.sendNewTransfer(getActivity(), Store.getCurrentSeed(), address, "0", "", Constants.NEW_ADDRESS_TAG);
        //rt.startNewRequestTask(tir);
    }

    @Subscribe
    public void onEvent(GetNewAddressResponse gnar) {
        attachNewAddress(gnar.getAddresses().get(0));
        addressEditText.setText(gnar.getAddresses().get(0));
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
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
        String address = addressEditText.getText().toString();
        //try {
            //noinspection StatementWithEmptyBody
            //if (Checksum.isAddressWithoutChecksum(address)) {
            //}
        //} catch (ArgumentException e) {
            //addressEditTextInputLayout.setError(getString(R.string.messages_enter_txaddress_with_checksum));
            //return false;
        //}
        return true;
    }

    private String getAddress() {
        //try {
            return addressEditText.getText().toString();
        //}/ catch (ArgumentException e) {
        //    e.printStackTrace();
        //}
        //return "";
    }

    private String getAmount() {
        return amountEditText.getText().toString();
    }

    private String getMessage() {
        return messageEditText.getText().toString();
    }

    private String getTaG() {
            return tagEditText.getText().toString();
    }
}
