package com.sushant.fashionapp.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Adapters.CategoryAdapter;
import com.sushant.fashionapp.Models.Category;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.TextUtils;
import com.sushant.fashionapp.databinding.FragmentHomeBinding;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private static final String LIST_STATE_KEY = "abc";
    FragmentHomeBinding binding;
    FirebaseAuth auth;
    CardAdapters popularAdapters;
    ArrayList<Product> products = new ArrayList<>();
    FirebaseDatabase database;
    public GridLayoutManager layoutManager;
    ArrayList<Category> categories = new ArrayList<>();
    CategoryAdapter categoryAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }


    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("userName").getValue(String.class);
                assert name != null;
                binding.txtUserName.setText(Html.fromHtml("Welcome <br><font color=\"#09AEA3\">" + TextUtils.captializeAllFirstLetter(name) + "</font"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Glide.with(this).load(R.drawable.profile).placeholder(R.drawable.avatar).into(binding.circleImageView);
        initReyclerView();
        initCategoryRecycler();

        //banner slider
        binding.imgBanner.registerLifecycle(getLifecycle());
        List<CarouselItem> list = new ArrayList<>();
        // Just image URL
        list.add(new CarouselItem(R.drawable.summer_banner));
        list.add(new CarouselItem(R.drawable.banner_2));
        list.add(new CarouselItem(R.drawable.banner_3));
        binding.imgBanner.setData(list);

        binding.chip1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initReyclerView();
            }
        });

        binding.appBarLayout2.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                binding.topbar.setAlpha(1.0f - Math.abs(verticalOffset / (float)
                        appBarLayout.getTotalScrollRange()));
            }
        });
        return binding.getRoot();
    }

    private void initReyclerView() {
        products.clear();
        products.add(new Product("1", "Tailor stitch Cotton Oversize Korean T-shirt and Shirts", R.drawable.demo_rect, 100, "French Store", 10));
        products.add(new Product("2", "paint", R.drawable.demo_rect, 200, "Nepali Store", 100));
        products.add(new Product("3", "bag", R.drawable.demo_rect, 300, "Korean Store", 101));
        products.add(new Product("4", "shoes", R.drawable.demo_rect, 400, "Chinese store", 102));
        products.add(new Product("5", "hat", R.drawable.demo_rect, 500, "Japanese Store", 103));
        products.add(new Product("6", "glasses", R.drawable.demo_rect, 600, "English Store", 1));
        products.add(new Product("7", "sushant", R.drawable.demo_rect, 700, "store7", 11));
        products.add(new Product("8", "sushant", R.drawable.demo_rect, 800, "store8", 12));

        //    layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.popularRecycler.setLayoutManager(layoutManager);
        popularAdapters = new CardAdapters(products, getActivity());
        popularAdapters.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.popularRecycler.setAdapter(popularAdapters);
    }

    private void initCategoryRecycler() {
        categories.add(new Category(R.drawable.bussiness_man, "Men"));
        categories.add(new Category(R.drawable.businesswoman, "Women"));
        categories.add(new Category(R.drawable.children, "Kid"));
        categories.add(new Category(R.drawable.baby, "Toddler"));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.categoryRecycler.setLayoutManager(layoutManager);
        categoryAdapter = new CategoryAdapter(categories, getActivity());
        binding.categoryRecycler.setAdapter(categoryAdapter);
    }


    @Override
    public void onResume() {
        binding.popularRecycler.scrollToPosition(3);
        super.onResume();
    }
}