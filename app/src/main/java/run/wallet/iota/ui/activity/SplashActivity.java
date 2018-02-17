package run.wallet.iota.ui.activity;

import android.content.Intent;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import run.wallet.R;
import run.wallet.common.B;
import run.wallet.iota.helper.AppTheme;
import run.wallet.iota.helper.TorHelper;

public class SplashActivity extends AppCompatActivity {

    Handler handler = new Handler();
    LinearLayout splashTor;
    TextView splashTorInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TorHelper.init(this,null);
        if(!TorHelper.isForce()) {
            goStart();
        }

    }
    private void goStart() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
    @Override
    public void onResume() {
        super.onResume();

        splashTor=findViewById(R.id.splash_tor);
        splashTorInfo=findViewById(R.id.splash_tor_status);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startLoop();
            }
        },100);

    }

    private void startLoop() {
        TorHelper.torEnable();

        splashTor.setVisibility(View.VISIBLE);
        if(TorHelper.getTorStatus()==TorHelper.STATUS_ON) {
            //Log.e("SPLASH","TOR ON - OPEN APP");
            goStart();
            splashTorInfo.setText(getString(R.string.tor_info_connected));
        } else {

            if(TorHelper.getTorStatus()==TorHelper.STATUS_OFF) {
                //Log.e("SPLASH","TOR OFF - START CALL");

                splashTorInfo.setText(getString(R.string.tor_info_start));
            } else {
                splashTorInfo.setText(getString(R.string.tor_info_connecting));
            }


            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Log.e("SPLASH","LOOP");
                    startLoop();
                }
            },1000);
        }

    }


}
