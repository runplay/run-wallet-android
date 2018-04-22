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

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Sf;
import run.wallet.common.json.JSONObject;
import run.wallet.iota.api.responses.AddressSecurityChangeResponse;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.UiManager;
import run.wallet.iota.ui.adapter.WalletAddressCardAdapter;
import run.wallet.iota.ui.adapter.WalletTransfersCardAdapter;
import run.wallet.iota.ui.dialog.ChooseSeedItemDialog;
import run.wallet.iota.ui.dialog.ShowNoDescDialog;
import run.wallet.iota.ui.dialog.WipeSeedDialog;

public class ChooseSeedEditFragment extends Fragment {


    private Unbinder unbinder;
    private View view;
    @BindView(R.id.address_toolbar)
    Toolbar Toolbar;
    @BindView(R.id.edit_wallet_name)
    EditText name;
    @BindView(R.id.edit_wallet_default)
    Switch setDefault;
    @BindView(R.id.edit_wallet_seed)
    TextView seed;
    @BindView(R.id.edit_wallet_view)
    Button btnViewSeed;
    @BindView(R.id.edit_wallet_reload)
    Button btnReload;
    @BindView(R.id.edit_wallet_check)
    Button checkUsed;
    @BindView(R.id.print_seed)
    Button printSeed;
    @BindView(R.id.print_trinity_comp)
    CheckBox printTrinity;

