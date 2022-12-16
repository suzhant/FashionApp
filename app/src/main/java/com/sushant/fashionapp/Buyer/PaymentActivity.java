package com.sushant.fashionapp.Buyer;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.ShopAdapter;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityPaymentBinding;

import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity {

    ActivityPaymentBinding binding;
    ShopAdapter adapter;
    ArrayList<Store> stores = new ArrayList<>();
    ArrayList<String> storeList = new ArrayList<>();
    ArrayList<Store> selectedStoreList = new ArrayList<>();
    FirebaseAuth auth;
    FirebaseDatabase database;
    String storePic, storeName, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storeList = getIntent().getStringArrayListExtra("storeInfo");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.statusLyt.radio1.setImageResource(R.drawable.ic_baseline_check_circle_24);
        binding.statusLyt.radio1.setImageTintList(ContextCompat.getColorStateList(this, R.color.black));
        binding.statusLyt.txt1.setTextColor(ContextCompat.getColor(this, R.color.black));
        binding.statusLyt.radio2.setImageTintList(ContextCompat.getColorStateList(this, R.color.teal_700));
        binding.statusLyt.txt2.setTextColor(ContextCompat.getColor(this, R.color.teal_700));

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        database.getReference().child("Store").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Store store = snapshot1.getValue(Store.class);
                    stores.add(store);
                }

                for (String storeId : storeList) {
                    for (Store s : stores) {
                        if (storeId.equals(s.getStoreId())) {
                            selectedStoreList.add(s);
                            break;
                        }
                    }
                }
                adapter.notifyItemInserted(selectedStoreList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        initRecycler();
    }

    private void initRecycler() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerShops.setLayoutManager(linearLayoutManager);
        adapter = new ShopAdapter(selectedStoreList, this);
        binding.recyclerShops.setAdapter(adapter);
    }
}