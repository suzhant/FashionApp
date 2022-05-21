package com.sushant.fashionapp.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.FragmentHomeBinding;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    FirebaseAuth auth;
    CardAdapters popularAdapters;
    ArrayList<Product> products = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }


    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();


        Glide.with(this).load(R.drawable.profile).placeholder(R.drawable.avatar).into(binding.circleImageView);
        initReyclerView();

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
        products.add(new Product("1", "sushant", R.drawable.demo_rect, 100));
        products.add(new Product("2", "sushant", R.drawable.demo_rect, 200));
        products.add(new Product("3", "sushant", R.drawable.demo_rect, 300));
        products.add(new Product("4", "sushant", R.drawable.demo_rect, 400));
        products.add(new Product("5", "sushant", R.drawable.demo_rect, 500));
        products.add(new Product("6", "sushant", R.drawable.demo_rect, 600));
        products.add(new Product("7", "sushant", R.drawable.demo_rect, 700));
        products.add(new Product("8", "sushant", R.drawable.demo_rect, 800));

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        binding.popularRecycler.setLayoutManager(layoutManager);
        popularAdapters = new CardAdapters(products, getActivity());
        popularAdapters.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.popularRecycler.setAdapter(popularAdapters);
    }
}