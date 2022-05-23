package com.sushant.fashionapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.databinding.ActivityHomePageBinding;
import com.sushant.fashionapp.fragments.AccountFragment;
import com.sushant.fashionapp.fragments.CartFragment;
import com.sushant.fashionapp.fragments.HomeFragment;
import com.sushant.fashionapp.fragments.WishListFragment;


public class ActivityHomePage extends AppCompatActivity {

    ActivityHomePageBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        replaceFragment(new HomeFragment());

//        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
//            @Override
//            public boolean onItemSelect(int i) {
//
//                switch (i){
//                    case 0:
//                        replaceFragment(new HomeFragment());
//                        break;
//                    case 1:
//                        replaceFragment(new WishListFragment());
//                        break;
//                    case 2:
//                        replaceFragment(new CartFragment());
//                        break;
//                    case 3:
//                        replaceFragment(new AccountFragment());
//                        break;
//                }
//                return false;
//            }
//        });

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_1:
                        replaceFragment(new HomeFragment());
                        break;
                    case R.id.page_2:
                        replaceFragment(new WishListFragment());
                        break;
                    case R.id.page_3:
                        replaceFragment(new CartFragment());
                        break;
                    case R.id.page_4:
                        replaceFragment(new AccountFragment());
                        break;

                }
                return true;
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frmLayout, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        logoutDialog();
    }


    private void logoutDialog() {
        if (CheckConnection.isOnline(ActivityHomePage.this)) {
            new MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme)
                    .setMessage("Do you want to logout?")
                    .setTitle("Logout")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(getApplicationContext(), ActivitySignIn.class));
                            auth.signOut();
                            finishAfterTransition();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        } else {
            CheckConnection.showCustomDialog(ActivityHomePage.this);
        }
    }
}