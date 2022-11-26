package com.sushant.fashionapp.Seller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sushant.fashionapp.databinding.ActivitySellerSettingBinding;

public class SellerSettingActivity extends AppCompatActivity {

    ActivitySellerSettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.lytChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SellerSettingActivity.this, SellerProfileActivity.class));
            }
        });

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}