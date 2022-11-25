package com.sushant.fashionapp.fragments.Seller;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.ActivityHomePage;
import com.sushant.fashionapp.ActivitySignIn;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.Utils.TextUtils;
import com.sushant.fashionapp.databinding.FragmentSellerAccountBinding;

public class SellerAccountFragment extends Fragment {

    FragmentSellerAccountBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String sellerId, name, email, phone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSellerAccountBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sellerId = snapshot.child("sellerId").getValue(String.class);
                database.getReference().child("Seller").child(sellerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        name = snapshot.child("userName").getValue(String.class);
                        email = snapshot.child("userEmail").getValue(String.class);
                        phone = snapshot.child("userPhone").getValue(String.class);
                        binding.txtEmail.setText(email);
                        binding.txtPhone.setText(phone);
                        String storeId = snapshot.child("storeId").getValue(String.class);
                        database.getReference().child("Store").child(storeId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String storeName = snapshot.child("storeName").getValue(String.class);
                                binding.txtName.setText(TextUtils.captializeAllFirstLetter(storeName));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckConnection.isOnline(getContext())) {
                    auth.signOut();
                    Intent intentLogout = new Intent(getContext(), ActivitySignIn.class);
                    intentLogout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentLogout);
                    Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                } else {
                    CheckConnection.showCustomDialog(getContext());
                }

            }
        });

        binding.cardSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ActivityHomePage.class));
            }
        });
        return binding.getRoot();
    }
}