package com.sushant.fashionapp.Buyer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.denzcoskun.imageslider.models.SlideModel;
import com.devs.readmoreoption.ReadMoreOption;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.ActivityHomePage;
import com.sushant.fashionapp.Adapters.VariantAdapter;
import com.sushant.fashionapp.Inteface.VariantClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.TextUtils;
import com.sushant.fashionapp.databinding.ActivityProductDetailsBinding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityProductDetails extends AppCompatActivity {

    ActivityProductDetailsBinding binding;
    int price, stock, pPic, quantity;
    String maxLimit;
    boolean isTextViewClicked = false;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String pName, sName, pId;
    String sizeId = "S";
    ArrayList<Product> products = new ArrayList<>();
    VariantAdapter variantAdapter;
    List<SlideModel> list = new ArrayList<>();
    VariantClickListener variantClickListener;
    boolean maxLimitExist, quantityExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

//        price = getIntent().getIntExtra("pPrice", 0);
//        stock = getIntent().getIntExtra("stock", 0);
        pPic = getIntent().getIntExtra("pPic", 0);
//        pName = getIntent().getStringExtra("pName");
//        sName = getIntent().getStringExtra("sName");
        pId = getIntent().getStringExtra("pId");


        list.add(new SlideModel(pPic, null));
        list.add(new SlideModel(R.drawable.red_skirt, null));
        list.add(new SlideModel(R.drawable.red_dress2, null));
        binding.imgSlider.setImageList(list);


//        binding.txtDescription.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!isTextViewClicked) {
//                    binding.txtDescription.setMaxLines(Integer.MAX_VALUE);
//                    isTextViewClicked = true;
//                } else {
//                    binding.txtDescription.setMaxLines(2);
//                    isTextViewClicked = false;
//                }
//            }
//        });

        initVariantRecycler();

        //expandable description
        ReadMoreOption readMoreOption = new ReadMoreOption.Builder(this)
                .textLength(2, ReadMoreOption.TYPE_LINE) // OR
                //.textLength(300, ReadMoreOption.TYPE_CHARACTER)
                .moreLabel("read more")
                .lessLabel("read less")
                .moreLabelColor(getColor(R.color.skyBlue))
                .lessLabelColor(Color.RED)
                .labelUnderLine(true)
                .expandAnimation(true)
                .build();
        readMoreOption.addReadMoreTo(binding.txtDescription, binding.txtDescription.getText().toString());

        binding.chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) binding.chipGroup.getChildAt(i);
                    if (chip.isChecked()) {
                        //this chip is selected.....
                        sizeId = chip.getText().toString();
                    }
                }
            }
        });

        database.getReference().child("Products").child(pId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("maxLimit").exists()) {
                    maxLimit = snapshot.child("maxLimit").getValue(String.class);
                    maxLimitExist = true;
                } else {
                    maxLimitExist = false;
                }
                price = snapshot.child("pPrice").getValue(Integer.class);
                //    stock =  snapshot.child("stock").getValue(Integer.class);
                pName = snapshot.child("pName").getValue(String.class);
                sName = snapshot.child("storeName").getValue(String.class);
                binding.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}<big>", price)));
                binding.txtPrdtName.setText(TextUtils.captializeAllFirstLetter(pName));
                binding.txtStoreName.setText(TextUtils.captializeAllFirstLetter(sName));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Cart").child(auth.getUid()).child("Product Details").child(pId + sizeId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("quantity").exists()) {
                    quantity = snapshot.child("quantity").getValue(Integer.class);
                    quantityExist = true;
                } else {
                    quantityExist = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stock > 0) {
                    if (quantityExist && maxLimitExist) {
                        int limit = Integer.parseInt(maxLimit);
                        if (quantity < limit) {
                            addProductToCart();
                        } else {
                            Snackbar.make(findViewById(R.id.parent), "Maximum Limit is reached!", Snackbar.LENGTH_SHORT).setAnchorView(binding.cardView).show();
                        }
                    } else {
                        addProductToCart();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.parent), "Out of stock!", Snackbar.LENGTH_SHORT).setAnchorView(binding.cardView).show();
                }
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityProductDetails.this, ActivityHomePage.class));
                finishAfterTransition();
                //  onBackPressed();
            }
        });
    }

    private void initVariantRecycler() {
        database.getReference().child("Products").child(pId).child("variants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    products.add(product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.variantRecycler.setLayoutManager(layoutManager);
        variantAdapter = new VariantAdapter(products, this);
        binding.variantRecycler.setAdapter(variantAdapter);
    }

    private void addProductToCart() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), "Added to cart",
                Snackbar.LENGTH_SHORT).setAction("Go to Cart", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityProductDetails.this, CartActivity.class));
            }
        }).setAnchorView(binding.cardView);
        TextView snackbarActionTextView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
        snackbarActionTextView.setAllCaps(false);
        database.getReference().child("Cart").child(auth.getUid()).child("Product Details").child(pId + sizeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    updateCartQuantity(quantity);
                    updateStock(stock);
                    snackbar.show();
                } else {
                    Product product = new Product(pId, pName, pPic, price, sName, stock);
                    product.setSize(sizeId);
                    product.setMaxLimit(maxLimit);
                    updateStock(stock);
                    database.getReference().child("Cart").child(auth.getUid()).child("Product Details").child(pId + sizeId).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            snackbar.show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void updateStock(int s) {
        s = s - 1;
        HashMap<String, Object> stock = new HashMap<>();
        stock.put("stock", s);
        FirebaseDatabase.getInstance().getReference().child("Products").child(pId).updateChildren(stock);
    }

    private void updateCartQuantity(int q) {
        q = q + 1;
        HashMap<String, Object> quantity = new HashMap<>();
        quantity.put("quantity", q);
        FirebaseDatabase.getInstance().getReference().child("Cart").child(FirebaseAuth.getInstance().getUid()).child("Product Details").child(pId + sizeId)
                .updateChildren(quantity);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}