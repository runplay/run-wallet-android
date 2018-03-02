package run.wallet.iota.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;

import run.wallet.R;
import run.wallet.common.B;

/**
 * Created by coops on 15/02/18.
 */

public class AppTheme {


    public static final int THEME_DEFAULT=0;
    public static final int THEME_BLUE=1;
    public static final int THEME_DG=2;
    public static final int THEME_PINK=3;
    public static final int THEME_GREEN=4;
    public static final int THEME_DN=5;
    public static final int THEME_SNOW=6;
    public static final int THEME_RED=7;
    public static final int THEME_BW=8;
    public static final int THEME_PURPLE=9;
    public static final int THEME_LGBOTA=10;

    private static int THEME=0;
    public static void init(Context context) {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(context);
        THEME = prefs.getInt("pref_theme",0);
    }

    public static final boolean isLgbota() {
        if(THEME==THEME_LGBOTA)
            return true;
        return false;
    }
    @SuppressWarnings("deprecation")
    public static void setNavColors(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(isLgbota()) {
                activity.getWindow().setNavigationBarColor(B.getColor(activity, getPrimary()));
                activity.getWindow().setStatusBarColor(B.getColor(activity, R.color.textColorLgbotaPrimary));
            } else {
                activity.getWindow().setNavigationBarColor(B.getColor(activity, getPrimary()));
                activity.getWindow().setStatusBarColor(B.getColor(activity, getPrimaryDark()));
            }
        }
    }
    public static void setTheme(Context context, int THEME_) {
        THEME=THEME_;
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt("pref_theme",THEME).commit();
    }
    public static int getPigRdrawable() {
        switch(THEME) {
            case THEME_LGBOTA:
                return R.drawable.piglbgt;
        }
        return R.drawable.pig;
    }
    public static final int getTheme() {
        switch(THEME) {
            case THEME_DEFAULT:
                return R.style.AppTheme;
            case THEME_BLUE:
                return R.style.AppThemeBlue;
            case THEME_PINK:
                return R.style.AppThemePink;
            case THEME_DG:
                return R.style.AppThemeDg;
            case THEME_GREEN:
                return R.style.AppThemeGreen;
            case THEME_DN:
                return R.style.AppThemeDn;
            case THEME_SNOW:
                return R.style.AppThemeSnow;
            case THEME_RED:
                return R.style.AppThemeRed;
            case THEME_BW:
                return R.style.AppThemeBw;
            case THEME_PURPLE:
                return R.style.AppThemePurple;
            case THEME_LGBOTA:
                return R.style.AppThemeLgbota;
        }
        return R.style.AppTheme;
    }
    public static int getColorPrimary(Context context) {
        return B.getColor(context,getPrimary());
    }
    public static final int getPrimary() {
        switch(THEME) {
            case THEME_DEFAULT:
                return R.color.colorPrimary;
            case THEME_BLUE:
                return R.color.colorBluePrimary;
            case THEME_PINK:
                return R.color.colorPinkPrimary;
            case THEME_DG:
                return R.color.colorDgPrimary;
            case THEME_GREEN:
                return R.color.colorGreenPrimary;
            case THEME_DN:
                return R.color.colorDnPrimary;
            case THEME_SNOW:
                return R.color.colorSnowPrimary;
            case THEME_RED:
                return R.color.colorRedPrimary;
            case THEME_BW:
                return R.color.colorBwPrimary;
            case THEME_PURPLE:
                return R.color.colorPurplePrimary;
            case THEME_LGBOTA:
                return R.color.colorLgbotaPrimary;
        }
        return R.color.colorPrimary;
    }
    public static int getColorPrimaryDark(Context context) {
        return B.getColor(context,getPrimaryDark());
    }
    public static final int getPrimaryDark() {
        switch(THEME) {
            case THEME_DEFAULT:
                return R.color.colorPrimaryDark;
            case THEME_BLUE:
                return R.color.colorBluePrimaryDark;
            case THEME_PINK:
                return R.color.colorPinkPrimaryDark;
            case THEME_DG:
                return R.color.colorDgPrimaryDark;
            case THEME_GREEN:
                return R.color.colorGreenPrimaryDark;
            case THEME_DN:
                return R.color.colorDnPrimaryDark;
            case THEME_SNOW:
                return R.color.colorSnowPrimaryDark;
            case THEME_RED:
                return R.color.colorRedPrimaryDark;
            case THEME_BW:
                return R.color.colorBwPrimaryDark;
            case THEME_PURPLE:
                return R.color.colorPurplePrimaryDark;
            case THEME_LGBOTA:
                return R.color.colorLgbotaPrimaryDark;
        }
        return R.color.colorPrimaryDark;
    }
    public static final int getAccent() {
        switch(THEME) {
            case THEME_DEFAULT:
                return R.color.colorAccent;
            case THEME_BLUE:
                return R.color.colorBlueAccent;
            case THEME_PINK:
                return R.color.colorPinkAccent;
            case THEME_DG:
                return R.color.colorDgAccent;
            case THEME_GREEN:
                return R.color.colorGreenAccent;
            case THEME_DN:
                return R.color.colorDnAccent;
            case THEME_SNOW:
                return R.color.colorSnowAccent;
            case THEME_RED:
                return R.color.colorRedAccent;
            case THEME_BW:
                return R.color.colorBwAccent;
            case THEME_PURPLE:
                return R.color.colorPurpleAccent;
            case THEME_LGBOTA:
                return R.color.colorLgbotaAccent;
        }
        return R.color.colorAccent;
    }
    public static final int getSecondary() {
        switch(THEME) {
            case THEME_DEFAULT:
                return R.color.colorSecondary;
            case THEME_BLUE:
                return R.color.colorBlueSecondary;
            case THEME_PINK:
                return R.color.colorPinkSecondary;
            case THEME_DG:
                return R.color.colorDgSecondary;
            case THEME_GREEN:
                return R.color.colorGreenSecondary;
            case THEME_DN:
                return R.color.colorDnSecondary;
            case THEME_SNOW:
                return R.color.colorSnowSecondary;
            case THEME_RED:
                return R.color.colorRedSecondary;
            case THEME_BW:
                return R.color.colorBwSecondary;
            case THEME_PURPLE:
                return R.color.colorPurpleSecondary;
            case THEME_LGBOTA:
                return R.color.colorLgbotaSecondary;
        }
        return R.color.colorSecondary;
    }
    public static final int getNavDrawableId() {
        switch(THEME) {
            case THEME_DEFAULT:
                return R.drawable.side_nav_bar;
            case THEME_BLUE:
                return R.drawable.side_nav_bar_blue;
            case THEME_PINK:
                return R.drawable.side_nav_bar_pink;
            case THEME_DG:
                return R.drawable.side_nav_bar_dg;
            case THEME_GREEN:
                return R.drawable.side_nav_bar_green;
            case THEME_DN:
                return R.drawable.side_nav_bar_dn;
            case THEME_SNOW:
                return R.drawable.side_nav_bar_snow;
            case THEME_RED:
                return R.drawable.side_nav_bar_red;
            case THEME_BW:
                return R.drawable.side_nav_bar_bw;
            case THEME_PURPLE:
                return R.drawable.side_nav_bar_purple;
            case THEME_LGBOTA:
                return R.color.colorLgbotaPrimary;
        }
        return R.drawable.side_nav_bar;
    }

    public static final int getButtonStyle() {
        switch(THEME) {
            case THEME_DEFAULT:
                return R.style.AppButton;

        }
        return R.style.AppButton;
    }
}
