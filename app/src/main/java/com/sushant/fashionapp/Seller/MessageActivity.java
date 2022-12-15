package com.sushant.fashionapp.Seller;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.FragmentAdapter;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.databinding.ActivityMessageBinding;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MessageActivity extends AppCompatActivity {

    ActivityMessageBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(), getLifecycle()));
        String[] titles = {"Bargain", "Chats"};
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(titles[position])
        ).attach();

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

}