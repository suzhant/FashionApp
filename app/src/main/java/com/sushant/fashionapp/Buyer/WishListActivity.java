package com.sushant.fashionapp.Buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.ActivityHomePage;
import com.sushant.fashionapp.Adapters.WishListAdapter;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.databinding.ActivityWishListBinding;

import java.util.ArrayList;

public class WishListActivity extends AppCompatActivity {

    ActivityWishListBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Product> products = new ArrayList<>();
    WishListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWishListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        initWishListRecycler();
        database.getReference().child("WishList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    products.add(product);
                }
                adapter.notifyDataSetChanged();
                if (products.isEmpty()) {
                    binding.emptyWishLstLyt.setVisibility(View.VISIBLE);
                    binding.fabAddToCart.setVisibility(View.GONE);
                } else {
                    binding.emptyWishLstLyt.setVisibility(View.GONE);
                    binding.fabAddToCart.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.btnShopNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ActivityHomePage.class));
            }
        });
    }

    private void initWishListRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        adapter = new WishListAdapter(products, this);
        binding.wishListRecycler.setLayoutManager(layoutManager);
        binding.wishListRecycler.setAdapter(adapter);
    }
}