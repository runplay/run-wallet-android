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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.google.zxing.Result;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.Unbinder;
import jota.utils.SeedRandomGenerator;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.helper.SeedValidator;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.dialog.CopySeedDialog;
import run.wallet.iota.ui.dialog.NoDescDialog;

public class ChooseSeedAddFragment extends Fragment {

    private static final String SEED = "seed";
    private static Activity activity;
    @BindView(R.id.add_seed_toolbar)
    Toolbar addSeedToolbar;

    @BindView(R.id.seed_login_seed_text_input_layout)
    TextInputLayout seedEditTextLayout;

    static TextInputEditText seedEditText;

    @BindView(R.id.seed_add_gen_pod)
    LinearLayout genPod;
    @BindView(R.id.seed_gen_holder)
    LinearLayout numberPickerHolder;
    @BindView(R.id.add_seed_copy)
    Button btnCopy;
    @BindView(R.id.btn_qr)
    Button btnQr;
    @BindView(R.id.seed_add_scroll_left)
    ImageButton scrollLeft;
    @BindView(R.id.seed_add_scroll_right)
    ImageButton scrollRight;
    @BindView(R.id.seed_add_scroll_view)
    HorizontalScrollView scrollView;

    static View screenQr;
    static View screenAdd;

    private String generatedSeed;

    private Unbinder unbinder;

