package com.sushant.fashionapp.Buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.AddressAdapter;
import com.sushant.fashionapp.Adapters.OrderSummaryAdapter;
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.databinding.ActivityCheckOutAcitivityBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

public class CheckOutAcitivity extends AppCompatActivity {

    ActivityCheckOutAcitivityBinding binding;
    ArrayList<Address> addresses = new ArrayList<>();
    AddressAdapter adapter;
    FirebaseAuth auth;
    FirebaseDatabase database;
    Query query;
    ValueEventListener valueEventListener;
    OrderSummaryAdapter orderSummaryAdapter;
    ArrayList<Store> stores = new ArrayList<>();
    ArrayList<Cart> products = new ArrayList<>();
    HashSet<String> storeIdList = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckOutAcitivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.linearAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddressActivity.class));
            }
        });

        query = database.getReference().child("Shipping Address").orderByChild("uId").equalTo(auth.getUid());
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addresses.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Address address = snapshot1.getValue(Address.class);
                    assert address != null;
                    addresses.add(address);
                    if (address.getDefault()) {
                        Collections.swap(addresses, 0, addresses.indexOf(address));
                    }
                }
//                for (int i = 0; i < addresses.size(); i++) {
//                    Address address = addresses.get(i);
//                    if (address.getDefault()) {
//                        Collections.swap(addresses,0,i);
//                    }
//                }
                adapter.notifyItemInserted(addresses.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        query.addValueEventListener(valueEventListener);

        database.getReference().child("Cart").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                stores.clear();
                storeIdList.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Cart cart = snapshot1.getValue(Cart.class);
                    products.add(cart);
                    storeIdList.add(cart.getStoreId());
                }

                for (String p : storeIdList) {
                    ArrayList<Cart> list = new ArrayList<>();
                    for (Cart product : products) {
                        if (p.equals(product.getStoreId())) {
                            list.add(product);
                        }
                    }
                    Store store = new Store();
                    store.setStoreId(p);
                    store.setProducts(list);
                    stores.add(store);
                }
                orderSummaryAdapter.notifyItemInserted(stores.size());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        initOrderRecycler();
        initRecycler();
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(CheckOutAcitivity.this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerAddress.setLayoutManager(layoutManager);
        adapter = new AddressAdapter(addresses, this);
        binding.recyclerAddress.setAdapter(adapter);
    }

    private void initOrderRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(CheckOutAcitivity.this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerOrder.setLayoutManager(layoutManager);
        orderSummaryAdapter = new OrderSummaryAdapter(stores, this);
        binding.recyclerOrder.setAdapter(orderSummaryAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (query != null) {
            query.removeEventListener(valueEventListener);
        }
    }
}