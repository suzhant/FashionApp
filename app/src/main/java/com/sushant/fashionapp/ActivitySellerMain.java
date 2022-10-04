package com.sushant.fashionapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationBarView;
import com.sushant.fashionapp.Utils.Dialogs;
import com.sushant.fashionapp.databinding.ActivitySellerMainBinding;
import com.sushant.fashionapp.fragments.Seller.SellerAccountFragment;
import com.sushant.fashionapp.fragments.Seller.SellerChatFragment;
import com.sushant.fashionapp.fragments.Seller.SellerHomeFragment;
import com.sushant.fashionapp.fragments.Seller.SellerStoreFragment;

public class ActivitySellerMain extends AppCompatActivity {

    ActivitySellerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navigationRail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        replaceFragment(new SellerHomeFragment());
                        return true;
                    case R.id.message:
                        replaceFragment(new SellerChatFragment());
                        return true;
                    case R.id.store:
                        replaceFragment(new SellerStoreFragment());
                        return true;
                    case R.id.account:
                        replaceFragment(new SellerAccountFragment());
                        return true;
                }
                return false;
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.sellerfrmLayout, fragment).setCustomAnimations(
                R.anim.push_left_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.push_left_out  // popExit
        );
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (binding.navigationRail.getSelectedItemId() == R.id.home) {
            Dialogs.logoutDialog(ActivitySellerMain.this, this);
        } else {
            binding.navigationRail.setSelectedItemId(R.id.home);
            if (binding.navigationRail.getVisibility() == View.GONE) {
                binding.navigationRail.setVisibility(View.VISIBLE);
            }

        }
    }
}