    private static View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_choose_add, container, false);
        view.setBackgroundColor(B.getColor(getActivity(), AppTheme.getSecondary()));
        unbinder = ButterKnife.bind(this, view);
        screenQr=view.findViewById(R.id.qr_screen);
        screenAdd=view.findViewById(R.id.add_screen);
        seedEditText=view.findViewById(R.id.seed_login_seed_input);
        activity=getActivity();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(addSeedToolbar);
        setHasOptionsMenu(false);
        addSeedToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        addSeedToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(seedEditText.getWindowToken(),0);
                getActivity().onBackPressed();
            }
        });
        scrollLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int scrollby =scrollView.getScrollX()-300;
                scrollby=scrollby<0?0:scrollby;
                scrollView.setScrollX(scrollby);
            }
        });
        scrollRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.setScrollX(scrollView.getScrollX()+300);
            }
        });
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CopySeed();
            }
        });
        btnQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasCameraPermission(activity)) {
                    screenQr.setVisibility(View.VISIBLE);
                    screenAdd.setVisibility(View.GONE);
                    openQrScanner(getActivity());
                } else {
                    requestPermissionCamera();
                }


            }
        });
        seedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(SeedValidator.isSeedValid(getActivity(),s.toString())==null) {
                    btnCopy.setAlpha(1f);
                    btnCopy.setEnabled(true);
                } else {
                    btnCopy.setAlpha(0.3f);
                    btnCopy.setEnabled(false);
                }
            }
        });
        btnCopy.setAlpha(0.3f);
        btnCopy.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Store.setCurrentFragment(this.getClass());
        generatedSeed=null;
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @OnClick(R.id.seed_add_button)
    public void onSeedLoginClick() {
        addSeedToStore();
    }

    @OnClick(R.id.seed_add_generate_seed)
    public void onSeedLoginGenerateSeedClick() {
        generatedSeed = SeedRandomGenerator.generateNewSeed();
        seedEditText.setText(generatedSeed);
        genPod.setVisibility(View.VISIBLE);

        drawCutomise();
    }

    private void CopySeed() {
        Bundle bundle = new Bundle();
        bundle.putString("generatedSeed", seedEditTextLayout.getEditText().toString());
        CopySeedDialog dialog = new CopySeedDialog();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), null);
    }
    private String pickerValues = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9";
    private String[] pickerArray= pickerValues.split("(?!^)");

    private void drawCutomise() {
        numberPickerHolder.removeAllViews();
        if(generatedSeed!=null) {
            char[] asArray = generatedSeed.toCharArray();
            for (int i = 0; i < asArray.length; i++) {
                char c = asArray[i];
                NumberPicker num = new NumberPicker(getActivity());
                num.setMinValue(0);
                num.setMaxValue(26);
                num.setDisplayedValues(pickerArray);
                num.setValue(pickerValues.indexOf(c));
                num.setClickable(false);
                num.setTag(Integer.valueOf(i));
                num.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        try {
                            Integer index = (Integer) picker.getTag();
                            char[] asArray = generatedSeed.toCharArray();
                            asArray[index] = pickerArray[newVal].charAt(0);
                            generatedSeed = new String(asArray);
                            seedEditText.setText(generatedSeed);
                        } catch(Exception e){}
                    }
                });
                numberPickerHolder.addView(num);
            }
        }
    }

    @OnEditorAction(R.id.seed_login_seed_input)
    public boolean onSeedLoginSeedInputEditorAction(int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            addSeedToStore();
        }
        return true;
    }

    private void addSeedToStore() {
        if(Store.getSeedList().size()>= Constants.WALLET_MAX_ALLOW) {
            Snackbar.make(getView(), R.string.max_seeds, Snackbar.LENGTH_LONG).show();
            return;
        }
        String seed = seedEditText.getText().toString();


        if (seed.isEmpty()) {
            seedEditTextLayout.setError(getString(R.string.messages_empty_seed));
            if (seedEditTextLayout.getError() != null)
                return;
        }
        String badSeed=SeedValidator.isSeedValid(getActivity(),seed);
        if(badSeed!=null) {
            Snackbar.make(getView(),badSeed, Snackbar.LENGTH_LONG).show();
            return;
        }
        List<Seeds.Seed> seeds=Store.getSeedList();
        for(Seeds.Seed tmp: seeds) {
            if(seed.equals(String.valueOf(Store.getSeedRaw(getActivity(),tmp)))) {
                Snackbar.make(getView(), R.string.message_seed_duplicate, Snackbar.LENGTH_LONG).show();
                return;

            }
        }
        seed=SeedValidator.getSeed(seed);
        if(!Store.isLoggedIn()) {
            login(seed);
        } else {
            addSeed(seed);
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(seedEditText.getWindowToken(), 0);
            getActivity().onBackPressed();
        }
    }
    private void login(String seed) {

        Bundle bundle = new Bundle();
        bundle.putString("seed", seed);
        if(generatedSeed!=null && generatedSeed.equals(seed)) {
            bundle.putBoolean("isgen", true);
        } else {
            bundle.putBoolean("isgen", false);
        }
        NoDescDialog encryptSeedDialog = new NoDescDialog();
        encryptSeedDialog.setArguments(bundle);
        encryptSeedDialog.show(getActivity().getFragmentManager(), null);

    }

    private void addSeed(String seed) {
        boolean isgen = false;
        if(generatedSeed!=null && generatedSeed.equals(seed))
            isgen=true;
        //Log.e("adding","seed: "+isgen);
        Store.addSeed(getActivity(),seed.toCharArray(),getSeedName(),false,isgen);
    }

    private String getSeedName() {
        return getString(R.string.wallet)+"-"+Store.getSeedList().size();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {

        if(seedEditText!=null && seedEditText.getText()!=null) {
            outState.putString(SEED, seedEditText.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            seedEditText.setText(savedInstanceState.getString(SEED));
        }
    }

    public static boolean hasCameraPermission(Context context) {
        int result = activity.checkCallingOrSelfPermission(Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionCamera() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {

                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        Constants.REQUEST_CAMERA_PERMISSION_SEED);

            } else {

                //Camera permissions have not been granted yet so request them directly
                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        Constants.REQUEST_CAMERA_PERMISSION_SEED);
            }
        }
    }
    private static boolean openQrScanner(Activity activity) {
        if(activity!=null) {
            FragmentManager fm = activity.getFragmentManager();
            FragmentTransaction tr = fm.beginTransaction();
            tr.replace(R.id.qr_screen, Fragment.instantiate(activity, QRScannerFragment.class.getName()),QRScannerFragment.class.getClass().getCanonicalName());

            tr.commit();
        }
        return true;
    }
    public static class QRScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {

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

            String strRes=String.valueOf(result);

            if(strRes!=null && !strRes.isEmpty()) {
                JSONObject res=null;
                try {
                    res=new JSONObject(strRes);
                } catch (Exception e) {
                    //Log.e("JSON","ex: "+e.getMessage());
                }
                if(res!=null) {
                    String smsg=SeedValidator.isSeedValid(getActivity(),res.optString("seed"));
                    if(smsg==null) {
                        //getActivity().cameraSeed=res.optString("seed");
                        seedEditText.setText(res.optString("seed"));
                    } else  {
                        Snackbar.make(view,smsg,Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    if(SeedValidator.isSeedValid(getActivity(),strRes)==null) {
                        seedEditText.setText(strRes);
                    } else  {
                        Snackbar.make(view,R.string.no_seed_data,Snackbar.LENGTH_SHORT).show();
                    }
                }

            } else {
                Snackbar.make(view,R.string.no_seed_data,Snackbar.LENGTH_SHORT).show();
            }
            scannerView.stopCamera();
            scannerView.invalidate();
            screenAdd.setVisibility(View.VISIBLE);
            screenQr.setVisibility(View.GONE);
        }

    }
}
