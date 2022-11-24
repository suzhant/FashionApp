package com.sushant.fashionapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sushant.fashionapp.fragments.Seller.BargainFragment;
import com.sushant.fashionapp.fragments.Seller.ChatFragment;

public class FragmentAdapter extends FragmentStateAdapter {
    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new ChatFragment();
        }
        return new BargainFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }


}
