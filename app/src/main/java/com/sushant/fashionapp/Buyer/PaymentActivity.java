package com.sushant.fashionapp.Buyer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Order;
import com.sushant.fashionapp.Models.Status;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityPaymentBinding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    ActivityPaymentBinding binding;
    ShopAdapter adapter;
    ArrayList<Store> stores = new ArrayList<>();
    ArrayList<String> storeIdList = new ArrayList<>();
    ArrayList<Store> selectedStoreList = new ArrayList<>();
    ArrayList<Store> finalStoreList = new ArrayList<>();
    ArrayList<Cart> products = new ArrayList<>();
    FirebaseAuth auth;
    FirebaseDatabase database;
    boolean selectKhalti = false, selectCash = false, isSelected;
    long totalPrice, finalPrice;
    private final static String pub = "test_public_key_7ad13f903bd34864b8939125903e80ed";
    ItemClickListener itemClickListener;
    Store store;
    Address address;
    ProgressDialog dialog;
    int deliverCharge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View view = LayoutInflater.from(this).inflate(R.layout.custom_khalti_button, binding.parent, false);
        binding.khaltiButton.setCustomView(view);

        storeIdList = getIntent().getStringArrayListExtra("storeInfo");
        totalPrice = getIntent().getLongExtra("totalPrice", 0);
        address = (Address) getIntent().getSerializableExtra("addressInfo");


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we place your order.");
        dialog.setCancelable(false);


        binding.txtPrice.setText(MessageFormat.format("Total: {0}", totalPrice));
        deliverCharge = selectedStoreList.size() * 70;

        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(String item, String type) {

            }

            @Override
            public <T> void onAddressClick(T Object, boolean b) {
                store = (Store) Object;
                int pos = selectedStoreList.indexOf(store);
                if (b) {
                    deliverCharge = deliverCharge - 70;
                    finalStoreList.get(pos).setDeliveryCharge(0);
                    finalStoreList.get(pos).setSelfPickUp(true);
                } else {
                    deliverCharge = deliverCharge + 70;
                    finalStoreList.get(pos).setDeliveryCharge(70);
                    finalStoreList.get(pos).setSelfPickUp(false);
                }


                finalPrice = totalPrice - deliverCharge;
                binding.txtPrice.setText(MessageFormat.format("Total: {0}", finalPrice));
                if (deliverCharge > 0) {
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

                for (String storeId : storeIdList) {
                    for (Store s : stores) {
                        if (storeId.equals(s.getStoreId())) {
                            s.setDeliveryCharge(70);
                            selectedStoreList.add(s);
                            break;
                        }
                    }
                }

                finalStoreList.clear();
                for (Store store : selectedStoreList) {
                    Store store1 = new Store();
                    store1.setStoreId(store.getStoreId());
                    store1.setDeliveryCharge(70);
                    store1.setSelfPickUp(false);
                    finalStoreList.add(store1);
                }
                adapter.notifyItemInserted(selectedStoreList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Cart").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Cart p = snapshot1.getValue(Cart.class);
                    
                    Cart product = new Cart();
                    product.setpId(p.getpId());
                    product.setpName(p.getpName());
                    product.setDeliveryStatus(Status.PENDING.name());
                    if (p.getBargainPrice() != null) {
                        product.setBargainPrice(p.getBargainPrice());
                    }
                    product.setpPic(p.getpPic());
                    product.setpPrice(p.getpPrice());
                    product.setSize(p.getSize());
                    product.setColor(p.getColor());
                    product.setStoreId(p.getStoreId());
                    product.setQuantity(p.getQuantity());
                    products.add(product);
                }
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
                if (selectCash) {
                    integrateCashOnDelivery();
                }
            }
        });


        initRecycler();
    }

    private void integrateCashOnDelivery() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we're preparing your orders.");
        dialog.setCancelable(false);
        dialog.show();
        if (!isSelected) {
            finalPrice = totalPrice;
        }
        String orderId = database.getReference().child("Order").push().getKey();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setBuyerId(auth.getUid());
        order.setAmount(finalPrice);
        order.setProducts(products);
        order.setTimestamp(new Date().getTime());
        order.setAddressId(address.getAddressId());
        order.setStores(finalStoreList);
        order.setPaid(false);
        order.setOrderStatus(Status.PENDING.name());
        order.setPaymentMethod("cash_on_delivery");
        database.getReference().child("Orders").child(auth.getUid()).child(orderId).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Cart").child(auth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), OrderFinalPageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void integrateKhalti() {
        String orderId = database.getReference().child("Order").push().getKey();

        if (!isSelected) {
            finalPrice = totalPrice;
        }

        assert orderId != null;
        Config.Builder builder = new Config.Builder(pub, orderId, "product", finalPrice, new OnCheckOutListener() {
            @Override
            public void onError(@NonNull String action, @NonNull Map<String, String> errorMap) {
                Log.i(action, errorMap.toString());
                Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(@NonNull Map<String, Object> data) {
                Log.i("success", data.toString());
                Order order = new Order();
                order.setOrderId(orderId);
                order.setBuyerId(auth.getUid());
                order.setAmount(finalPrice);
                order.setToken(data.get("token").toString());
                order.setProducts(products);
                order.setTimestamp(new Date().getTime());
                order.setAddressId(address.getAddressId());
                order.setStores(finalStoreList);
                order.setPaid(true);
                order.setOrderStatus(Status.PENDING.name());
                order.setPaymentMethod("Khalti");
                database.getReference().child("Orders").child(auth.getUid()).child(orderId).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Cart").child(auth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Intent intent = new Intent(getApplicationContext(), OrderFinalPageActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });


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