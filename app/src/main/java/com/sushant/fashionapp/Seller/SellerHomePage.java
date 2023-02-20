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
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.Dialogs;
import com.sushant.fashionapp.databinding.ActivitySellerHomePageBinding;
import com.sushant.fashionapp.fragments.Seller.SellerAccountFragment;
import com.sushant.fashionapp.fragments.Seller.SellerHomeFragment;
import com.sushant.fashionapp.fragments.Seller.SellerStoreFragment;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SellerHomePage extends AppCompatActivity implements DefaultLifecycleObserver {

    ActivitySellerHomePageBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    DatabaseReference statusRef, infoConnected;
    ValueEventListener eventListener;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        sharedPreferences = getSharedPreferences("store", MODE_PRIVATE);
        String storeId = sharedPreferences.getString("storeId", "");
        statusRef = database.getReference().child("Store").child(storeId);

        manageConnection();

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

        Query query = database.getReference().child("Bargain").orderByChild("buyerId").equalTo(auth.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Bargain bargain = snapshot1.getValue(Bargain.class);
                        assert bargain != null;
                        boolean isBlocked = bargain.getBlocked();
                        if (isBlocked) {
                            long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES); //1 day old
                            if (bargain.getTimestamp() < cutoff) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("blocked", false);
                                map.put("noOfTries", 5);
                                database.getReference().child("Bargain").child(bargain.getBargainId()).updateChildren(map);
                            }
                        }
                        if (bargain.getStatus().equals("accepted")) {
                            Long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS); //2 day old
                            if (bargain.getTimestamp() < cutoff) {
                                database.getReference().child("Bargain").child(bargain.getBargainId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Query query = database.getReference().child("Cart").child(auth.getUid()).orderByChild("pId").equalTo(bargain.getProductId());
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                                                    Cart cart = snapshot11.getValue(Cart.class);
                                                    HashMap<String, Object> map = new HashMap<>();
                                                    map.put("bargainPrice", null);
                                                    database.getReference().child("Cart").child(auth.getUid())
                                                            .child(cart.getVariantPId()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                }
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        if (infoConnected != null) {
            infoConnected.removeEventListener(eventListener);
        }
    }


    private void manageConnection() {
        final DatabaseReference status = statusRef.child("Connection").child("Status");
        final DatabaseReference lastOnlineRef = statusRef.child("Connection").child("lastOnline");
        infoConnected = database.getReference(".info/connected");

        eventListener = infoConnected.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                assert connected != null;
                if (connected) {
                    status.setValue("online");
                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                } else {
                    status.onDisconnect().setValue("offline");
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        // app moved to foreground
        if (auth.getCurrentUser() != null) {
            updateStatus("online");

        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        // app moved to background
        if (auth.getCurrentUser() != null) {
            updateStatus("offline");
        }
    }

    void updateStatus(String status) {
        HashMap<String, Object> obj = new HashMap<>();
        obj.put("Status", status);
        statusRef.child("Connection").updateChildren(obj);
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