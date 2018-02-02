package run.wallet.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class bEditText extends android.support.v7.widget.AppCompatEditText {

    private EditTextImeBackListener mOnImeBack;

    public bEditText(Context context) {
        super(context);
        //B.addStyle(this);
    }

    public bEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        //B.addStyle(this);
    }

    public bEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //B.addStyle(this);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null) mOnImeBack.onImeBack();
        }
        return super.onKeyPreIme(keyCode, event); //super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
        mOnImeBack = listener;
    }
    public interface EditTextImeBackListener {
        public abstract void onImeBack();
    }
}