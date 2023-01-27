package com.sushant.fashionapp.Buyer;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
    SortFilterAdapter sortFilterAdapter;
    ArrayList<SortModel> list = new ArrayList<>();
    ItemClickListener itemClickListener;
    String sortBy = "All", category = "All", season = "All";
    public boolean isSorted = false;
    TextView txtClear;
    String from;
    Query query1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewMoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        from = getIntent().getStringExtra("from");

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

        if (from.equals("recommend")) {
            query1 = database.getReference().child("Recommended Products").child(auth.getUid());
            binding.toolbar.setTitle("Recommended Products");
        } else if (from.equals("recent")) {
            query1 = database.getReference().child("Products");
            binding.toolbar.setTitle("Recent Products");
        }

        query1.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                unmodifiedList.clear();
                int i = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    if (i < 100) {
                        products.add(product);
                    }
                    unmodifiedList.add(product);
                    i++;
                }
                if (from.equals("recommend")) {
                    Collections.sort(products, Product.getLatestTime);
                }
                adapters.notifyItemInserted(products.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.btnSortFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
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
        //      list.add(new SortModel("Season", season));
//        list.add(new SortModel("Size", "All"));
//        list.add(new SortModel("In stock", "on stock"));
        initSortFilterRecycler(recyclerView);
        bottomSheetDialog.show();


        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (isSorted) {
                    binding.btnSortFilter.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.skyBlue));
                    binding.btnSortFilter.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    binding.btnSortFilter.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                } else {
                    binding.btnSortFilter.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray_100));
                    binding.btnSortFilter.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    binding.btnSortFilter.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.black)));
                }
                if (!isSorted) {
                    products.clear();
                    products.addAll(unmodifiedList);
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
                    binding.btnSortFilter.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.skyBlue));
                    binding.btnSortFilter.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    binding.btnSortFilter.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                } else {
                    binding.btnSortFilter.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray_100));
                    binding.btnSortFilter.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    binding.btnSortFilter.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.black)));
                }


                if (category.equals("All") || sortBy.equals("All") || season.equals("All")) {
                    products.clear();
                    products.addAll(unmodifiedList);
                }

//                if (!season.equals("All")){
//                    filterSeason(season);
//                }

                if (!category.equals("All")) {
                    filterCategory(category);
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
        sortFilterAdapter = new SortFilterAdapter(list, this, itemClickListener);
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