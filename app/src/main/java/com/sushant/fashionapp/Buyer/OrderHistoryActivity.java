package com.sushant.fashionapp.Buyer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayoutMediator;
import com.sushant.fashionapp.Adapters.OrderHistoryFragmentAdapter;
import com.sushant.fashionapp.databinding.ActivityOrderHistoryBinding;

public class OrderHistoryActivity extends AppCompatActivity {

    ActivityOrderHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.viewPager.setAdapter(new OrderHistoryFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
        String[] titles = {"Pending", "Completed", "Cancelled"};
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(titles[position])
        ).attach();

    }
}