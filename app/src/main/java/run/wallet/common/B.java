
package run.wallet.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.animation.AlphaAnimation;



public final class B {


    public static AlphaAnimation animateAlphaFlash() {
        AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
        animation1.setDuration(200);
        animation1.setStartOffset(50);
        animation1.setFillAfter(true);
        return animation1;
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



}
