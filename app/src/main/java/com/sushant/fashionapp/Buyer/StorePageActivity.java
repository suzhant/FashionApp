package com.sushant.fashionapp.Buyer;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.databinding.ActivityStorePageBinding;

import java.util.ArrayList;

public class StorePageActivity extends AppCompatActivity {

    ActivityStorePageBinding binding;
    CardAdapters adapters;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String storeId, storePic, storeName;
    ArrayList<Product> products = new ArrayList<>();
    ArrayList<Product> searchList = new ArrayList<>();
    ArrayList<Product> tempList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStorePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        storeId = getIntent().getStringExtra("storeId");
        storePic = getIntent().getStringExtra("storePic");
        storeName = getIntent().getStringExtra("storeName");


        Glide.with(this).load(storePic).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(binding.imgStorePic);
        binding.txtStoreName.setText(storeName);
        Query query = database.getReference().child("Products").orderByChild("storeId").equalTo(storeId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    products.add(product);
                }
                tempList.addAll(products);
                adapters.notifyItemInserted(products.size());
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

        binding.edSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = textView.getText().toString().trim();
                    if (!query.isEmpty()) {
                        String processedQuery = removeSpecialChar(query);
                        if (products.size() == 0) {
                            products.addAll(tempList);
                        }
                        search(processedQuery);
                        products.clear();
                        products.addAll(searchList);
                        hideSoftKeyboard();
                        adapters.notifyDataSetChanged();
                        handled = true;
                    }
                }
                return handled;
            }
        });

        binding.edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    products.clear();
                    products.addAll(tempList);
                }
                adapters.notifyDataSetChanged();
            }
        });


        initRecycler();
    }

    private void initRecycler() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        binding.recyclerStoreProducts.setLayoutManager(layoutManager);
        adapters = new CardAdapters(products, this);
        binding.recyclerStoreProducts.setAdapter(adapters);
    }

    private void search(String toString) {
        searchList.clear();
        for (Product p : products) {
            String subsubcat = removeSpecialChar(p.getSubSubCategory());
            if (p.getpName().toLowerCase().contains(toString.toLowerCase()) || p.getDesc().toLowerCase().contains(toString.toLowerCase())
                    || p.getCategory().toLowerCase().contains(toString) || p.getSubCategory().toLowerCase().contains(toString)
                    || subsubcat.toLowerCase().contains(toString) || toString.contains(p.getSeason().toLowerCase())) {
                searchList.add(p);
            }
        }
        binding.edSearch.dismissDropDown();
    }

    private String removeSpecialChar(String query) {
        StringBuilder resultStr = new StringBuilder();
        for (int i = 0; i < query.length(); i++) {
            if (Character.isWhitespace(query.charAt(i))) {
                resultStr.append(" ");
            }
            //comparing alphabets with their corresponding ASCII value
            if (query.charAt(i) > 64 && query.charAt(i) <= 122) //returns true if both conditions returns true
            {
                //adding characters into empty string
                resultStr.append(query.charAt(i));
            }
        }
        return resultStr.toString();
    }

    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}