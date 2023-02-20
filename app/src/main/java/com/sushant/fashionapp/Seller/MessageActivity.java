package com.sushant.fashionapp.Seller;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityMessageBinding;
import com.sushant.fashionapp.fragments.Buyer.SettingFragment;
import com.sushant.fashionapp.fragments.Seller.BargainFragment;
import com.sushant.fashionapp.fragments.Seller.ChatFragment;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    ActivityMessageBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    TextView nav_email, nav_username;
    CircleImageView nav_profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

//        binding.viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(), getLifecycle()));
//        String[] titles = {"Bargain", "Chats"};
//        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
//                (tab, position) -> tab.setText(titles[position])
//        ).attach();
//
//        binding.viewPager.setUserInputEnabled(false);


        replaceFragment(new ChatFragment());
        binding.navView.setCheckedItem(R.id.item_chat);

        binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_chat:
                        replaceFragment(new ChatFragment());
                        binding.toolbar.setTitle("Chat");
                        binding.txtMessage.setVisibility(View.VISIBLE);
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.item_bargain:
                        replaceFragment(new BargainFragment());
                        binding.toolbar.setTitle("Bargain");
                        binding.txtMessage.setVisibility(View.GONE);
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.item_setting:
                        replaceFragment(new SettingFragment());
                        binding.toolbar.setTitle("Setting");
                        binding.txtMessage.setVisibility(View.GONE);
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.item_home:
                        startActivity(new Intent(getApplicationContext(), SellerHomePage.class));
                        break;

                }
                return true;
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        View header = binding.navView.getHeaderView(0);
        nav_email = header.findViewById(R.id.nav_email);
        nav_username = header.findViewById(R.id.nav_username);
        nav_profilePic = header.findViewById(R.id.nav_profilePic);

        Query query1 = database.getReference().child("Store").orderByChild("buyerId").equalTo(auth.getUid());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Store store = snapshot1.getValue(Store.class);
                    Glide.with(getApplicationContext()).load(store.getStorePic()).placeholder(R.drawable.avatar).into(nav_profilePic);
                    nav_username.setText(store.getStoreName());
                    nav_email.setText(store.getStoreEmail());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.fragmentContainer.getId(), fragment).setCustomAnimations(
                R.anim.push_left_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.push_left_out  // popExit
        );
        transaction.commit();
    }


    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}