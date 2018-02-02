package run.wallet.common;


import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;



public class Functions {
	
	
	
    public static int dpToPx(int dp, Context ctx) {
    Resources r = ctx.getResources();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

	

}
