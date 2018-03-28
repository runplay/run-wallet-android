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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;

import run.wallet.R;
import run.wallet.common.Sf;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.QRCode;
import run.wallet.common.json.JSONException;
import run.wallet.common.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import run.wallet.iota.model.Store;

public class QRScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scannerView = new ZXingScannerView(getActivity());
        return scannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Store.setCurrentFragment(this.getClass());
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {

        QRCode qrCode = new QRCode();

        String strRes=String.valueOf(result);
        if(strRes!=null && !strRes.isEmpty()) {
            if(strRes.length()==81 || strRes.length()==90) {
                qrCode.setAddress(strRes);
            } else if(strRes.startsWith("iota:")) {
                strRes=strRes.replaceFirst("iota:","").replaceFirst("/","");
                if(strRes.contains("?")) {
                    String [] sp = strRes.split("\\?");
                    qrCode.setAddress(sp[0]);
                    try {
                        if (sp.length > 1) {
                            String[] params = sp[1].split("&");
                            if (params != null && params.length > 0) {
                                for (int i = 0; i < params.length; i++) {
                                    if (params[i].startsWith("amount=")) {
                                        qrCode.setAmount(params[i].replaceFirst("amount=", ""));
                                    } else if (params[i].startsWith("value=")) {
                                        qrCode.setAmount(params[i].replaceFirst("value=", ""));
                                    } else if (params[i].startsWith("tag=")) {
                                        qrCode.setTag(params[i].replaceFirst("tag=", ""));
                                    } else if (params[i].startsWith("message=")) {
                                        qrCode.setMessage(params[i].replaceFirst("message=", ""));
                                    }
                                }
                            }
                        }
                    } catch(Exception e) {}
                } else {
                    qrCode.setAddress(strRes);
                }

            } else {
                JSONObject json = new JSONObject(strRes);
                qrCode.setAddress(json.optString("address"));
                if(!json.optString("amount").isEmpty()) {
                    qrCode.setAmount(json.optLong("amount")+"");
                } else if(!json.optString("value").isEmpty()) {
                    qrCode.setAmount(json.optLong("value")+"");
                }
                if(!json.optString("message").isEmpty())
                    qrCode.setMessage(json.optString("message"));
                else if(!json.optString("msg").isEmpty())
                    qrCode.setMessage(json.optString("msg"));

                qrCode.setTag(json.optString("tag"));
            }
        }



        //remove all fragment from backStack, right, 2 times
        //getActivity().onBackPressed();
        //getActivity().onBackPressed();

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.QRCODE, qrCode);

        FragmentManager fm = getActivity().getFragmentManager();
        FragmentTransaction tr = fm.beginTransaction();
        //fm.popBackStack();
        Fragment fragment = new SnTrFragment();
        fragment.setArguments(bundle);
        tr.replace(R.id.container, fragment, null).commit();
        scannerView.invalidate();



    }

}
