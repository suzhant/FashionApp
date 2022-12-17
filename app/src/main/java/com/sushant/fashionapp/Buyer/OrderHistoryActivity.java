package com.sushant.fashionapp.Buyer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sushant.fashionapp.databinding.ActivityOrderHistoryBinding;

public class OrderHistoryActivity extends AppCompatActivity {

    ActivityOrderHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}