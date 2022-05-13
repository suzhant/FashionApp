package com.sushant.fashionapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        //  Objects.requireNonNull(getSupportActionBar()).hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashScreen.this,OnBoarding.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finishAfterTransition();
            }
        },2000);
    }
}