package com.sushant.fashionapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.databinding.ActivityProductDetailsBinding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ActivityProductDetails extends AppCompatActivity {

    ActivityProductDetailsBinding binding;
    int price, stock, pPic;
    boolean isTextViewClicked = false;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String pName, sName, pId;
    boolean added;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        price = getIntent().getIntExtra("pPrice", 0);
        stock = getIntent().getIntExtra("stock", 0);
        pPic = getIntent().getIntExtra("pPic", 0);
        pName = getIntent().getStringExtra("pName");
        sName = getIntent().getStringExtra("sName");
        pId = getIntent().getStringExtra("pId");
        binding.txtPrice.setText(MessageFormat.format("Rs. {0}", price));
        binding.txtPrdtName.setText(pName);
        binding.txtStoreName.setText(sName);


        List<SlideModel> list = new ArrayList<>();
        list.add(new SlideModel(R.drawable.red_dress3, null));
        list.add(new SlideModel(R.drawable.red_skirt, null));
        list.add(new SlideModel(R.drawable.red_dress2, null));
        binding.imgSlider.setImageList(list);


        binding.txtDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTextViewClicked) {
                    binding.txtDescription.setMaxLines(Integer.MAX_VALUE);
                    isTextViewClicked = true;
                } else {
                    binding.txtDescription.setMaxLines(2);
                    isTextViewClicked = false;
                }
            }
        });

        database.getReference().child("Cart").child(auth.getUid()).child(pId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.btnAddCart.setEnabled(false);
                    binding.btnAddCart.setText("Added to Cart");
                    added = true;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!added) {
                    addProductToCart();
                }
            }
        });
    }

    private void addProductToCart() {
        Product product = new Product(pId, pName, pPic, price, sName, stock);
        database.getReference().child("Cart").child(auth.getUid()).child(pId).setValue(product);
        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
    }
}