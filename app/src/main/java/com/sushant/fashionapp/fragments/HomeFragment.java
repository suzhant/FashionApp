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
        initPopularRecyclerView();
        initCategoryRecycler();
        initRecentRecycler();

        //banner slider
        binding.imgBanner.registerLifecycle(getLifecycle());
        List<CarouselItem> list = new ArrayList<>();
        // Just image URL
        list.add(new CarouselItem(R.drawable.summer_banner));
        list.add(new CarouselItem(R.drawable.banner_2));
        list.add(new CarouselItem(R.drawable.banner_3));
        binding.imgBanner.setData(list);


//        binding.appBarLayout2.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                binding.topbar.setAlpha(1.0f - Math.abs(verticalOffset / (float)
//                        appBarLayout.getTotalScrollRange()));
//            }
//        });
        return binding.getRoot();
    }

    private void initPopularRecyclerView() {
        products.clear();
        //    UUID pid = UUID.randomUUID();
        //   String pid1 = String.valueOf(pid);
        String key = database.getReference().child("Products").push().getKey();

        Product product1 = new Product(key, "Denim black jean style", "French Store", R.drawable.denim_black_jean_style);
        product1.setMaxLimit(String.valueOf(2));
        product1.setLove(0);
        product1.setpPrice(100);
        ArrayList<Product> variant1 = new ArrayList<>();
        Product black = new Product(key + "B", "Black", R.drawable.denim_black_jean_style);
        ArrayList<Product> size1 = new ArrayList<>();
        size1.add(new Product(key + "BS", "S", 100, 10));
        size1.add(new Product(key + "BM", "M", 120, 10));
        size1.add(new Product(key + "BL", "L", 150, 10));
        black.setSizes(size1);
        variant1.add(black);

        Product red = new Product(key + "R", "Red", R.drawable.boy_sweeter);
        ArrayList<Product> size2 = new ArrayList<>();
        size2.add(new Product(key + "RS", "S", 100, 10));
        size2.add(new Product(key + "RL", "L", 150, 10));
        red.setSizes(size2);
        variant1.add(red);
        product1.setVariants(variant1);
        products.add(product1);

        Product product2 = new Product("2", "boots", R.drawable.boots, 200, "Nepali Store", 10);
        product2.setMaxLimit(String.valueOf(5));
        products.add(product2);
        products.add(new Product("3", "boxer", R.drawable.boxer, 300, "Korean Store", 11));
        products.add(new Product("4", "sweeter", R.drawable.boy_sweeter, 400, "Chinese store", 102));
        products.add(new Product("5", "leather gloves", R.drawable.leather_gloves, 500, "Japanese Store", 103));
        products.add(new Product("6", "stripe turtle neck top", R.drawable.stripe_turtle_neck_shirt, 600, "English Store", 1));
        database.getReference().child("Products").setValue(null);
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            database.getReference().child("Products").child(product.getpId()).setValue(product);
        }

        //    layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.popularRecycler.setLayoutManager(layoutManager);
        popularAdapters = new CardAdapters(products, getActivity());
        popularAdapters.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.popularRecycler.setAdapter(popularAdapters);

    }

    private void initCategoryRecycler() {
        categories.add(new Category(R.drawable.indian_male, "Men"));
        categories.add(new Category(R.drawable.model_with_coffee, "Women"));
        categories.add(new Category(R.drawable.boy_kid, "Kid"));
        categories.add(new Category(R.drawable.little_boy, "Toddler"));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.categoryRecycler.setLayoutManager(layoutManager);
        categoryAdapter = new CategoryAdapter(categories, getActivity());
        categoryAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.categoryRecycler.setAdapter(categoryAdapter);
    }

    private void initRecentRecycler() {
        //    layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recentRecycler.setLayoutManager(layoutManager);
        popularAdapters = new CardAdapters(products, getActivity());
        popularAdapters.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.recentRecycler.setAdapter(popularAdapters);
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}