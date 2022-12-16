package com.sushant.fashionapp.Buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.AddressAdapter;
import com.sushant.fashionapp.Adapters.OrderSummaryAdapter;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityCheckOutAcitivityBinding;

import java.text.MessageFormat;
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
    ArrayList<String> storeIds = new ArrayList<>();
    ItemClickListener itemClickListener;
    boolean isSelected;
    Address addressInfo;

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

        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(String item, String type) {

            }

            @Override
            public void onAddressClick(Address address, boolean b) {
                //   binding.btnPlaceOrder.setEnabled(b);
                isSelected = b;
                addressInfo = address;
            }
        };

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
                        if (addresses.indexOf(address) != 0) {
                            Collections.swap(addresses, 0, addresses.indexOf(address));
                        }
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

                //fetching products from cart and unique storeIds
                int total = 0;
                int price;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Cart cart = snapshot1.getValue(Cart.class);
                    products.add(cart);
                    assert cart != null;
                    if (cart.getBargainPrice() != null) {
                        price = cart.getBargainPrice() * cart.getQuantity();
                    } else {
                        price = cart.getpPrice() * cart.getQuantity();
                    }
                    total = total + price;
                    storeIdList.add(cart.getStoreId());
                }
                int deliverCharge = storeIdList.size() * 70;
                binding.txtPrice.setText(MessageFormat.format("Total: Rs. {0}", total + deliverCharge));

                //grouping products by stores and adding it to list
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
                storeIds.addAll(storeIdList);
                orderSummaryAdapter.notifyItemInserted(stores.size());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSelected) {
                    Snackbar.make(findViewById(R.id.parent), "Please select delivery address", Snackbar.LENGTH_SHORT).setAnchorView(R.id.linearLayout8).show();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                intent.putExtra("addressInfo", addressInfo);
                intent.putExtra("storeInfo", storeIds);
                startActivity(intent);
            }
        });

        binding.statusLyt.radio1.setImageTintList(ContextCompat.getColorStateList(this, R.color.teal_700));
        binding.statusLyt.txt1.setTextColor(ContextCompat.getColor(this, R.color.teal_700));

        initOrderRecycler();
        initRecycler();
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(CheckOutAcitivity.this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerAddress.setLayoutManager(layoutManager);
        adapter = new AddressAdapter(addresses, this, itemClickListener);
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