package com.sushant.fashionapp.Buyer;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.databinding.ActivityStorePageBinding;

import java.util.ArrayList;

public class StorePageActivity extends AppCompatActivity {

    ActivityStorePageBinding binding;
    CardAdapters adapters;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String storeId, storePic, storeName;
    ArrayList<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStorePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        storeId = getIntent().getStringExtra("storeId");
        storePic = getIntent().getStringExtra("storePic");
        storeName = getIntent().getStringExtra("storeName");


        Glide.with(this).load(storePic).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(binding.imgStorePic);
        binding.txtStoreName.setText(storeName);
        Query query = database.getReference().child("Products").orderByChild("storeId").equalTo(storeId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    products.add(product);
                }
                adapters.notifyItemInserted(products.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        initRecycler();
    }

    private void initRecycler() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        binding.recyclerStoreProducts.setLayoutManager(layoutManager);
        adapters = new CardAdapters(products, this);
        binding.recyclerStoreProducts.setAdapter(adapters);
    }
}