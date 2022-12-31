package com.sushant.fashionapp.fragments.Buyer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.Buyer;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.FragmentMessageBinding;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageFragment extends Fragment {


    FragmentMessageBinding binding;
    TextView nav_email, nav_username;
    CircleImageView nav_profilePic;
    FirebaseDatabase database;
    FirebaseAuth auth;

    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMessageBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


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
                        break;

                    case R.id.item_setting:
                        replaceFragment(new SettingFragment());
                        binding.toolbar.setTitle("Setting");
                        binding.txtMessage.setVisibility(View.GONE);
                        break;

                }
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(requireActivity(), binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        View header = binding.navView.getHeaderView(0);
        nav_email = header.findViewById(R.id.nav_email);
        nav_username = header.findViewById(R.id.nav_username);
        nav_profilePic = header.findViewById(R.id.nav_profilePic);

        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Buyer buyer = snapshot.getValue(Buyer.class);
                Glide.with(requireContext()).load(buyer.getUserPic()).placeholder(R.drawable.avatar).into(nav_profilePic);
                nav_username.setText(buyer.getUserName());
                nav_email.setText(buyer.getUserEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return binding.getRoot();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.fragmentContainer.getId(), fragment).setCustomAnimations(
                R.anim.push_left_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.push_left_out  // popExit
        );
        transaction.commit();
    }

}