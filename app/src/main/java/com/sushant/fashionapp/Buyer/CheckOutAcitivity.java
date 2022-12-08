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
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.databinding.ActivityCheckOutAcitivityBinding;

import java.util.ArrayList;
import java.util.Objects;

public class CheckOutAcitivity extends AppCompatActivity {

    ActivityCheckOutAcitivityBinding binding;
    ArrayList<Address> addresses = new ArrayList<>();
    AddressAdapter adapter;
    FirebaseAuth auth;
    FirebaseDatabase database;

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

        binding.txtAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddressActivity.class));
            }
        });

        Query query = database.getReference().child("Shipping Address").orderByChild("uId").equalTo(auth.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addresses.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Address address = snapshot1.getValue(Address.class);
                    addresses.add(address);
                }
                adapter.notifyItemInserted(addresses.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        initRecycler();
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(CheckOutAcitivity.this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerAddress.setLayoutManager(layoutManager);
        adapter = new AddressAdapter(addresses, this);
        binding.recyclerAddress.setAdapter(adapter);
    }

}