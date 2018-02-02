package run.wallet.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import run.wallet.R;


public class bImageButton extends android.support.v7.widget.AppCompatImageButton {

	public bImageButton(Context context) {
	    super(context);
	    init(context);
	    // TODO Auto-generated constructor stub
	}
	
	public bImageButton(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init(context);
	    // TODO Auto-generated constructor stub
	}
	
	public bImageButton(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init(context);
	    // TODO Auto-generated constructor stub
    }
	public void setLayoutParamsInPx(int width, int height) {
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width,height);
		this.setLayoutParams(layoutParams);
	}
	private void init(Context context) {
		this.setBackgroundResource(R.drawable.btn_general);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		//params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.setMargins(2, 2, 2, 2);
		this.setLayoutParams(params);
		this.setClickable(true);
		this.setBackgroundColor(context.getResources().getColor(R.color.colorLight));
		//this.setTextSize(20);
		//this.setTextColor(context.getResources().getColor(R.color.black));
		this.setPadding(2, 2, 2, 2);

	}
}