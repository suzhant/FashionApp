package com.sushant.fashionapp.Seller;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayoutMediator;
import com.sushant.fashionapp.Adapters.FragmentAdapter;
import com.sushant.fashionapp.databinding.ActivityMessageBinding;

public class MessageActivity extends AppCompatActivity {

    ActivityMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(), getLifecycle()));
        String[] titles = {"Bargain", "Chats"};
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(titles[position])
        ).attach();

    }

}