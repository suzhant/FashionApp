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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private static final String LIST_STATE_KEY = "abc";
    FragmentHomeBinding binding;
    FirebaseAuth auth;
    CardAdapters popularAdapters;
    ArrayList<Product> products = new ArrayList<>();
    FirebaseDatabase database;
    public GridLayoutManager layoutManager;

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
                binding.txtUserName.setText(Html.fromHtml("Welcome <br><font color=\"#09AEA3\">" + captializeAllFirstLetter(name) + "</font"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
        products.add(new Product("1", "shirt", R.drawable.demo_rect, 100, "French Store", 10));
        products.add(new Product("2", "paint", R.drawable.demo_rect, 200, "Nepali Store", 100));
        products.add(new Product("3", "bag", R.drawable.demo_rect, 300, "Korean Store", 101));
        products.add(new Product("4", "shoes", R.drawable.demo_rect, 400, "Chinese store", 102));
        products.add(new Product("5", "hat", R.drawable.demo_rect, 500, "Japanese Store", 103));
        products.add(new Product("6", "glasses", R.drawable.demo_rect, 600, "English Store", 1));
        products.add(new Product("7", "sushant", R.drawable.demo_rect, 700, "store7", 11));
        products.add(new Product("8", "sushant", R.drawable.demo_rect, 800, "store8", 12));

        layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        binding.popularRecycler.setLayoutManager(layoutManager);
        popularAdapters = new CardAdapters(products, getActivity());
        popularAdapters.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.popularRecycler.setAdapter(popularAdapters);
    }

    private String captializeAllFirstLetter(String name) {
        char[] array = name.toCharArray();
        array[0] = Character.toUpperCase(array[0]);

        for (int i = 1; i < array.length; i++) {
            if (Character.isWhitespace(array[i - 1])) {
                array[i] = Character.toUpperCase(array[i]);
            }
        }
        return new String(array);
    }

    @Override
    public void onResume() {
        binding.popularRecycler.scrollToPosition(3);
        super.onResume();
    }
}