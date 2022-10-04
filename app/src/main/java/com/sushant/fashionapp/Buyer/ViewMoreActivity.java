package com.sushant.fashionapp.Buyer;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Adapters.SortFilterAdapter;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.SortModel;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityViewMoreBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ViewMoreActivity extends AppCompatActivity {

    ActivityViewMoreBinding binding;
    CardAdapters adapters;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Product> products = new ArrayList<>();
    ArrayList<Product> unmodifiedList = new ArrayList<>();
    SortFilterAdapter sortFilterAdapter;
    ArrayList<SortModel> list = new ArrayList<>();
    ItemClickListener itemClickListener;
    String sortBy = "All", category = "All", colour = "All", size = "All", inStock = "on stock";
    public boolean isSorted = false;
    TextView txtClear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewMoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

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
                }

                txtClear.setVisibility(View.VISIBLE);
                isSorted = true;
            }
        };

        initRecyclerView();

        database.getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                unmodifiedList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    products.add(product);
                    unmodifiedList.add(product);
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
        list.add(new SortModel("Colour", colour));
        list.add(new SortModel("Size", "All"));
        list.add(new SortModel("In stock", "on stock"));
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


                if (!category.equals("All")) {
                    filterGender(category);
                }
                if (!colour.equals("All")) {
                    filterColor(colour);
                }
                if (!sortBy.equals("All")) {
                    performSort(sortBy);
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

    private void resetAll(RecyclerView recyclerView) {
        list.clear();
        sortBy = "All";
        category = "All";
        isSorted = false;
        list.add(new SortModel("Sort by", sortBy));
        list.add(new SortModel("Category", category));
        list.add(new SortModel("Colour", colour));
        list.add(new SortModel("Size", "All"));
        list.add(new SortModel("In stock", "on stock"));
        initSortFilterRecycler(recyclerView);
    }

    private void filterGender(String gender) {
        category = gender;
        products.clear();
        Predicate<Product> byFemale = product -> product.getCategory().equals(gender);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<Product> result = unmodifiedList.stream().filter(byFemale)
                    .collect(Collectors.toList());
            products.addAll(result);
        }
        adapters.notifyDataSetChanged();
    }


    private void filterColor(String color) {
        colour = color;
        products.clear();
        Predicate<Product> byColor = product -> product.getVariants().get(0).getColor().equals(color);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<Product> result = unmodifiedList.stream().filter(byColor)
                    .collect(Collectors.toList());
            products.addAll(result);
        }
        adapters.notifyDataSetChanged();
    }

    private void performSort(String sort) {
        sortBy = sort;
        switch (sortBy) {
            case "Ascending":
                Collections.sort(products, Product.ascending);
                break;
            case "Descending":
                Collections.sort(products, Product.descending);
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
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        binding.viewMoreRecycler.setLayoutManager(layoutManager);
        adapters = new CardAdapters(products, this);
        binding.viewMoreRecycler.setAdapter(adapters);
    }
}