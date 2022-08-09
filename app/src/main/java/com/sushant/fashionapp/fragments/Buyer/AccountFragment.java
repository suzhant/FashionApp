package com.sushant.fashionapp.fragments.Buyer;

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
import com.sushant.fashionapp.ActivitySignIn;
import com.sushant.fashionapp.Buyer.WishListActivity;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.databinding.FragmentAccountBinding;
import com.sushant.fashionapp.seller.SellerHomePage;
import com.sushant.fashionapp.seller.SellerRegistration;

import java.util.Objects;

public class AccountFragment extends Fragment {

    FragmentAccountBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String sellerId;
    boolean isSeller = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

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
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("sellerId").exists()) {
                    sellerId = snapshot.child("sellerId").getValue(String.class);
                    database.getReference().child("Seller").child(sellerId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        isSeller = true;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.cardSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSeller) {
                    startActivity(new Intent(getContext(), SellerRegistration.class));
                } else {
                    Toast.makeText(getContext(), "You are a seller", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getContext(), SellerHomePage.class));
                }
            }
        });

        binding.cardWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), WishListActivity.class));
            }
        });
        return binding.getRoot();
    }
}