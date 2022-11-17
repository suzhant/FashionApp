package com.sushant.fashionapp.Buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.databinding.ActivitySearchResultBinding;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity {

    ActivitySearchResultBinding binding;
    CardAdapters adapters;
    ArrayList<Product> searchList = new ArrayList<>();
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Product> products = new ArrayList<>();
    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        query = getIntent().getStringExtra("pName");


        database.getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    products.add(product);
                }
                search(query);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.edSearch.setText(query);
        initRecyclerView();

        binding.edSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Intent intent = new Intent(SearchResultActivity.this, SearchActivity.class);
                    intent.putExtra("pName", query);
                    startActivity(intent);
                }
            }
        });

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        binding.recyclerSearchView.setLayoutManager(layoutManager);
        adapters = new CardAdapters(searchList, this);
        binding.recyclerSearchView.setAdapter(adapters);
    }

    private void search(String toString) {
        searchList.clear();
        for (Product p : products) {
            if (p.getpName().toLowerCase().contains(toString.toLowerCase()) || p.getDesc().toLowerCase().contains(toString.toLowerCase())) {
                searchList.add(p);
            }
        }
        binding.edSearch.dismissDropDown();
        adapters.notifyDataSetChanged();
    }

//    public void setHistoryChips(ArrayList<String> histories) {
//        binding.chipGroup.removeAllViews();
//        for (String history : histories) {
//            Chip mChip = (Chip) this.getLayoutInflater().inflate(R.layout.item_chip_history, null, false);
//            mChip.setText(history);
//            int paddingDp = (int) TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP, 10,
//                    getResources().getDisplayMetrics()
//            );
//            mChip.setPadding(paddingDp, 0, paddingDp, 0);
//            mChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    if (b) {
//                        search(mChip.getText().toString());
//                    }
//                }
//            });
//            binding.chipGroup.addView(mChip);
//        }
//    }


}