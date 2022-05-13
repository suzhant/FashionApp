package com.sushant.fashionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sushant.fashionapp.databinding.ActivityWelcomeScreenBinding;

public class WelcomeScreen extends AppCompatActivity {

    ActivityWelcomeScreenBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityWelcomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //  getSupportActionBar().hide();

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeScreen.this,ActivityRegister.class));
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
            }
        });

        binding.btnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeScreen.this,ActivitySignIn.class));
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
            }
        });

    }
}