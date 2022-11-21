package com.sushant.fashionapp.fragments.Buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.ActivitySignIn;
import com.sushant.fashionapp.Buyer.BuyerSettingActivity;
import com.sushant.fashionapp.Buyer.WishListActivity;
import com.sushant.fashionapp.Models.Seller;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.databinding.FragmentAccountBinding;
import com.sushant.fashionapp.seller.SellerHomePage;
import com.sushant.fashionapp.seller.SellerRegistration;

import java.util.Objects;

public class AccountFragment extends Fragment {

    FragmentAccountBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
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
                Seller buyer = snapshot.getValue(Seller.class);
                assert buyer != null;
                if (snapshot.child("sellerId").exists()) {
                    isSeller = true;
                }

                if (snapshot.child("userName").exists()) {
                    String buyerName = buyer.getUserName();
                    binding.txtName.setText(buyerName);
                } else {
                    binding.txtName.setText("Name not registered yet");
                }

                if (snapshot.child("userPhone").exists()) {
                    String phone = buyer.getUserPhone();
                    binding.txtPhone.setText(phone);
                } else {
                    binding.txtPhone.setText("Phone number not registered yet");
                }

                if (snapshot.child("userEmail").exists()) {
                    String email = buyer.getUserEmail();
                    binding.txtEmail.setText(email);
                } else if (snapshot.child("userSecondaryEmail").exists()) {
                    String email = buyer.getUserSecondaryEmail();
                    binding.txtEmail.setText(email);
                } else {
                    binding.txtEmail.setVisibility(View.GONE);
                }


                if (snapshot.child("userPic").exists()) {
                    String buyerPic = buyer.getUserPic();
                    if (getActivity() != null) {
                        Glide.with(getActivity()).load(buyerPic)
                                .placeholder(R.drawable.avatar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(binding.imgPic);
                    }

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

        binding.cardSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), BuyerSettingActivity.class));
            }
        });
        return binding.getRoot();
    }
}