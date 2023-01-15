package com.sushant.fashionapp.Seller;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.databinding.ActivityDisplayProductBinding;

import java.util.ArrayList;

public class DisplayProductActivity extends AppCompatActivity {

    ActivityDisplayProductBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    CardAdapters adapters;
    ArrayList<Product> products = new ArrayList<>();
    String sellerId, storeId;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDisplayProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        reference = database.getReference().child("Users").child(auth.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    storeId = snapshot.child("storeId").getValue(String.class);
                    Log.d("storeId", storeId);
                    Query query = database.getReference().child("Products").orderByChild("storeId").equalTo(storeId).limitToFirst(20);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Product product = snapshot1.getValue(Product.class);
                                assert product != null;
                                products.add(product);
//                                    if (product.getStoreId().equals(storeId)) {
//
//                                    }
                            }
                            adapters.notifyItemInserted(products.size());
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


        initRecyclerView();


    }

    private void initRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        binding.viewMoreRecycler.setLayoutManager(layoutManager);
        adapters = new CardAdapters(products, this);
        binding.viewMoreRecycler.setAdapter(adapters);
    }
}