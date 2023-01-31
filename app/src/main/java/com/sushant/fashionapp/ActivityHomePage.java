package com.sushant.fashionapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sushant.fashionapp.Buyer.CartActivity;
import com.sushant.fashionapp.Buyer.MessageActivity;
import com.sushant.fashionapp.Utils.Dialogs;
import com.sushant.fashionapp.databinding.ActivityHomePageBinding;
import com.sushant.fashionapp.fragments.Buyer.AccountFragment;
import com.sushant.fashionapp.fragments.Buyer.HomeFragment;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Objects;


public class ActivityHomePage extends AppCompatActivity implements DefaultLifecycleObserver {

    ActivityHomePageBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    double cartNumber;
    ValueEventListener cartListener, eventListener;
    DatabaseReference cartRef, statusRef, infoConnected;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        statusRef = database.getReference().child("Users").child(auth.getUid());
        manageConnection();


        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String authId = sharedPreferences.getString("authId", "");
        SharedPreferences.Editor authEditor = getSharedPreferences("data", MODE_PRIVATE).edit();
        if (authId != null) {
            authEditor.putBoolean("enableBiometric", authId.equals(auth.getUid()));
        }
        authEditor.apply();


        BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.page_3);
        SharedPreferences.Editor editor = getSharedPreferences("userData", MODE_PRIVATE).edit();
        editor.putBoolean("isSeller", false);
        editor.apply();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                String token = task.getResult();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                String uid = user.getUid();
                HashMap<String, Object> obj = new HashMap<>();
                obj.put("Token", token);
                database.getReference().child("Users").child(uid).updateChildren(obj);
            }
        });

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
                        startActivity(new Intent(ActivityHomePage.this, MessageActivity.class));
                        //   replaceFragment(new MessageFragment());
                        break;
                    case R.id.page_3:
                        startActivity(new Intent(ActivityHomePage.this, CartActivity.class));
                        // replaceFragment(new CartFragment());
                        break;
                    case R.id.page_4:
                        replaceFragment(new AccountFragment());
                        break;

                }
                return true;
            }
        });

        cartRef = database.getReference().child("Cart").child(Objects.requireNonNull(auth.getUid()));
        cartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartNumber = snapshot.getChildrenCount();
                if (cartNumber > 0) {
                    badge.setVisible(true);
                    badge.setNumber((int) cartNumber);
                } else {
                    badge.setVisible(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        cartRef.addValueEventListener(cartListener);


    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frmLayout, fragment).setCustomAnimations(
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
            Dialogs.logoutDialog(ActivityHomePage.this, this);
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
        if (cartRef != null) {
            cartRef.addValueEventListener(cartListener);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (cartRef != null) {
            cartRef.removeEventListener(cartListener);
        }
        if (infoConnected != null) {
            infoConnected.removeEventListener(eventListener);
        }
        super.onDestroy();
    }

    void updateStatus(String status) {
        HashMap<String, Object> obj = new HashMap<>();
        obj.put("Status", status);
        statusRef.child("Connection").updateChildren(obj);
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

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declareLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength);
    }
}