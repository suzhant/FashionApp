package com.sushant.fashionapp.fragments.Seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;
import com.sushant.fashionapp.Adapters.FragmentAdapter;
import com.sushant.fashionapp.databinding.FragmentSellerChatBinding;


public class SellerChatFragment extends Fragment {

    FragmentSellerChatBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSellerChatBinding.inflate(inflater, container, false);


        binding.viewPager.setAdapter(new FragmentAdapter(getChildFragmentManager(), getLifecycle()));
        String[] titles = {"Bargain", "Chats"};
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(titles[position])
        ).attach();


        return binding.getRoot();
    }
}