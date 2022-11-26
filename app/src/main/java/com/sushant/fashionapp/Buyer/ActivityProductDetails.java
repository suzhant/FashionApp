package com.sushant.fashionapp.Buyer;

import static com.sushant.fashionapp.Utils.TextUtils.captializeAllFirstLetter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.models.SlideModel;
import com.devs.readmoreoption.ReadMoreOption;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.sushant.fashionapp.Adapters.VariantAdapter;
import com.sushant.fashionapp.Inteface.VariantClickListener;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Rating;
import com.sushant.fashionapp.Models.Size;
import com.sushant.fashionapp.Models.Variants;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.TextUtils;
import com.sushant.fashionapp.databinding.ActivityProductDetailsBinding;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ActivityProductDetails extends AppCompatActivity {

    ActivityProductDetailsBinding binding;
    int price, stock, quantity, variantPos, sizeIndex;
    String pic;
    String maxLimit;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String pName, sName, pId, color, pDesc, storeId, storePic;
    String sizeId, actualProductId;
    ArrayList<Variants> products = new ArrayList<>();
    VariantAdapter variantAdapter;
    List<SlideModel> list = new ArrayList<>();
    VariantClickListener variantClickListener;
    ValueEventListener variantListener, wishListListener;
    DatabaseReference variantRef, wishListRef;
    ArrayList<Size> sizes = new ArrayList<>();
    ArrayList<Product> wishList = new ArrayList<>();
    boolean isLoved, isAccepted, isExist = false;
    String bargainId;
    DatabaseReference reference;
    ValueEventListener valueEventListener;
    Integer noOfTries;
    Boolean isBlocked;
    Long timestamp, cutoff;
    String sellerId;

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
        storeId = getIntent().getStringExtra("storeId");
        pId = getIntent().getStringExtra("pId");
        pDesc = getIntent().getStringExtra("pDesc");
        pName = getIntent().getStringExtra("pName");
        index = getIntent().getIntExtra("index", 0);


        binding.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}<big>", price)));
        binding.txtPrdtName.setText(captializeAllFirstLetter(pName));


        variantClickListener = new VariantClickListener() {
            @Override
            public void onClick(Variants product, int pos) {
                Log.d("variants", "called");
                pic = product.getPhotos().get(0);
                color = product.getColor();
                variantPos = pos;
                sizes.clear();
                binding.chipGroup.clearCheck();
                sizeId = null;
                sizes.addAll(product.getSizes());

                //hiding all the chips
                for (int j = 0; j < binding.chipGroup.getChildCount(); j++) {
                    Chip chip = (Chip) binding.chipGroup.getChildAt(j);
                    chip.setVisibility(View.GONE);
                }
                //showing chips associated with the sizes of colors
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

                //resetting wish icon to normal
                binding.imgWish.setImageResource(R.drawable.ic_love);
                binding.imgWish.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.black));
            }

            @Override
            public void onProductClick(Product product, int pos) {

            }

        };


        initVariantRecycler();

        //fetching variants
        variantRef = database.getReference().child("Products").child(pId).child("variants");
        variantListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("variantListener", "called");
                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Variants product = snapshot1.getValue(Variants.class);
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

        //fetching wishlist items
        wishListRef = database.getReference().child("WishList").child(auth.getUid());
        wishListListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    assert product != null;
                    wishList.add(product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        wishListRef.addValueEventListener(wishListListener);


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
                            Size p = sizes.get(j);
                            if (sizeId.equals(p.getSize())) {
                                stock = p.getStock();
                                sizeIndex = j;
                                break;
                            }
                        }
                        //finding if the selected item is in the wishlist
                        for (Product p : wishList) {
                            if (actualProductId.equals(p.getVariantPId())) {
                                binding.imgWish.setImageResource(R.drawable.ic_love_fill);
                                binding.imgWish.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.skyBlue));
                                isLoved = true;
                                break;
                            } else {
                                binding.imgWish.setImageResource(R.drawable.ic_love);
                                binding.imgWish.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.black));
                                isLoved = false;
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
//                startActivity(new Intent(ActivityProductDetails.this, ActivityHomePage.class));
//                finishAfterTransition();
                onBackPressed();
            }
        });

        binding.imgWish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sizeId == null) {
                    Snackbar.make(binding.parent, "Please select size", Snackbar.LENGTH_SHORT).setAnchorView(binding.btnAddCart).show();
                    return;
                }
                if (isLoved) {
                    deleteProductFromWishList();
                } else {
                    addToWishList();
                }
            }
        });


        reference = database.getReference().child("Bargain");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Bargain bargain = snapshot1.getValue(Bargain.class);
                        if (bargain.getProductId().equals(pId) && bargain.getBuyerId().equals(auth.getUid())) {
                            bargainId = bargain.getBargainId();
                            isExist = true;
                            noOfTries = bargain.getNoOfTries();
                            isBlocked = bargain.getBlocked();
                            timestamp = bargain.getTimestamp();

                            if (isBlocked) {
                                cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS); //1 day old
                                if (timestamp < cutoff) {
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("blocked", false);
                                    map.put("noOfTries", 5);
                                    database.getReference().child("Bargain").child(bargainId).updateChildren(map);
                                }
                            }


                            break;
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reference.addValueEventListener(valueEventListener);

        binding.btnBargain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isExist) {
                    openBargainDialog();
                } else {
                    openCancelBargainDialog();
                }
            }
        });

        database.getReference().child("Store").child(storeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sellerId = snapshot.child("sellerId").getValue(String.class);
                if (snapshot.child("storePic").exists()) {
                    storePic = snapshot.child("storePic").getValue(String.class);
                    Glide.with(ActivityProductDetails.this).load(storePic).placeholder(R.drawable.avatar).into(binding.imgStorePic);
                }
                sName = snapshot.child("storeName").getValue(String.class);
                binding.txtStoreName.setText(captializeAllFirstLetter(sName));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Ratings").child(pId).child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("rating").exists()) {
                    Float rating = snapshot.child("rating").getValue(Float.class);
                    binding.rating.setRating(rating);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Ratings").child(pId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    float rat = 0;
                    int count = 0, one = 0, two = 0, three = 0, four = 0, five = 0;
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Rating rating = snapshot1.getValue(Rating.class);
                        rat += rating.getRating();
                        int number = Math.round(rating.getRating());
                        if (number == 1.0) {
                            one++;
                        }
                        if (number == 2.0) {
                            two++;
                        }
                        if (number == 3.0) {
                            three++;
                        }
                        if (number == 4.0) {
                            four++;
                        }
                        if (number == 5.0) {
                            five++;
                        }
                        count++;
                    }
                    float finalRating = rat / count;
                    float number = BigDecimal.valueOf(finalRating)
                            .setScale(1, BigDecimal.ROUND_HALF_UP)
                            .floatValue();
                    binding.ratingBar.setRating(number);
                    binding.txtRating.setText(MessageFormat.format("{0}.0", number));
                    binding.txtUsers.setText(MessageFormat.format("{0}", count));
                    one = (int) ((one / (float) count) * 100);
                    two = (int) ((two / (float) count) * 100);
                    three = (int) ((three / (float) count) * 100);
                    four = (int) ((four / (float) count) * 100);
                    five = (int) ((five / (float) count) * 100);
                    binding.progress1.setProgress(one);
                    binding.progress2.setProgress(two);
                    binding.progress3.setProgress(three);
                    binding.progress4.setProgress(four);
                    binding.progress5.setProgress(five);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.rating.setOnRatingBarChangeListener(new SimpleRatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(SimpleRatingBar simpleRatingBar, float rating, boolean fromUser) {
                Rating rating1 = new Rating();
                rating1.setRating(rating);
                rating1.setProductId(pId);
                rating1.setUserId(auth.getUid());
                database.getReference().child("Ratings").child(pId).child(auth.getUid()).setValue(rating1);
            }
        });


    }

    private void openCancelBargainDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_bargain_cancel);
        MaterialButton btnCancelRequest = bottomSheetDialog.findViewById(R.id.btnCancelRequest);
        MaterialButton btnChangePrice = bottomSheetDialog.findViewById(R.id.btnChangePrice);
        TextView txtOriginalPrice = bottomSheetDialog.findViewById(R.id.txtOrigPrice);
        TextView txtBargainPrice = bottomSheetDialog.findViewById(R.id.txtBargainPrice);
        TextView txtBargainDate = bottomSheetDialog.findViewById(R.id.txtBargainDate);
        TextView txtRemainingTries = bottomSheetDialog.findViewById(R.id.txtRemainingTries);
        ImageView imgClose = bottomSheetDialog.findViewById(R.id.imgClose);
        EditText edPrice = bottomSheetDialog.findViewById(R.id.edPrice);
        LinearLayout parent = bottomSheetDialog.findViewById(R.id.bargainParent);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        database.getReference().child("Bargain").child(bargainId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Bargain bargain = snapshot.getValue(Bargain.class);
                Integer origPrice = bargain.getOriginalPrice();
                Integer bargainPrice = bargain.getBargainPrice();
                Integer priceDiff = origPrice - bargainPrice;
                Long timestamp = bargain.getTimestamp();

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy   hh:mm a");
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                txtBargainDate.setText(Html.fromHtml(MessageFormat.format("Bargain Date:&nbsp;  <big>{0}</big>", dateFormat.format(new Date(timestamp)))));
                //    txtBargainTime.setText(Html.fromHtml(MessageFormat.format("Bargain Time: &nbsp; <big>{0}</big>", timeFormat.format(new Date(timestamp)))));
                txtOriginalPrice.setText(Html.fromHtml(MessageFormat.format("Seller Price: &nbsp; <big> Rs.{0}</big>", origPrice)));
                txtBargainPrice.setText(Html.fromHtml(MessageFormat.format("Bargain Price: &nbsp; <big>Rs.{0}</big>", bargainPrice)));
                txtRemainingTries.setText(MessageFormat.format("Remaining Tries: {0}", noOfTries));
                //     txtPriceDiff.setText(Html.fromHtml(MessageFormat.format("Price Difference: &nbsp; <big>Rs.{0}</big>", priceDiff)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        assert imgClose != null;
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        assert btnCancelRequest != null;
        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getReference().child("Bargain").child(bargainId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        isExist = false;
                        Snackbar.make(findViewById(R.id.parent), "Bargain Request cancelled successfully", Snackbar.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });

        assert btnChangePrice != null;
        btnChangePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String price = edPrice.getText().toString();
                if (price.isEmpty()) {
                    Snackbar.make(parent, "Empty Field", Snackbar.LENGTH_SHORT).show();
                    edPrice.requestFocus();
                    return;
                }
                if (!isBlocked) {
                    if (noOfTries > 0) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("bargainPrice", Integer.valueOf(price));
                        map.put("timestamp", new Date().getTime());
                        if (noOfTries == 1) {
                            map.put("blocked", true);
                        }
                        noOfTries = noOfTries - 1;
                        map.put("noOfTries", noOfTries);
                        database.getReference().child("Bargain").child(bargainId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Snackbar.make(findViewById(R.id.parent), "Bargain request sent successfully", Snackbar.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }
                        });
                    }
                } else {
                    Snackbar.make(parent, "Try after some time.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        bottomSheetDialog.show();
    }


    private void openBargainDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_bargain);
        MaterialButton btnRequest = bottomSheetDialog.findViewById(R.id.btnRequest);
        MaterialButton btnGetCurrentPrice = bottomSheetDialog.findViewById(R.id.btnGetCurrentPrice);
        TextInputEditText edPrice = bottomSheetDialog.findViewById(R.id.edPrices);
        TextInputLayout ipPrice = bottomSheetDialog.findViewById(R.id.ipPrice);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        assert btnGetCurrentPrice != null;
        btnGetCurrentPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert edPrice != null;
                edPrice.setText(String.valueOf(price));
            }
        });

        assert btnRequest != null;
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bargainPrice = edPrice.getText().toString();
                if (!bargainPrice.isEmpty()) {
                    String key = database.getReference().child("Bargain").push().getKey();
                    Bargain bargain = new Bargain(price, Integer.valueOf(bargainPrice), key);
                    bargain.setTimestamp(new Date().getTime());
                    bargain.setProductId(pId);
                    bargain.setBuyerId(auth.getUid());
                    bargain.setStoreId(storeId);
                    bargain.setStatus("netural");
                    bargain.setNoOfTries(5);
                    bargain.setBlocked(false);
                    bargain.setSellerId(sellerId);
                    assert key != null;
                    database.getReference().child("Bargain").child(key).setValue(bargain).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Snackbar.make(findViewById(R.id.parent), "Bargain Request sent successfully", Snackbar.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                            Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    openCancelBargainDialog();
                                }
                            };
                            handler.postDelayed(runnable, 1000);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(findViewById(R.id.parent), "Bargain Request couldn't be sent. Try again!!", Snackbar.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                }
            }
        });

        bottomSheetDialog.show();

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
                    Cart product = new Cart(pId, pName, pic, price);
                    product.setVariantPId(actualProductId);
                    product.setVariantIndex(variantPos);
                    product.setSizeIndex(sizeIndex);
                    product.setDesc(pDesc);
                    product.setSize(sizeId);
                    product.setMaxLimit(maxLimit);
                    product.setColor(color);
                    product.setQuantity(1);
                    product.setStoreName(sName);
                    product.setStoreId(storeId);
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

    private void addToWishList() {
        binding.imgWish.setVisibility(View.GONE);
        binding.progressCircular.setVisibility(View.VISIBLE);
        Cart product = new Cart(pId, pName, pic, price);
        product.setVariantPId(actualProductId);
        product.setVariantIndex(variantPos);
        product.setSizeIndex(sizeIndex);
        product.setDesc(pDesc);
        product.setSize(sizeId);
        product.setMaxLimit(maxLimit);
        product.setColor(color);
        product.setStoreName(sName);
        product.setStoreId(storeId);
        database.getReference().child("WishList").child(auth.getUid()).child(actualProductId).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                binding.progressCircular.setVisibility(View.GONE);
                binding.imgWish.setVisibility(View.VISIBLE);
                binding.imgWish.setImageResource(R.drawable.ic_love_fill);
                binding.imgWish.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.skyBlue));
                isLoved = true;
                Snackbar.make(findViewById(R.id.parent), "Added to WishList", Snackbar.LENGTH_SHORT).setAction("Go to WishList", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getApplicationContext(), WishListActivity.class));
                    }
                }).show();
            }
        });
    }

    private void deleteProductFromWishList() {
        binding.imgWish.setVisibility(View.GONE);
        binding.progressCircular.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("WishList").child(auth.getUid()).child(actualProductId).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                isLoved = false;
                binding.progressCircular.setVisibility(View.GONE);
                binding.imgWish.setVisibility(View.VISIBLE);
                binding.imgWish.setImageResource(R.drawable.ic_love);
                binding.imgWish.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.black));
                Snackbar.make(findViewById(R.id.parent), "Removed from wishList", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (variantRef != null) {
            variantRef.removeEventListener(variantListener);
        }
        if (wishListRef != null) {
            wishListRef.removeEventListener(wishListListener);
        }
        if (reference != null) {
            reference.removeEventListener(valueEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ondestroy", "destroyed");
        if (variantRef != null) {
            variantRef.removeEventListener(variantListener);
        }
        if (wishListRef != null) {
            wishListRef.removeEventListener(wishListListener);
        }
        if (reference != null) {
            reference.removeEventListener(valueEventListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reference != null) {
            reference.addValueEventListener(valueEventListener);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (reference != null) {
            reference.addValueEventListener(valueEventListener);
        }
    }

}