    private static final DecimalFormat df = new DecimalFormat( "#00" );
    private Seeds.Seed useSeed;
    private boolean hasChanges;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_choose_edit, container, false);
        view.setBackgroundColor(B.getColor(getActivity(), AppTheme.getSecondary()));
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(Toolbar);
        setHasOptionsMenu(false);
        useSeed=Store.getCacheSeed();
        Toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.md_nav_back));
        Toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();
            }
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(useSeed!=null) {
                    useSeed.name = s.toString();
                    hasChanges = true;
                }
            }
        });
        setDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked) {
                    if(useSeed!=null) {
                        useSeed.isdefault = isChecked;
                        Store.setDefaultSeed(useSeed);
                        hasChanges = true;
                        setDefault.setEnabled(false);
                        setDefault.setText(getString(R.string.title_edit_isdefault));
                    }
                }
            }
        });
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WipeSeedDialog dialog = new WipeSeedDialog();
                dialog.setSeed(useSeed);

                dialog.show(getActivity().getFragmentManager(), null);
            }
        });
        checkUsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppService.checkUsedAddress(useSeed);
                Store.setCurrentSeed(getActivity(),useSeed);
                WalletTransfersFragment.resetScroll();

                WalletAddressesFragment.resetScroll();
                WalletTransfersCardAdapter.setFilterAddress(null,null);
                WalletAddressCardAdapter.load(getActivity(),true);
                WalletTransfersCardAdapter.load(getActivity(),true);
                UiManager.openFragment(getActivity(), WalletTabFragment.class);
            }
        });
        printSeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPrintSeed();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(hasChanges) {
            Store.saveSeeds(getActivity());
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Store.setCurrentFragment(this.getClass());
        hasChanges=false;
        if(Store.getCacheSeed()==null)
            getActivity().onBackPressed();
        useSeed=Store.getCacheSeed();
        seed.setText(useSeed.getShortValue());
        name.setText(useSeed.name);
        if(useSeed.isdefault) {
            setDefault.setChecked(true);
            setDefault.setEnabled(false);
            setDefault.setText(getString(R.string.title_edit_isdefault));
        } else {
            setDefault.setChecked(false);
            setDefault.setEnabled(true);
            setDefault.setText(getString(R.string.title_edit_default));
        }
    }
    @OnClick(R.id.edit_wallet_view)
    public void onEditWalletViewClick() {
        ShowNoDescDialog showSeedDialog = new ShowNoDescDialog();
        showSeedDialog.setSeed(Store.getCacheSeed());
        showSeedDialog.show(getActivity().getFragmentManager(), null);
    }
    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }
    private void printImage(Bitmap bitmap) {
        try {
            PrintHelper photoPrinter = new PrintHelper(getActivity());
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            photoPrinter.printBitmap("Ink check", bitmap);
        } catch(Exception e) {
            Snackbar.make(view,getActivity().getString(R.string.print_failed),Snackbar.LENGTH_SHORT).show();
        }
    }
    private void goPrintSeed() {
        boolean trinityComp = printTrinity.isChecked();
        Bitmap qrImg=generateImage(useSeed,getActivity(),true,trinityComp);
        if(qrImg!=null) {
            printImage(qrImg);
        } else {
            Snackbar.make(view,getActivity().getString(R.string.print_error),Snackbar.LENGTH_SHORT).show();
        }
    }
    protected Bitmap generateImage(Seeds.Seed seed, final Activity activity, boolean withQR, boolean trinityComp){
        if(seed==null){
            return null;
        }
        String useSeed=String.valueOf(Store.getSeedRaw(activity,seed));
        String bmpData=useSeed;
        if(!trinityComp) {
            JSONObject job = new JSONObject();
            job.put("seed",useSeed);
            job.put("name",seed.name);
            bmpData=job.toString();
        }
        int size = 390;
        int fixed = 200;
        int sizeM=230;
        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintMap.put(EncodeHintType.MARGIN, 1);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {
            BitMatrix byteMatrix = qrCodeWriter.encode(bmpData, BarcodeFormat.QR_CODE, size,
                    size, hintMap);
            int height = byteMatrix.getHeight();
            int width = byteMatrix.getWidth();
            final Bitmap qrImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++){
                for (int y = 0; y < height; y++){
                    qrImage.setPixel(x, y, byteMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                }
            }
            Bitmap background= Bitmap.createScaledBitmap(((BitmapDrawable) B.getDrawable(activity, R.drawable.qr_bg)).getBitmap(),390,90,false);
            Bitmap bmOverlay = null;
            if(withQR) {
                bmOverlay = Bitmap.createBitmap(qrImage.getWidth(), qrImage.getHeight() + sizeM, qrImage.getConfig());
            } else {
                bmOverlay =Bitmap.createBitmap(size, fixed, qrImage.getConfig());
            }
            Canvas canvas = new Canvas(bmOverlay);
            Rect bg = new Rect();
            bg.set(0,0,qrImage.getWidth(),qrImage.getHeight()+sizeM);
            Paint bgp = new Paint(Paint.ANTI_ALIAS_FLAG);
            // text color - #3D3D3D
            bgp.setColor(Color.rgb(255,255,255));
            canvas.drawRect(bg,bgp);
            if(withQR) {
                canvas.drawBitmap(qrImage, 0, sizeM, null);
            }
            canvas.drawBitmap(background, 0, 0,null );

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(3,10,3));
            paint.setTextSize((int) (16));
            paint.setShadowLayer(1f, 0f, 1f, Color.argb(10,10,20,10));

            Paint spaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            spaint.setColor(Color.rgb(1,1,1));
            spaint.setTextSize((int) (12));

            Rect bounds = new Rect();
            paint.getTextBounds(useSeed.substring(0,30), 0, 30, bounds);

            int x = (qrImage.getWidth() - bounds.width())/2;
            int y = 120;


            canvas.drawText(useSeed.substring(0,30), x, y+20 , paint);
            canvas.drawText(useSeed.substring(30,60), x, y+40, paint);
            canvas.drawText(useSeed.substring(60,useSeed.length()), x, y+60, paint);
            canvas.drawText("Store this somewhere safe, the seed allows anyone", x, y+90 , spaint);
            canvas.drawText("who knows it to have full access the IOTA secured", x, y+110 , spaint);
            String name=seed.name;
            if(name==null || name.isEmpty()) {
                name=getFileDateYYYYMMDDHHMMSS();
            }
            canvas.drawText(activity.getString(R.string.name)+": "+name, x, y , paint);
            return bmOverlay;

        } catch (WriterException e) {
            Log.e("QR.ex",""+e.getMessage());

        }
        return null;
    }

    protected static String getFileDateYYYYMMDDHHMMSS()  {
        GregorianCalendar gc = new GregorianCalendar();
        int day = gc.get(GregorianCalendar.DAY_OF_MONTH);
        int month = gc.get(GregorianCalendar.MONTH)+1;
        int year = gc.get(GregorianCalendar.YEAR);
        int hour = gc.get(GregorianCalendar.HOUR_OF_DAY);
        int minutes = gc.get(GregorianCalendar.MINUTE);
        return year + "-" + df.format(month) + "-"+ df.format(day) + "-" +df.format(hour)+"h"+df.format(minutes)+"m";
    }
}
