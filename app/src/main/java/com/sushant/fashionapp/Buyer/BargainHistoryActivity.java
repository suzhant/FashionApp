package com.sushant.fashionapp.Buyer;

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
import com.sushant.fashionapp.Adapters.BuyerBargainAdapter;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.databinding.ActivityBargainHistoryBinding;

import java.util.ArrayList;

public class BargainHistoryActivity extends AppCompatActivity {

    BuyerBargainAdapter adapter;
    ArrayList<Bargain> list = new ArrayList<>();
    FirebaseDatabase database;
    FirebaseAuth auth;

    ActivityBargainHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBargainHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        database.getReference().child("Bargain").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Bargain bargain = snapshot1.getValue(Bargain.class);
                    if (auth.getUid().equals(bargain.getBuyerId())) {
                        list.add(bargain);
                    }
                }
                adapter.notifyItemInserted(list.size());
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerBargainHistory.setLayoutManager(layoutManager);
        adapter = new BuyerBargainAdapter(list, this);
        binding.recyclerBargainHistory.setAdapter(adapter);
    }
}