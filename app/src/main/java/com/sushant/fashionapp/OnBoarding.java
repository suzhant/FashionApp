package com.sushant.fashionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sushant.fashionapp.databinding.ActivityOnBoardingBinding;

public class OnBoarding extends AppCompatActivity {

    ActivityOnBoardingBinding binding;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnBoardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        // Objects.requireNonNull(getSupportActionBar()).hide();
        binding.fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OnBoarding.this, WelcomeScreen.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(OnBoarding.this, ActivityHomePage.class));
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }


    }
}