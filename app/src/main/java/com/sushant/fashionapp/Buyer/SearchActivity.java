package com.sushant.fashionapp.Buyer;

import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.ActivityHomePage;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Adapters.SearchAdapter;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.databinding.ActivitySearchBinding;

import java.util.ArrayList;


public class SearchActivity extends AppCompatActivity {

    ActivitySearchBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<String> list = new ArrayList<>();
    ArrayList<String> searchHistory = new ArrayList<>();
    ArrayList<Product> searchList = new ArrayList<>();
    ArrayList<String> searchQuery = new ArrayList<>();
    ArrayList<Product> products = new ArrayList<>();
    SearchAdapter searchAdapter;
    CardAdapters adapters;
    ItemClickListener itemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        if (getIntent() != null) {
            String text = getIntent().getStringExtra("pName");
            binding.edSearch.setText(text);
        }

        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(String query, String type) {
                if (!query.isEmpty()) {
                    binding.recentLyt.setVisibility(View.GONE);
                    binding.recyclerRecent.setVisibility(View.GONE);
                    binding.recyclerSearchView.setVisibility(View.VISIBLE);
                    String processedQuery = removeSpecialChar(query);
                    searchHistory.add(processedQuery);
                    saveHistory(processedQuery);
                    search(processedQuery);
                    hideSoftKeyboard();
                }
            }
        };

        database.getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    list.add(product.getpName());
                    products.add(product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Search History").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                searchHistory.clear();
                searchQuery.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String history = snapshot1.child("query").getValue(String.class);
                    searchHistory.add(history);
                }
                searchQuery.addAll(searchHistory);
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.edSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = textView.getText().toString().trim();
                    if (!query.isEmpty()) {
                        binding.recentLyt.setVisibility(View.GONE);
                        binding.recyclerRecent.setVisibility(View.GONE);
                        binding.recyclerSearchView.setVisibility(View.VISIBLE);
                        String processedQuery = removeSpecialChar(query);
                        searchHistory.add(processedQuery);
                        saveHistory(processedQuery);
                        //       search(processedQuery);
                        hideSoftKeyboard();
                        Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                        intent.putExtra("pName", query);
                        startActivity(intent);
                        handled = true;
                    }
                }
                return handled;
            }
        });

        initSearchRecycler();
        binding.edSearch.requestFocus();


        binding.edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.recentLyt.setVisibility(View.GONE);
                binding.recyclerRecent.setVisibility(View.VISIBLE);
                binding.recyclerSearchView.setVisibility(View.GONE);
                binding.recyclerSearchView.setVisibility(View.VISIBLE);
                searchQuery.clear();
                for (Product p : products) {
                    if (p.getpName().toLowerCase().contains(charSequence.toString().toLowerCase()) || p.getDesc().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        searchQuery.add(p.getpName());
                    }
                }
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0) {
                    searchQuery.clear();
                    searchQuery.addAll(searchHistory);
                    binding.recentLyt.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SearchActivity.this, ActivityHomePage.class));
            }
        });

//        ArrayAdapter<String> adapter = new
//                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
//
//        binding.edSearch.setAdapter(adapter);
//        binding.edSearch.setDropDownAnchor(binding.divider.getId());
//        binding.edSearch.setThreshold(2);


        binding.txtClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getReference().child("Search History").child(auth.getUid()).removeValue();
            }
        });

        initRecyclerView();


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

    private void saveHistory(String searchHistory) {
        String key = database.getReference().push().getKey();

        database.getReference().child("Search History").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean duplicate = false;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String history = snapshot1.child("query").getValue(String.class);
                    if (history.equals(searchHistory)) {
                        duplicate = true;
                    }
                }
                if (!duplicate) {
                    database.getReference().child("Search History").child(auth.getUid()).child(key).child("query").setValue(searchHistory);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    private void initSearchRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerRecent.setLayoutManager(layoutManager);
        searchAdapter = new SearchAdapter(this, searchQuery, searchHistory, itemClickListener);
        binding.recyclerRecent.setAdapter(searchAdapter);
    }

    private void initRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        binding.recyclerSearchView.setLayoutManager(layoutManager);
        adapters = new CardAdapters(searchList, this);
        binding.recyclerSearchView.setAdapter(adapters);
    }

    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}