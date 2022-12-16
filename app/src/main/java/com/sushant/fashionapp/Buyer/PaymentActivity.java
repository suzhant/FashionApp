package com.sushant.fashionapp.Buyer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.khalti.checkout.helper.Config;
import com.khalti.checkout.helper.KhaltiCheckOut;
import com.khalti.checkout.helper.OnCheckOutListener;
import com.khalti.checkout.helper.PaymentPreference;
import com.sushant.fashionapp.Adapters.ShopAdapter;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityPaymentBinding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    ActivityPaymentBinding binding;
    ShopAdapter adapter;
    ArrayList<Store> stores = new ArrayList<>();
    ArrayList<Store> selectedStore = new ArrayList<>();
    ArrayList<String> storeList = new ArrayList<>();
    ArrayList<Store> selectedStoreList = new ArrayList<>();
    FirebaseAuth auth;
    FirebaseDatabase database;
    String storePic, storeName, address;
    boolean selectKhalti = false, selectCash = false, isSelected;
    long totalPrice, finalPrice;
    private final static String pub = "test_public_key_7ad13f903bd34864b8939125903e80ed";
    ItemClickListener itemClickListener;
    Store store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View view = LayoutInflater.from(this).inflate(R.layout.custom_khalti_button, binding.parent, false);
        binding.khaltiButton.setCustomView(view);

        storeList = getIntent().getStringArrayListExtra("storeInfo");
        totalPrice = getIntent().getLongExtra("totalPrice", 0);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.txtPrice.setText(MessageFormat.format("Total: {0}", totalPrice));

        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(String item, String type) {

            }

            @Override
            public <T> void onAddressClick(T Object, boolean b) {
                store = (Store) Object;
                if (b) {
                    selectedStore.add(store);
                } else {
                    selectedStore.remove(store);
                }

                int number_of_store = selectedStore.size();
                Log.d("storeNo", "onAddressClick: " + number_of_store);
                finalPrice = totalPrice - (number_of_store * 70L);
                binding.txtPrice.setText(MessageFormat.format("Total: {0}", finalPrice));
                if (number_of_store > 0) {
                    isSelected = true;
                }
            }
        };


        binding.statusLyt.radio1.setImageResource(R.drawable.ic_baseline_check_circle_24);
        binding.statusLyt.radio1.setImageTintList(ContextCompat.getColorStateList(this, R.color.black));
        binding.statusLyt.txt1.setTextColor(ContextCompat.getColor(this, R.color.black));
        binding.statusLyt.radio2.setImageTintList(ContextCompat.getColorStateList(this, R.color.teal_700));
        binding.statusLyt.txt2.setTextColor(ContextCompat.getColor(this, R.color.teal_700));

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        database.getReference().child("Store").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Store store = snapshot1.getValue(Store.class);
                    stores.add(store);
                }

                for (String storeId : storeList) {
                    for (Store s : stores) {
                        if (storeId.equals(s.getStoreId())) {
                            selectedStoreList.add(s);
                            break;
                        }
                    }
                }
                adapter.notifyItemInserted(selectedStoreList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.cardKhalti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectKhalti = !selectKhalti;
                if (selectKhalti) {
                    //       binding.cardKhalti.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.skyBlue));
                    binding.imgKhaltiCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.skyBlue));
                    selectCash = false;
                } else {
                    //     binding.cardKhalti.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.medium_gray));
                    binding.imgKhaltiCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), com.khalti.R.color.disabled));
                }
                if (!selectCash) {
                    //     binding.cardCashOnDelivery.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.medium_gray));
                    binding.imgCashCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), com.khalti.R.color.disabled));
                }
            }
        });

        binding.cardCashOnDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCash = !selectCash;
                if (selectCash) {
                    //   binding.cardCashOnDelivery.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.skyBlue));
                    binding.imgCashCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.skyBlue));
                    selectKhalti = false;
                } else {
                    //    binding.cardCashOnDelivery.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.medium_gray));
                    binding.imgCashCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), com.khalti.R.color.disabled));
                }
                if (!selectKhalti) {
                    //     binding.cardKhalti.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.medium_gray));
                    binding.imgKhaltiCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), com.khalti.R.color.disabled));
                }
            }
        });

        binding.khaltiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectKhalti && !selectCash) {
                    Snackbar.make(findViewById(R.id.parent), "Select Payment method", Snackbar.LENGTH_SHORT).setAnchorView(R.id.linear).show();
                    return;
                }

                if (selectKhalti) {
                    //  binding.khaltiButton.setOnClickListener(this);
                    integrateKhalti();
                }
            }
        });


        initRecycler();
    }

    private void integrateKhalti() {
        String key = database.getReference().child("Payment").push().getKey();
        if (!isSelected) {
            finalPrice = totalPrice;
        }

        assert key != null;
        Config.Builder builder = new Config.Builder(pub, key, key, finalPrice, new OnCheckOutListener() {
            @Override
            public void onError(@NonNull String action, @NonNull Map<String, String> errorMap) {
                Log.i(action, errorMap.toString());
            }

            @Override
            public void onSuccess(@NonNull Map<String, Object> data) {
                Log.i("success", data.toString());
                //  String key = database.getReference().child("Payment").push().getKey();
            }
        }).paymentPreferences(new ArrayList<PaymentPreference>() {{
            add(PaymentPreference.KHALTI);
        }});
        Config config = builder.build();
        binding.khaltiButton.setCheckOutConfig(config);
        KhaltiCheckOut khaltiCheckOut = new KhaltiCheckOut(this, config);
        khaltiCheckOut.show();
    }

    private void initRecycler() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerShops.setLayoutManager(linearLayoutManager);
        adapter = new ShopAdapter(selectedStoreList, this, itemClickListener);
        binding.recyclerShops.setAdapter(adapter);
    }
}