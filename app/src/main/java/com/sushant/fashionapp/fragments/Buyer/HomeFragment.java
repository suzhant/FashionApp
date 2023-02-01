package com.sushant.fashionapp.fragments.Buyer;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Adapters.CategoryAdapter;
import com.sushant.fashionapp.Buyer.SearchActivity;
import com.sushant.fashionapp.Buyer.ViewMoreActivity;
import com.sushant.fashionapp.Models.Category;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.TextUtils;
import com.sushant.fashionapp.databinding.FragmentHomeBinding;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    FirebaseAuth auth;
    CardAdapters popularAdapters;
    FirebaseDatabase database;
    public GridLayoutManager layoutManager;
    ArrayList<Category> categories = new ArrayList<>();
    CategoryAdapter categoryAdapter;
    String buyerPic;
    FirebaseStorage storage;
    ArrayList<String> urls = new ArrayList<>();
    ArrayList<Product> smallList = new ArrayList<>();
    ArrayList<Product> recommend_list = new ArrayList<>();
    CardAdapters recentAdapter;

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
        storage = FirebaseStorage.getInstance();


        database.getReference().child("Action History").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    urls.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        String url = snapshot1.child("imgUrl").getValue(String.class);
                        urls.add(url);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Query query = database.getReference().child("Products").limitToFirst(10);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                smallList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    smallList.add(product);
                }
                recentAdapter.notifyItemInserted(smallList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Recommended Products").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recommend_list.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Product product = snapshot1.getValue(Product.class);
                        recommend_list.add(product);
                    }
                    Collections.sort(recommend_list, Product.getLatestTime);
                } else {
                    recommend_list.addAll(smallList);
                }
                binding.progressCircular2.setVisibility(View.GONE);
                popularAdapters.notifyItemInserted(recommend_list.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userName").exists()) {
                    String name = snapshot.child("userName").getValue(String.class);
                    binding.txtUserName.setText(Html.fromHtml("Welcome <br><font color=\"#09AEA3\">" + TextUtils.captializeAllFirstLetter(name) + "</font"));
                } else {
                    String phone = snapshot.child("userPhone").getValue(String.class);
                    binding.txtUserName.setText(Html.fromHtml("Welcome <br><font color=\"#09AEA3\">" + phone + "</font"));
                }

                if (snapshot.child("userPic").exists()) {
                    buyerPic = snapshot.child("userPic").getValue(String.class);
                    if (getActivity() != null) {
                        Glide.with(getActivity()).load(buyerPic).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(binding.circleImageView);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("category").child("subCategory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Category category = snapshot1.getValue(Category.class);
                    categories.add(category);
                }
                binding.progressCircular1.setVisibility(View.GONE);
                categoryAdapter.notifyItemInserted(categories.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //banner slider
        binding.imgBanner.registerLifecycle(getLifecycle());
        List<CarouselItem> list = new ArrayList<>();
        // Just image URL
        list.add(new CarouselItem(R.drawable.summer_banner));
        list.add(new CarouselItem(R.drawable.banner_2));
        list.add(new CarouselItem(R.drawable.banner_3));
        binding.imgBanner.setData(list);




        initPopularRecyclerView();
        initCategoryRecycler();
        initRecentRecycler();

//        binding.appBarLayout2.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                binding.topbar.setAlpha(1.0f - Math.abs(verticalOffset / (float)
//                        appBarLayout.getTotalScrollRange()));
//            }
//        });
        binding.txtPopularViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewMoreActivity.class);
                intent.putExtra("from", "recommend");
                startActivity(intent);
            }
        });

        binding.imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SearchActivity.class));
            }
        });

        binding.txtRecentViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ViewMoreActivity.class);
                intent.putExtra("from", "recent");
                startActivity(intent);
            }
        });


        return binding.getRoot();
    }


    private void initPopularRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        binding.popularRecycler.setLayoutManager(layoutManager);
        popularAdapters = new CardAdapters(recommend_list, getActivity());
        popularAdapters.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.popularRecycler.setAdapter(popularAdapters);

    }

    private void initCategoryRecycler() {
//        categories.add(new Category(R.drawable.indian_male, "Men"));
//        categories.add(new Category(R.drawable.model_with_coffee, "Women"));
//        categories.add(new Category(R.drawable.boy_kid, "Kid"));
//        categories.add(new Category(R.drawable.little_boy, "Toddler"));

//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        binding.categoryRecycler.setLayoutManager(layoutManager);
        categoryAdapter = new CategoryAdapter(categories, getActivity());
        categoryAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.categoryRecycler.setAdapter(categoryAdapter);
    }

    private void initRecentRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recentRecycler.setLayoutManager(layoutManager);
        recentAdapter = new CardAdapters(smallList, getActivity());
        recentAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.recentRecycler.setAdapter(recentAdapter);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

}