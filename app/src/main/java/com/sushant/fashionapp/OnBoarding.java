package com.sushant.fashionapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.sushant.fashionapp.Seller.SellerHomePage;
import com.sushant.fashionapp.databinding.ActivityOnBoardingBinding;

public class OnBoarding extends AppCompatActivity {

    ActivityOnBoardingBinding binding;
    FirebaseAuth auth;
    SharedPreferences sharedPreferences;
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
            sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
            boolean isSeller = sharedPreferences.getBoolean("isSeller", false);
            if (isSeller) {
                startActivity(new Intent(OnBoarding.this, SellerHomePage.class));
            } else {
                startActivity(new Intent(OnBoarding.this, ActivityHomePage.class));
            }
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

        }
    }
}