package com.sushant.fashionapp.Buyer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Adapters.SortFilterAdapter;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.SortModel;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityStorePageBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StorePageActivity extends AppCompatActivity {

    ActivityStorePageBinding binding;
    CardAdapters adapters;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String storeId, storePic, storeName;
    ArrayList<Product> products = new ArrayList<>();
    ArrayList<Product> searchList = new ArrayList<>();
    ArrayList<Product> tempList = new ArrayList<>();
    ArrayList<Product> unmodifiedList = new ArrayList<>();
    SortFilterAdapter sortFilterAdapter;
    ArrayList<SortModel> list = new ArrayList<>();
    ItemClickListener itemClickListener;
    String sortBy = "All", category = "All", colour = "All", brand = "All", season = "All";
    public boolean isSorted = false;
    TextView txtClear;
    Query query1, query2;
    ValueEventListener valueEventListener1, valueEventListener2;

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


        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(String item, String type) {
                switch (type) {
                    case "Sort by":
                        sortBy = item;
                        break;
                    case "Category":
                        category = item;
                        break;
                    case "Colour":
                        colour = item;
                        break;
                    case "Brand":
                        brand = item;
                        break;
                    case "Season":
                        season = item;
                        break;
                }
                txtClear.setVisibility(View.VISIBLE);
                isSorted = !sortBy.equals("All") || !category.equals("All") || !colour.equals("All") || !brand.equals("All");
                if (!isSorted) {
                    txtClear.setVisibility(View.GONE);

                }
            }

            @Override
            public <T> void onAddressClick(T Object, boolean b) {

            }
        };


        Glide.with(this).load(storePic).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(binding.imgStorePic);
        binding.txtStoreName.setText(storeName);

        query1 = database.getReference().child("Products").orderByChild("storeId").equalTo(storeId).limitToFirst(20);
        valueEventListener1 = new ValueEventListener() {
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
        };
        query1.addValueEventListener(valueEventListener1);

        query2 = database.getReference().child("Products").orderByChild("storeId").equalTo(storeId);
        valueEventListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                unmodifiedList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    unmodifiedList.add(product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        query2.addValueEventListener(valueEventListener2);

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
                    adapters.notifyItemRangeChanged(0, products.size());
                }

            }
        });

        binding.btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
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
            String subsubcat = removeSpecialChar(p.getArticleType());
            if (p.getpName().toLowerCase().contains(toString.toLowerCase()) || p.getDesc().toLowerCase().contains(toString.toLowerCase())
                    || p.getMasterCategory().toLowerCase().contains(toString) || p.getCategory().toLowerCase().contains(toString)
                    || subsubcat.toLowerCase().contains(toString)) {
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


    private void showBottomSheetDialog() {
        list.clear();
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_sort_filter);
        RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.sortFilterRecycler);
        MaterialButton btnApply = bottomSheetDialog.findViewById(R.id.btnApply);
        txtClear = bottomSheetDialog.findViewById(R.id.txtClear);
        assert recyclerView != null;
        list.add(new SortModel("Sort by", sortBy));
        list.add(new SortModel("Category", category));
        list.add(new SortModel("Colour", colour));
        list.add(new SortModel("Brand", brand));
        //      list.add(new SortModel("Season", season));
//        list.add(new SortModel("Size", "All"));
//        list.add(new SortModel("In stock", "on stock"));
        initSortFilterRecycler(recyclerView);
        bottomSheetDialog.show();


        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (isSorted) {
                    binding.btnBrowse.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.skyBlue));
                    binding.btnBrowse.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    binding.btnBrowse.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                } else {
                    binding.btnBrowse.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    binding.btnBrowse.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    binding.btnBrowse.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.black)));
                }
                if (!isSorted) {
                    products.clear();
                    products.addAll(tempList);
                    adapters.notifyDataSetChanged();
                    bottomSheetDialog.dismiss();
                }
            }
        });
        assert btnApply != null;
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSorted) {
                    binding.btnBrowse.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.skyBlue));
                    binding.btnBrowse.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    binding.btnBrowse.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                } else {
                    binding.btnBrowse.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    binding.btnBrowse.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    binding.btnBrowse.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.black)));
                }


                if (category.equals("All") || sortBy.equals("All") || colour.equals("All") || brand.equals("All")) {
                    products.clear();
                    products.addAll(unmodifiedList);
                }

