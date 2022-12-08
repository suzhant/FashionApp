package com.sushant.fashionapp.Buyer;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.BuyerBargainAdapter;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.databinding.ActivityBargainHistoryBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class BargainHistoryActivity extends AppCompatActivity {

    BuyerBargainAdapter adapter;
    ArrayList<Bargain> list = new ArrayList<>();
    FirebaseDatabase database;
    FirebaseAuth auth;
    ActivityBargainHistoryBinding binding;
    ValueEventListener valueEventListener;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBargainHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        reference = database.getReference().child("Bargain");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Bargain bargain = snapshot1.getValue(Bargain.class);
                    if (bargain.getBuyerId().equals(auth.getUid())) {
                        list.add(bargain);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reference.addValueEventListener(valueEventListener);

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        database.getReference().child("Bargain").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        if (bargain.getBuyerId().equals(auth.getUid()) && bargain.getStatus().equals("accepted")) {
                            Long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS); //2 day old
                            if (bargain.getTimestamp() < cutoff) {
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
                                                    database.getReference().child("Bargain").child(bargain.getBargainId()).removeValue();
                                                }
                                            });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

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


        initRecycler();
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerBargainHistory.setLayoutManager(layoutManager);
        adapter = new BuyerBargainAdapter(list, this);
        binding.recyclerBargainHistory.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reference != null) {
            reference.removeEventListener(valueEventListener);
        }
    }

}