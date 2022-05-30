package com.sushant.fashionapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.FragmentCartBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class CartFragment extends Fragment {

    FragmentCartBinding binding;
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

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCartBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment();
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
                    binding.btnDelete.setVisibility(View.VISIBLE);
                } else {
                    binding.btnDelete.setVisibility(View.GONE);
                }
            }
        };


        initRecyclerView();
        databaseReference = database.getReference().child("Cart").child(Objects.requireNonNull(auth.getUid()));
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    assert product != null;
                    if (product.getpId() != null) {
                        products.add(product);
                    }
                }
                cartAdapter.notifyDataSetChanged();
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


        binding.btnDelete.setOnClickListener(new View.OnClickListener() {
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
                    binding.btnDelete.setVisibility(View.GONE);
                    Snackbar snackbar = Snackbar.make(requireActivity().findViewById(R.id.cartLayout), "Cart Deleted",
                            Snackbar.LENGTH_SHORT).setAction("Undo", new View.OnClickListener() {
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
                replaceFragment();
            }
        });


        return binding.getRoot();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.cartRecycler.setLayoutManager(layoutManager);
        cartAdapter = new CartAdapter(products, getContext(), productClickListener);
        binding.cartRecycler.setAdapter(cartAdapter);
    }

    private void refreshAdapter() {
        cartAdapter = new CartAdapter(products, getContext(), productClickListener);
        binding.cartRecycler.setAdapter(cartAdapter);
    }

    private void replaceFragment() {
        bottomNavigationView.setSelectedItemId(R.id.page_1);
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        databaseReference.removeEventListener(valueEventListener);
        super.onDestroy();
    }

}