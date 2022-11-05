package com.sushant.fashionapp.seller;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDisplayProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        initRecyclerView();


    }

    private void initRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        binding.viewMoreRecycler.setLayoutManager(layoutManager);
        adapters = new CardAdapters(products, this);
        binding.viewMoreRecycler.setAdapter(adapters);
    }
}