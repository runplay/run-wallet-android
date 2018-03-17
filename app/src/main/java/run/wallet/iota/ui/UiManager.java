package run.wallet.iota.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobapphome.mahencryptorlib.MAHEncryptor;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import jota.utils.IotaToText;
import jota.utils.IotaToText;
import run.wallet.R;
import run.wallet.common.B;
import run.wallet.common.Sf;
import run.wallet.iota.api.requests.ApiRequest;
import run.wallet.iota.api.requests.AuditAddressesRequest;
import run.wallet.iota.api.requests.AutoNudgeRequest;
import run.wallet.iota.api.requests.GetAccountDataRequest;
import run.wallet.iota.api.requests.GetFirstLoadRequest;
import run.wallet.iota.api.requests.GetNewAddressRequest;
import run.wallet.iota.api.requests.NudgeRequest;
import run.wallet.iota.api.requests.RefreshUsedAddressesRequest;
import run.wallet.iota.api.requests.ReplayBundleRequest;
import run.wallet.iota.api.requests.SendTransferRequest;
import run.wallet.iota.helper.AESCrypt;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.Constants;
import run.wallet.iota.model.Address;
import run.wallet.iota.model.Seeds;
import run.wallet.iota.model.Store;
import run.wallet.iota.service.AppService;
import run.wallet.iota.ui.activity.MainActivity;
import run.wallet.iota.ui.activity.SettingsActivity;
import run.wallet.iota.ui.dialog.ForgotPasswordDialog;

/**
 * Created by coops on 18/12/17.
 */

