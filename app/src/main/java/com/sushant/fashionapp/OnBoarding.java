package com.sushant.fashionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sushant.fashionapp.databinding.ActivityOnBoardingBinding;

public class OnBoarding extends AppCompatActivity {

    ActivityOnBoardingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityOnBoardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Objects.requireNonNull(getSupportActionBar()).hide();
        binding.fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(OnBoarding.this,WelcomeScreen.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
            }
        });

    }
}