package com.sushant.fashionapp.fragments.Buyer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sushant.fashionapp.Adapters.CardAdapters;
import com.sushant.fashionapp.Adapters.CategoryAdapter;
import com.sushant.fashionapp.Buyer.SearchActivity;
import com.sushant.fashionapp.Buyer.ViewMoreActivity;
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.Models.Category;
import com.sushant.fashionapp.Models.Order;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.PdfGenerator;
import com.sushant.fashionapp.Utils.TextUtils;
import com.sushant.fashionapp.databinding.FragmentHomeBinding;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


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
    String buyerPic;
    Address receiverAddress = new Address();
    Order customerOrder = new Order();
    FirebaseStorage storage;

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


        //banner slider
        binding.imgBanner.registerLifecycle(getLifecycle());
        List<CarouselItem> list = new ArrayList<>();
        // Just image URL
        list.add(new CarouselItem(R.drawable.summer_banner));
        list.add(new CarouselItem(R.drawable.banner_2));
        list.add(new CarouselItem(R.drawable.banner_3));
        binding.imgBanner.setData(list);

        Query query = database.getReference().child("Products").limitToLast(5);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    products.add(product);
                }
                Collections.reverse(products);
                popularAdapters.notifyItemInserted(products.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                startActivity(new Intent(getContext(), ViewMoreActivity.class));
            }
        });

        binding.imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SearchActivity.class));
            }
        });


        database.getReference().child("Shipping Address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Address address = snapshot1.getValue(Address.class);
                    if (address.getDefault() && address.getuId().equals(auth.getUid())) {
                        receiverAddress = address;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.imgNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customerOrder.setOrderDate(new Date().getTime());
                customerOrder.setOrderId("123DF24VC");


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        File file = PdfGenerator.createPdf(getContext(), receiverAddress, "sushantshrestha62@gmail.com", customerOrder);
                        uploadPdf(file);

                    } catch (FileNotFoundException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else {
                    Dexter.withContext(getContext())
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    try {
                                        PdfGenerator.createPdf(getContext(), receiverAddress, "sushantshrestha62@gmail.com", customerOrder);
                                    } catch (FileNotFoundException | MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(getContext(), "Please accept permissions", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                            }).check();
                }

            }
        });
        return binding.getRoot();
    }

    private void uploadPdf(File document) {
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("Please wait..");
        dialog.setCancelable(false);
        dialog.show();
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Invoice").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        UploadTask uploadTask = reference.putFile(Uri.fromFile(document));
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("invoiceNo", new Date().getTime());
                            map.put("invoiceUrl", filePath);
                            database.getReference().child("Invoice").child(auth.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void initPopularRecyclerView() {
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
        CardAdapters popularAdapters;
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

    private String fetchData() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://asos2.p.rapidapi.com/products/v2/list?store=US&offset=0&categoryId=4209&limit=48&country=US&sort=freshness&currency=USD&sizeSchema=US&lang=en-US")
                .get()
                .addHeader("X-RapidAPI-Key", "f612959de8msh3ef06566f13b1b1p1d2c9ejsnba5e3cef2d84")
                .addHeader("X-RapidAPI-Host", "asos2.p.rapidapi.com")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().toString();
    }
}