public class UiManager {
    public static boolean isLollipopOrMore() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        return false;
    }
    public static void setKeyboard(Activity activity, View editTextView, boolean showKeyboard) {
        editTextView.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm!=null) {
            if(showKeyboard) {
                imm.showSoftInput(editTextView, 0);
            } else {
                imm.hideSoftInputFromWindow(editTextView.getWindowToken(), 0);
            }
        }
    }
    public static void setActionbarColor(AppCompatActivity activity, int color) {
        activity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
        //activity.getSupportActionBar().invalidateOptionsMenu();
        activity.invalidateOptionsMenu();
    }
    private static AlertDialog dialog;
    public static final AlertDialog getPasswordDialog() {
        return dialog;
    }


    private class filterBar {
        LinearLayout view;
        Adapter adapter;

    }

    public static void displayInfoBar(Activity context, LinearLayout forView) {
        makeElements(context);
        forView.setBackgroundColor(B.getColor(context,AppTheme.getPrimaryDark()));
        try {
            List<ApiRequest> attaching = AppService.getRunningTasks();
            List<LinearLayout> processing=new ArrayList<>();
            if(!attaching.isEmpty()) {
                Seeds.Seed currentSeed=Store.getCurrentSeed();
                for (ApiRequest req : attaching) {
                    if(req instanceof SendTransferRequest) {
                        SendTransferRequest str = (SendTransferRequest) req;
                        if(str.getSeed().id.equals(currentSeed.id)) {
                            if (str.getValue() == 0) {
                                processing.add(createProcessRunningPod(context, R.drawable.tran_white, context.getString(R.string.info_new_attach), 0));
                            } else {
                                processing.add(createProcessRunningPod(context, R.drawable.send_white, context.getString(R.string.info_transfer), str.getValue()));
                            }
                        }
                    } else if(req instanceof GetAccountDataRequest) {
                        GetAccountDataRequest request=(GetAccountDataRequest) req;
                        if(request.getSeed().id.equals(currentSeed.id)) {
                            processing.add(createProcessRunningPod(context, R.drawable.refresh_white, context.getString(R.string.info_refresh), 0));
                        }
                    } else if(req instanceof GetNewAddressRequest) {
                        GetNewAddressRequest request=(GetNewAddressRequest) req;
                        if(request.getSeed().id.equals(currentSeed.id)) {
                            processing.add(createProcessRunningPod(context, R.drawable.tran_white, context.getString(R.string.info_new_address), 0));
                        }
                    } else if(req instanceof GetFirstLoadRequest) {
                        GetFirstLoadRequest request=(GetFirstLoadRequest) req;
                        if(request.getSeed().id.equals(currentSeed.id)) {
                            processing.add(createProcessRunningPod(context, R.drawable.refresh_white, context.getString(R.string.info_first_load), 0));
                        }
                    } else if(req instanceof AuditAddressesRequest) {
                        AuditAddressesRequest request = (AuditAddressesRequest) req;
                        if(request.getSeed().id.equals(currentSeed.id)) {
                            processing.add(createProcessRunningPod(context, R.drawable.refresh_white, context.getString(R.string.info_audit), 0));
                        }
                    } else if(req instanceof ReplayBundleRequest) {
                        ReplayBundleRequest request=(ReplayBundleRequest) req;
                        if(request.getSeed().id.equals(currentSeed.id)) {
                            processing.add(createProcessRunningPod(context, R.drawable.send_white, context.getString(R.string.info_resend), 0));
                        }
                    } else if(req instanceof NudgeRequest) {
                        NudgeRequest request=(NudgeRequest) req;
                        if(request.getSeed().id.equals(currentSeed.id)) {
                            processing.add(createProcessRunningPod(context, R.drawable.send_white, context.getString(R.string.info_nudge), 0));
                        }
                    } else if(req instanceof AutoNudgeRequest) {
                        //if(request.getSeed().id.equals(currentSeed.id)) {
                        processing.add(createProcessRunningPod(context, R.drawable.send_white, context.getString(R.string.auto), 0));
                        //}
                    } else if(req instanceof RefreshUsedAddressesRequest) {
                        //if(request.getSeed().id.equals(currentSeed.id)) {
                        processing.add(createProcessRunningPod(context, R.drawable.send_white, context.getString(R.string.info_audit), 0));
                        //}
                    }

                }
            }
            forView.removeAllViews();
            if(!processing.isEmpty()) {
                for(LinearLayout addview: processing) {
                    forView.addView(addview);
                }
                forView.canScrollHorizontally(View.LAYOUT_DIRECTION_LTR);
                if(forView.getVisibility()!=View.VISIBLE) {
                    forView.setVisibility(View.VISIBLE);
                    AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom);
                }
            } else if(Store.getUsedAddressCheckResult()!=null) {
                Snackbar.make(forView, Store.getUsedAddressCheckResult(), Snackbar.LENGTH_LONG).show();
                Store.setUsedAddressCheckResult(null);
            } else if(Store.getCurrentSeed().warnUsed) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean showused=prefs.getBoolean(Constants.PREFERENCES_SHOW_USED,true);

                TextView messy = new TextView(context);
                if(showused) {
                    messy.setText(context.getString(R.string.usedAddressWarn));
                } else {
                    messy.setText(context.getString(R.string.usedAddressSettings));
                }

                messy.setTextColor(Color.WHITE);
                messy.setPadding(30, 30, 30, 30);
                //messy.setText
                messy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent settings = new Intent(context, SettingsActivity.class);
                        context.startActivityForResult(settings, 0);
                    }
                });
                forView.addView(messy);

                forView.canScrollHorizontally(View.LAYOUT_DIRECTION_LTR);
                if (forView.getVisibility() != View.VISIBLE) {
                    AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom);
                }



            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                if(prefs.getBoolean(Constants.PREFERENCES_SHOW_CANCELLED, true)
                    && !prefs.getBoolean(Constants.PREFERENCES_SHOW_ATTACH, true)
                        && Store.getTransfers().size()>15
                        ) {
                    int count=prefs.getInt(Constants.PREF_MSG_MESSY, 0);
                    if(count<2) {
                        TextView messy = new TextView(context);
                        messy.setText(context.getString(R.string.message_messy_view));
                        messy.setTextColor(Color.WHITE);
                        messy.setPadding(30, 30, 30, 30);
                        //messy.setText
                        messy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent settings = new Intent(context, SettingsActivity.class);
                                context.startActivityForResult(settings, 0);
                            }
                        });
                        forView.addView(messy);

                        forView.canScrollHorizontally(View.LAYOUT_DIRECTION_LTR);
                        if (forView.getVisibility() != View.VISIBLE) {
                            AnimationUtils.loadAnimation(context, R.anim.slide_in_from_bottom);
                        }
                        prefs.edit().putInt(Constants.PREF_MSG_MESSY,++count).commit();
                    }

                } else {
                    AnimationUtils.loadAnimation(context, R.anim.slide_out_to_bottom);
                }
            }

        } catch(Exception e) {}
    }
    private static void makeElements(Context context) {
        if(bgcolor==0) {
            bgcolor= B.getColor(context, AppTheme.getPrimary());
            bglight=B.getColor(context,R.color.colorLight);
            white=B.getColor(context,R.color.white);
            red=B.getColor(context,R.color.flatRed);
            green=B.getColor(context,R.color.green);
            main.setMargins(8,8,8,8);
            param.setMargins(8,4,8,4);
            param2.setMargins(8,4,8,4);
            param3.setMargins(0,4,8,4);
        }
    }
    private static LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static LinearLayout.LayoutParams main = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
    private static int bgcolor= 0;
    private static int bglight=0;
    private static int white=0;
    private static int red=0;
    private static int green=0;
    private static LinearLayout createProcessRunningPod(Context context, int Rdrawable, String name, long value) {

        List<Address> allAddresses = Store.getAddresses();


        LinearLayout layout = new LinearLayout(context);
        //layout.setBackground(B.getDrawable(context,R.drawable.info_bar_item));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setLayoutParams(main);
        layout.setPadding(2, 2, 2, 2);


        ImageView addId = new ImageView(context);
        addId.setLayoutParams(param);
        addId.setImageResource(Rdrawable);
        //addId.setMa

        TextView addValue = new TextView(context);
        addValue.setLayoutParams(param2);
        String text = IotaToText.convertRawIotaAmountToDisplayText(value, true);

        addValue.setText(text);
        addValue.setTextSize(20F);
        addValue.setTypeface(null, Typeface.BOLD);
        if(value<0) {
            addValue.setTextColor(red);
        } else if(value>0) {
            addValue.setTextColor(green);
        }
        addValue.setPadding(5, 2, 2, 2);
        addValue.setSingleLine();

        TextView addAddress = new TextView(context);
        addAddress.setLayoutParams(param3);
        addAddress.setText(name);
        addAddress.setTextColor(white);
        addAddress.setTextSize(12F);
        addAddress.setPadding(5, 2, 2, 2);
        addAddress.setSingleLine();


        layout.addView(addId);

        layout.addView(addAddress);
        if(value!=0) {
            layout.addView(addValue);
        }
        return layout;
    }

    private static TextView text;
    public static void checkPin (final Activity activity) {

        if (!Store.isLoggedIn()) {
            if(dialog!=null) {
                dialog.dismiss();
                dialog=null;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.PinDialog);
            final View dialogView = View.inflate(activity, R.layout.dialog_password, null);

            text = (TextView) dialogView.findViewById(R.id.pass_userPin);

            Button ib0 = (Button) dialogView.findViewById(R.id.button0);
            assert ib0 != null;
            ib0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "0");
                }
            });

            Button ib1 = (Button) dialogView.findViewById(R.id.button1);
            assert ib1 != null;
            ib1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "1");
                }
            });

            Button ib2 = (Button) dialogView.findViewById(R.id.button2);
            assert ib2 != null;
            ib2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "2");
                }
            });

            Button ib3 = (Button) dialogView.findViewById(R.id.button3);
            assert ib3 != null;
            ib3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "3");
                }
            });

            Button ib4 = (Button) dialogView.findViewById(R.id.button4);
            assert ib4 != null;
            ib4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "4");
                }
            });

            Button ib5 = (Button) dialogView.findViewById(R.id.button5);
            assert ib5 != null;
            ib5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "5");
                }
            });

            Button ib6 = (Button) dialogView.findViewById(R.id.button6);
            assert ib6 != null;
            ib6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "6");
                }
            });

            Button ib7 = (Button) dialogView.findViewById(R.id.button7);
            assert ib7 != null;
            ib7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "7");
                }
            });

            Button ib8 = (Button) dialogView.findViewById(R.id.button8);
            assert ib8 != null;
            ib8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "8");
                }
            });

            Button ib9 = (Button) dialogView.findViewById(R.id.button9);
            assert ib9 != null;
            ib9.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterNum(activity,dialogView, "9");
                }
            });


            ImageButton enter = (ImageButton) dialogView.findViewById(R.id.imageButtonEnter);
            assert enter != null;

            final ImageButton cancel = (ImageButton) dialogView.findViewById(R.id.imageButtonCancel);
            assert cancel != null;
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    text.setText("");
                }
            });

            final Button clear = (Button) dialogView.findViewById(R.id.buttonReset);
            assert clear != null;
            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog();
                    forgotPasswordDialog.show(activity.getFragmentManager(), null);

                }
            });

            builder.setView(dialogView);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    activity.finishAffinity();
                }
            });

            dialog = builder.create();
            dialog.show();

            enter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    login(activity,true);


                }
            });


        }
    }
    private static void login(Activity activity, boolean warn) {
        String password = text.getText().toString().trim();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        try {
            AESCrypt aes = new AESCrypt(password);
            String encSeed = prefs.getString(Constants.PREFERENCE_ENC_PASS, "");
            String dec = aes.decrypt(encSeed);

            if(!Store.login(dec))
                throw new Exception();
            Intent intent = new Intent(activity.getIntent());
            activity.startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN);
            dialog.dismiss();
        } catch (Exception e) {
            if(warn)
                Snackbar.make(text, R.string.toast_wrongPW, Snackbar.LENGTH_LONG).show();
        }
    }
    private static void enterNum (Activity activity, View view, String number) {
        TextView text = (TextView) view.findViewById(R.id.pass_userPin);
        String textNow = text.getText().toString().trim();
        String pin = textNow + number;
        text.setText(pin);

        String password=text.getText().toString().trim();
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(activity);
        int pl=prefs.getInt(Constants.PREFERENCE_PASS_LENGTH,0);
        if(pl>0 && password.length()==pl) {
            login(activity,false);
        }
    }
    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(Context context, int Rdrawable) {
        if(android.os.Build.VERSION.SDK_INT>= 21) {
            return context.getDrawable(Rdrawable);
        } else {
            return context.getResources().getDrawable(Rdrawable);
        }
    }
    public static int getColor(Context context, int Rcolor) {
        return ContextCompat.getColor(context, Rcolor);

    }
    public static void setActionBarBackOnly(Activity activity, String title, ColorDrawable color) {
        final AppCompatActivity apact = (AppCompatActivity) activity;
        ActionBar ab = apact.getSupportActionBar();
        if(color!=null) {
            ab.setBackgroundDrawable(color);
        }
        ab.setTitle(title);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        apact.supportInvalidateOptionsMenu();
        //ab.invalidateOptionsMenu();
        //NavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);

        apact.supportInvalidateOptionsMenu();
        //ab.invalidateOptionsMenu();
        ab.show();

    }
    public static void setActionBarHome(Activity activity, String title, ColorDrawable color) {
        final AppCompatActivity apact = (AppCompatActivity) activity;
        ActionBar ab = apact.getSupportActionBar();
        if(color!=null) {
            ab.setBackgroundDrawable(color);
        }
        ab.setTitle(title);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(false);
        apact.supportInvalidateOptionsMenu();
        //ab.invalidateOptionsMenu();
        ab.show();

    }
    public static void popBackStack(Activity activity) {
        activity.getFragmentManager().popBackStack();
    }
    public static boolean openFragment(Activity activity, Class<? extends Fragment> fragment) {
        if(activity!=null && !activity.isDestroyed()) {
            FragmentManager fm = activity.getFragmentManager();
            FragmentTransaction tr = fm.beginTransaction();
            tr.replace(R.id.container, Fragment.instantiate(activity, fragment.getName()),fragment.getClass().getCanonicalName());

            tr.commit();
        }
        return true;
    }
    public static boolean openFragmentBackStack(Activity activity,Class<? extends Fragment> fragment) {
        if(activity!=null && !activity.isDestroyed()) {
            FragmentManager fm = activity.getFragmentManager();
            FragmentTransaction tr = fm.beginTransaction();
            tr.setCustomAnimations(R.animator.fade_in, R.animator.fade_out,
                    R.animator.fade_in, R.animator.fade_out);

            //String tag = fragment.getClass().getCanonicalName();
            tr.replace(R.id.container, Fragment.instantiate(activity, fragment.getName()), fragment.getClass().getCanonicalName());
            tr.addToBackStack(null);
            tr.commit();
        }
        return true;
    }
}
