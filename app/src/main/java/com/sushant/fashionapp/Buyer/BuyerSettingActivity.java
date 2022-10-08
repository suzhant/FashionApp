package com.sushant.fashionapp.Buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sushant.fashionapp.databinding.ActivityBuyerSettingBinding;

public class BuyerSettingActivity extends AppCompatActivity {

    ActivityBuyerSettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuyerSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.lytChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BuyerSettingActivity.this, BuyerProfileActivity.class));
            }
        });

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.lytSecurity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BuyerSettingActivity.this, BuyerSecurityActivity.class));
            }
        });
    }
}