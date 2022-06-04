package com.sushant.fashionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CartAdapter;
import com.sushant.fashionapp.Inteface.ProductClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.databinding.ActivityCartBinding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class CartActivity extends AppCompatActivity {

    ActivityCartBinding binding;
    CartAdapter cartAdapter;
    ArrayList<Product> products = new ArrayList<>();
    FirebaseDatabase database;
    FirebaseAuth auth;
    ProductClickListener productClickListener;
    ArrayList<Product> checkedProducts = new ArrayList<>();
    ArrayList<Product> oldProductList = new ArrayList<>();
    ValueEventListener valueEventListener;
    DatabaseReference databaseReference;
    int size = 0;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        productClickListener = new ProductClickListener() {
            @Override
            public void onClick(Product product, boolean b) {
                if (b) {
                    checkedProducts.add(product);
                    size++;
                } else {
                    checkedProducts.remove(product);
                    size--;
                }
                if (size > 0) {
                    binding.imgDelete.setVisibility(View.VISIBLE);
                } else {
                    binding.imgDelete.setVisibility(View.GONE);
                }
            }
        };


        initRecyclerView();
        databaseReference = database.getReference().child("Cart").child(Objects.requireNonNull(auth.getUid()));
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                int sum = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    assert product != null;
                    if (product.getpId() != null) {
                        products.add(product);
                        sum = sum + product.getpPrice() * product.getQuantity();
                    }
                }
                cartAdapter.notifyDataSetChanged();
                binding.txtPrice.setText(MessageFormat.format("Rs. {0}", sum));
//                    cartAdapter.updateProductList(products);
//                    oldProductList.clear();
//                    oldProductList.addAll(products);
                if (products.size() == 0) {
                    binding.emptyCartLyt.setVisibility(View.VISIBLE);
                    binding.cardView.setVisibility(View.GONE);
                } else {
                    binding.cardView.setVisibility(View.VISIBLE);
                    binding.emptyCartLyt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);


        binding.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (size > 0) {
                    for (Product p : checkedProducts) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put(p.getpId(), null);
                        database.getReference().child("Cart").child(auth.getUid()).updateChildren(map);
                    }
                    refreshAdapter();
                    size = 0;
                    binding.imgDelete.setVisibility(View.GONE);
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.cartLayout), "Cart Deleted",
                            Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            for (Product p : checkedProducts) {
                                database.getReference().child("Cart").child(auth.getUid()).child(p.getpId()).setValue(p);
                            }
                            checkedProducts.clear();
                        }
                    });
                    snackbar.show();
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            //see Snackbar.Callback docs for event details
                            checkedProducts.clear();
                        }

                        @Override
                        public void onShown(Snackbar snackbar) {

                        }
                    });
                }
            }
        });

        binding.btnShopNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CartActivity.this, ActivityHomePage.class));
            }
        });

    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(CartActivity.this, LinearLayoutManager.VERTICAL, false);
        binding.cartRecycler.setLayoutManager(layoutManager);
        cartAdapter = new CartAdapter(products, CartActivity.this, productClickListener);
        binding.cartRecycler.setAdapter(cartAdapter);
    }

    private void refreshAdapter() {
        cartAdapter = new CartAdapter(products, CartActivity.this, productClickListener);
        binding.cartRecycler.setAdapter(cartAdapter);
    }


    @Override
    public void onDestroy() {
        databaseReference.removeEventListener(valueEventListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}