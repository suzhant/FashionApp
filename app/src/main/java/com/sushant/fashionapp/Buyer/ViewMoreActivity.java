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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.sushant.fashionapp.Utils.AutoFitGridLayoutManager;
import com.sushant.fashionapp.databinding.ActivityViewMoreBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ViewMoreActivity extends AppCompatActivity {

    ActivityViewMoreBinding binding;
    CardAdapters adapters;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Product> products = new ArrayList<>();
    HashSet<Product> unmodifiedList = new LinkedHashSet<>();
    HashSet<String> cat_list = new LinkedHashSet<>();
    SortFilterAdapter sortFilterAdapter;
    ArrayList<SortModel> list = new ArrayList<>();
    ItemClickListener itemClickListener;
    String sortBy = "All", category = "All", season = "All";
    public boolean isSorted = false;
    TextView txtClear;
    String from;
    Query query1;
    String catName;
    ArrayList<Product> searchList = new ArrayList<>();
    ArrayList<Product> tempList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewMoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        from = getIntent().getStringExtra("from");
        catName = getIntent().getStringExtra("catName");

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

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
                    case "Season":
                        season = item;
                        break;
                }

                txtClear.setVisibility(View.VISIBLE);
                isSorted = !sortBy.equals("All") || !category.equals("All") || !season.equals("All");
                if (!isSorted) {
                    txtClear.setVisibility(View.GONE);

                }
            }

            @Override
            public <T> void onAddressClick(T address, boolean b) {

            }

        };

        initRecyclerView();

        switch (from) {
            case "recommend":
                query1 = database.getReference().child("Recommended Products").child(auth.getUid());
                binding.toolbar.setTitle("Recommended Products");
                break;
            case "recent":
                query1 = database.getReference().child("Products");
                binding.toolbar.setTitle("Recent Products");
                break;
            case "category":
                query1 = database.getReference().child("Products").orderByChild("category").equalTo(catName);
                binding.toolbar.setTitle(catName);
                break;
        }

        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                unmodifiedList.clear();
                cat_list.clear();
                tempList.clear();
                int i = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    if (from.equals("recommend")) {
                        products.add(product);
                        tempList.add(product);
                    } else {
                        if (i < 100) {
                            products.add(product);
                            tempList.add(product);
                        }
                    }

                    unmodifiedList.add(product);
                    if (from.equals("category")) {
                        cat_list.add(product.getArticleType());
                    } else {
                        cat_list.add(product.getCategory());
                    }
                    i++;
                }
                if (from.equals("recommend")) {
                    Collections.sort(products, Product.getLatestTime);
                }
                binding.progressCircular.setVisibility(View.GONE);
                adapters.notifyItemInserted(products.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
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
        list.add(new SortModel("Season", season));
        initSortFilterRecycler(recyclerView);
        bottomSheetDialog.show();


//        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                if (isSorted) {
//                    binding.imgFilter.setImageResource(R.drawable.ic_baseline_filter_alt_24);
//                    binding.imgFilter.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.skyBlue));
//                } else {
//                    binding.imgFilter.setImageResource(R.drawable.ic_baseline_filter_alt_off_24);
//                    binding.imgFilter.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.black));
//                }
//
//                if (!isSorted) {
//                    products.clear();
//                    products.addAll(unmodifiedList);
//                    adapters.notifyDataSetChanged();
//                    bottomSheetDialog.dismiss();
//                }
//            }
//        });
        assert btnApply != null;
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSorted) {
                    binding.imgFilter.setImageResource(R.drawable.ic_baseline_filter_alt_24);
                    binding.imgFilter.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.skyBlue));
                } else {
                    binding.imgFilter.setImageResource(R.drawable.ic_baseline_filter_alt_off_24);
                    binding.imgFilter.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.black));
                    products.clear();
                    products.addAll(unmodifiedList);
                    adapters.notifyDataSetChanged();
                    bottomSheetDialog.dismiss();
                }



                if (category.equals("All") || sortBy.equals("All") || season.equals("All")) {
                    products.clear();
                    products.addAll(unmodifiedList);
                }

//                if (!season.equals("All")){
//                    filterSeason(season);
//                }

                if (!category.equals("All")) {
                    if (from.equals("category")) {
                        filterArticle(category);
                    } else {
                        filterCategory(category);
                    }

                }
                if (!sortBy.equals("All")) {
                    performSort(sortBy);
                }
                if (!season.equals("All")) {
                    filterSeason();
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

    private void filterSeason() {
        products.clear();
        Predicate<Product> byBrand = product -> product.getSeason().equals(season);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Set<Product> result = unmodifiedList.stream().filter(byBrand)
                    .collect(Collectors.toSet());
            products.addAll(result);
        }
        adapters.notifyDataSetChanged();
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
        season = "All";
        isSorted = false;
        list.add(new SortModel("Sort by", sortBy));
        list.add(new SortModel("Category", category));
        list.add(new SortModel("Season", season));
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

    private void filterArticle(String article) {
        products.clear();
        Predicate<Product> byFemale = product -> product.getArticleType().equals(article);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Set<Product> result = unmodifiedList.stream().filter(byFemale)
                    .collect(Collectors.toSet());
            products.addAll(result);
        }
        adapters.notifyDataSetChanged();
    }

    private void search(String toString) {
        searchList.clear();
        for (Product p : unmodifiedList) {
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
        sortFilterAdapter = new SortFilterAdapter(list, this, itemClickListener, cat_list);
        recyclerView.setAdapter(sortFilterAdapter);
    }


    private void initRecyclerView() {
        AutoFitGridLayoutManager layoutManager = new AutoFitGridLayoutManager(this, 500);
        //  GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        binding.viewMoreRecycler.setLayoutManager(layoutManager);
        adapters = new CardAdapters(products, this);
        binding.viewMoreRecycler.setAdapter(adapters);
    }
}