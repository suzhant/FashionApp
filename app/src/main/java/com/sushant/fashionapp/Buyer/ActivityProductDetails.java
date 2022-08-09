package com.sushant.fashionapp.Buyer;

import static com.sushant.fashionapp.Utils.TextUtils.captializeAllFirstLetter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.ActivityHomePage;
import com.sushant.fashionapp.Adapters.VariantAdapter;
import com.sushant.fashionapp.Inteface.VariantClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.TextUtils;
import com.sushant.fashionapp.databinding.ActivityProductDetailsBinding;

import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ActivityProductDetails extends AppCompatActivity {

    ActivityProductDetailsBinding binding;
    int price, stock, quantity, variantPos, sizeIndex;
    String pic;
    String maxLimit;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String pName, sName, pId, color, pDesc;
    String sizeId, actualProductId, variantId;
    ArrayList<Product> products = new ArrayList<>();
    ArrayList<String> images = new ArrayList<>();
    VariantAdapter variantAdapter;
    List<SlideModel> list = new ArrayList<>();
    VariantClickListener variantClickListener;
    ValueEventListener variantListener, wishListListener;
    DatabaseReference variantRef, wishListRef;
    ArrayList<Product> sizes = new ArrayList<>();
    List<CarouselItem> listCarousel = new ArrayList<>();

    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d("oncreate", "created");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        price = getIntent().getIntExtra("pPrice", 0);
        pic = getIntent().getStringExtra("pPic");
        sName = getIntent().getStringExtra("sName");
        pId = getIntent().getStringExtra("pId");
        pDesc = getIntent().getStringExtra("pDesc");
        pName = getIntent().getStringExtra("pName");
        index = getIntent().getIntExtra("index", 0);


        binding.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}<big>", price)));
        binding.txtPrdtName.setText(captializeAllFirstLetter(pName));
        binding.txtStoreName.setText(captializeAllFirstLetter(sName));


        variantClickListener = new VariantClickListener() {
            @Override
            public void onClick(Product product, int pos) {
                Log.d("variants", "called");
                pic = product.getPhotos().get(0);
                color = product.getColor();
                variantPos = pos;
                sizes.clear();
                binding.chipGroup.clearCheck();
                sizeId = null;
                sizes.addAll(product.getSizes());

                for (int j = 0; j < binding.chipGroup.getChildCount(); j++) {
                    Chip chip = (Chip) binding.chipGroup.getChildAt(j);
                    chip.setVisibility(View.GONE);
                }
                for (int i = 0; i < sizes.size(); i++) {
                    for (int j = 0; j < binding.chipGroup.getChildCount(); j++) {
                        Chip chip = (Chip) binding.chipGroup.getChildAt(j);
                        if (chip.getText().toString().equals(sizes.get(i).getSize())) {
                            chip.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }

                //  adding pic in slider
                list.clear();
                for (int i = 0; i < product.getPhotos().size(); i++) {
                    list.add(new SlideModel(product.getPhotos().get(i), null));
                    binding.imgSlider.setImageList(list);
                }

            }
        };


        initVariantRecycler();

        variantRef = database.getReference().child("Products").child(pId).child("variants");
        variantListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("variantListener", "called");
                products.clear();
                //  list.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    products.add(product);
                }
//                for (Product product:products) {
//                    if (product.getPhotos()!=null){
//                        for (String image:product.getPhotos()){
//                            list.add(new SlideModel(image, null));
//                        }
//                    }
//                }

                variantAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        variantRef.addValueEventListener(variantListener);

        wishListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

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
        readMoreOption.addReadMoreTo(binding.txtDescription, pDesc);

        binding.chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) binding.chipGroup.getChildAt(i);
                    if (chip.isChecked()) {
                        //this chip is selected.....
                        sizeId = chip.getText().toString();
                        actualProductId = pId + TextUtils.getFirstLetter(color) + sizeId;
                        for (int j = 0; j < sizes.size(); j++) {
                            Product p = sizes.get(j);
                            if (sizeId.equals(p.getSize())) {
                                stock = p.getStock();
                                sizeIndex = j;
                                break;
                            }
                        }
                    }
                }
            }
        });

        binding.btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sizeId == null) {
                    Snackbar.make(binding.parent, "Please select size", Snackbar.LENGTH_SHORT).setAnchorView(binding.btnAddCart).show();
                    return;
                }
                if (stock > 0) {
                    addProductToCart();
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
        Log.d("variantAdapter", "created");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.variantRecycler.setLayoutManager(layoutManager);
        variantAdapter = new VariantAdapter(products, this, variantClickListener, index);
        binding.variantRecycler.setAdapter(variantAdapter);
    }

    private void addProductToCart() {
        Snackbar snackbar = getSnackbar();
        database.getReference().child("Cart").child(auth.getUid()).child("Product Details").child(actualProductId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    quantity = snapshot.child("quantity").getValue(Integer.class);
                    if (quantity < 5) {
                        updateCartQuantity(quantity);
                        updateStock(stock);
                        snackbar.show();
                    } else {
                        Snackbar.make(findViewById(R.id.parent), "Maximum Limit is reached!", Snackbar.LENGTH_SHORT).setAnchorView(binding.cardView).show();
                    }
                } else {
                    Product product = new Product(pId, pName, pic, price, sName, stock);
                    product.setVariantPId(actualProductId);
                    product.setVariantIndex(variantPos);
                    product.setSizeIndex(sizeIndex);
                    product.setDesc(pDesc);
                    product.setSize(sizeId);
                    product.setMaxLimit(maxLimit);
                    product.setColor(color);
                    product.setQuantity(1);
                    updateStock(stock);
                    database.getReference().child("Cart").child(auth.getUid()).child("Product Details").child(actualProductId).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    @NonNull
    private Snackbar getSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), "Added to cart",
                Snackbar.LENGTH_SHORT).setAction("Go to Cart", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityProductDetails.this, CartActivity.class));
            }
        }).setAnchorView(binding.cardView);
        TextView snackbarActionTextView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
        snackbarActionTextView.setAllCaps(false);
        return snackbar;
    }

    private void updateStock(int s) {
        s = s - 1;
        HashMap<String, Object> stock = new HashMap<>();
        stock.put("stock", s);
        database.getReference().child("Products").child(pId).child("variants").child(String.valueOf(variantPos)).child("sizes")
                .child(String.valueOf(sizeIndex)).updateChildren(stock);
    }

    private void updateCartQuantity(int q) {
        q = q + 1;
        HashMap<String, Object> quantity = new HashMap<>();
        quantity.put("quantity", q);
        FirebaseDatabase.getInstance().getReference().child("Cart").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("Product Details").child(actualProductId)
                .updateChildren(quantity);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        variantRef.removeEventListener(variantListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ondestroy", "destroyed");
        variantRef.removeEventListener(variantListener);
    }
}