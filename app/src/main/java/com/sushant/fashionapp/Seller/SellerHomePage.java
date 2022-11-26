package com.sushant.fashionapp.Seller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.Dialogs;
import com.sushant.fashionapp.databinding.ActivitySellerHomePageBinding;
import com.sushant.fashionapp.fragments.Seller.SellerAccountFragment;
import com.sushant.fashionapp.fragments.Seller.SellerHomeFragment;
import com.sushant.fashionapp.fragments.Seller.SellerStoreFragment;

public class SellerHomePage extends AppCompatActivity {

    ActivitySellerHomePageBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        replaceFragment(new SellerHomeFragment());
        SharedPreferences.Editor editor = getSharedPreferences("userData", MODE_PRIVATE).edit();
        editor.putBoolean("isSeller", true);
        editor.apply();

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_1:
                        replaceFragment(new SellerHomeFragment());
                        break;
                    case R.id.page_2:
                        startActivity(new Intent(SellerHomePage.this, MessageActivity.class));
                        // replaceFragment(new SellerChatFragment());
                        break;
                    case R.id.page_3:
                        replaceFragment(new SellerStoreFragment());
                        // replaceFragment(new CartFragment());
                        break;
                    case R.id.page_4:
                        replaceFragment(new SellerAccountFragment());
                        break;

                }
                return true;
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
        if (binding.bottomNavigation.getSelectedItemId() == R.id.page_1) {
            Dialogs.logoutDialog(SellerHomePage.this, this);
        } else {
            binding.bottomNavigation.setSelectedItemId(R.id.page_1);
            if (binding.bottomNavigation.getVisibility() == View.GONE) {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    protected void onResume() {
        binding.bottomNavigation.setSelectedItemId(R.id.page_1);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //    private void logoutDialog() {
//        if (CheckConnection.isOnline(ActivityHomePage.this)) {
//            new MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme)
//                    .setMessage("Do you want to logout?")
//                    .setTitle("Logout")
//                    .setCancelable(false)
//                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            startActivity(new Intent(getApplicationContext(), ActivitySignIn.class));
//                            auth.signOut();
//                            finishAfterTransition();
//                        }
//                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            }).show();
//        } else {
//            CheckConnection.showCustomDialog(ActivityHomePage.this);
//        }
//    }
}