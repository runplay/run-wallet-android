package run.wallet.iota.ui.activity;

import android.content.Intent;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Start home activity

        startActivity(new Intent(SplashActivity.this, MainActivity.class));

        // close splash activity

        finish();

    }

}
