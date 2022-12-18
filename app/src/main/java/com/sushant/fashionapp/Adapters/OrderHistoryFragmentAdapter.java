package com.sushant.fashionapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sushant.fashionapp.fragments.Buyer.OrderCancelledFragment;
import com.sushant.fashionapp.fragments.Buyer.OrderCompletedFragment;
import com.sushant.fashionapp.fragments.Buyer.OrderPendingFragment;

public class OrderHistoryFragmentAdapter extends FragmentStateAdapter {

    public OrderHistoryFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new OrderCompletedFragment();
            case 2:
                return new OrderCancelledFragment();
            case 0:
            default:
                return new OrderPendingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
