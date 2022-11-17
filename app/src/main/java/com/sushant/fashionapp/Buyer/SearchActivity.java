package com.sushant.fashionapp.Buyer;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivitySearchBinding;

import java.util.ArrayList;


public class SearchActivity extends AppCompatActivity {

    ActivitySearchBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<String> list = new ArrayList<>();
    ArrayList<String> searchHistory = new ArrayList<>();
    ArrayList<Product> products = new ArrayList<>();
    ArrayList<Product> searchList = new ArrayList<>();
    CardAdapters adapters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

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
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String history = snapshot1.child("query").getValue(String.class);
                    searchHistory.add(history);
                }
                setHistoryChips(searchHistory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ArrayAdapter<String> adapter = new
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

        binding.edSearch.setAdapter(adapter);
        binding.edSearch.setDropDownAnchor(binding.divider.getId());
        binding.edSearch.setThreshold(2);

        initRecyclerView();

        binding.edSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = textView.getText().toString().trim();
                    String processedQuery = removeSpecialChar(query);
                    search(processedQuery);
                    searchHistory.add(processedQuery);
                    saveHistory(processedQuery);
                    hideSoftKeyboard();
                    handled = true;
                }
                return handled;
            }
        });


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

    public void setHistoryChips(ArrayList<String> histories) {
        binding.chipGroup.removeAllViews();
        for (String history : histories) {
            Chip mChip = (Chip) this.getLayoutInflater().inflate(R.layout.item_chip_history, null, false);
            mChip.setText(history);
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics()
            );
            mChip.setPadding(paddingDp, 0, paddingDp, 0);
            mChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        search(mChip.getText().toString());
                    }
                }
            });
            binding.chipGroup.addView(mChip);
        }
    }


}