//                if (!season.equals("All")){
//                    filterSeason(season);
//                }
                if (!brand.equals("All")) {
                    filterBrand(brand);
                }
                if (!category.equals("All")) {
                    filterCategory(category);
                }
                if (!sortBy.equals("All")) {
                    performSort(sortBy);
                }
                if (!colour.equals("All")) {
                    filterColor(colour);
                }


                bottomSheetDialog.dismiss();
            }
        });

        if (isSorted) {
            txtClear.setVisibility(View.VISIBLE);
        } else {
            txtClear.setVisibility(View.GONE);
        }

        txtClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSorted = false;
                resetAll(recyclerView);
                txtClear.setVisibility(View.GONE);
            }
        });
    }


    private void filterBrand(String brand) {
        products.clear();
        Predicate<Product> byBrand = product -> product.getBrandName().equals(brand);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Set<Product> result = unmodifiedList.stream().filter(byBrand)
                    .collect(Collectors.toSet());
            products.addAll(result);
        }
        adapters.notifyDataSetChanged();
    }

    private void resetAll(RecyclerView recyclerView) {
        list.clear();
        sortBy = "All";
        category = "All";
        colour = "All";
        season = "All";
        brand = "All";
        isSorted = false;
        list.add(new SortModel("Sort by", sortBy));
        list.add(new SortModel("Category", category));
        list.add(new SortModel("Colour", colour));
        list.add(new SortModel("Brand", brand));
        //     list.add(new SortModel("Season", season));
//        list.add(new SortModel("Size", "All"));
//        list.add(new SortModel("In stock", "on stock"));
        initSortFilterRecycler(recyclerView);
    }

    private void filterCategory(String cat) {
        category = cat;
        products.clear();
        Predicate<Product> byFemale = product -> product.getCategory().equals(category);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Set<Product> result = unmodifiedList.stream().filter(byFemale)
                    .collect(Collectors.toSet());
            products.addAll(result);
        }
        adapters.notifyDataSetChanged();
    }


    private void filterColor(String color) {
        colour = color;

        ArrayList<Product> list = new ArrayList<>(products);
        products.clear();
        for (Product p : list) {
            for (int i = 0; i < p.getVariants().size(); i++) {
                if (p.getVariants().get(i).getColor().equals(color)) {
                    p.setPreviewPic(p.getVariants().get(i).getPhotos().get(0));
                    p.setVariantIndex(i);
                    products.add(p);
                }
            }

        }

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            List<Product> result = unmodifiedList.stream().filter(byColor)
//                    .collect(Collectors.toList());
//            products.addAll(result);
//        }
        adapters.notifyDataSetChanged();
    }

    private void performSort(String sort) {
        sortBy = sort;
        if (products.isEmpty()) {
            products.addAll(unmodifiedList);
        }
        switch (sortBy) {
            case "Ascending":
                Collections.sort(products, Product.ascending);
                break;
            case "Descending":
                Collections.sort(products, Product.descending);
                break;
            case "Time: new to old":
                Collections.sort(products, Product.newToOld);
                break;
            case "Time: old to new":
                Collections.sort(products, Product.oldToNew);
                break;
            case "Price: low to high":
                Collections.sort(products, Product.lowToHigh);
                break;
            case "Price: high to low":
                Collections.sort(products, Product.highToLow);
                break;
        }
        adapters.notifyItemRangeChanged(0, products.size());
    }

    private void initSortFilterRecycler(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        sortFilterAdapter = new SortFilterAdapter(list, this, itemClickListener, storeId, "shop");
        recyclerView.setAdapter(sortFilterAdapter);
    }

    @Override
    protected void onDestroy() {
        if (query1 != null) {
            query1.removeEventListener(valueEventListener1);
        }
        if (query2 != null) {
            query2.removeEventListener(valueEventListener2);
        }
        super.onDestroy();
    }
}