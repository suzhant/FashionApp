package com.sushant.fashionapp.Buyer;

import static com.sushant.fashionapp.Utils.TextUtils.captializeAllFirstLetter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.devs.readmoreoption.ReadMoreOption;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.sushant.fashionapp.Adapters.VPAdapter;
import com.sushant.fashionapp.Adapters.VariantAdapter;
import com.sushant.fashionapp.Inteface.VariantClickListener;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Rating;
import com.sushant.fashionapp.Models.Size;
import com.sushant.fashionapp.Models.Variants;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.ProductRecommendation;
import com.sushant.fashionapp.Utils.TextUtils;
import com.sushant.fashionapp.databinding.ActivityProductDetailsBinding;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ActivityProductDetails extends AppCompatActivity {

    ActivityProductDetailsBinding binding;
    private final static String pub = "test_public_key_7ad13f903bd34864b8939125903e80ed";
    int price, stock, quantity, variantPos, sizeIndex;
    String pic;
    String maxLimit;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String pName, sName, pId, color, pDesc, storeId, storePic;
    String sizeId, actualProductId;
    ArrayList<Variants> variants = new ArrayList<>();
    VariantAdapter variantAdapter;
    //  List<SlideModel> list = new ArrayList<>();
    ArrayList<String> list = new ArrayList<>();
    VariantClickListener variantClickListener;
    ValueEventListener variantListener, wishListListener, ratingListener;
    DatabaseReference variantRef, wishListRef, ratingRef;
    ArrayList<Size> sizes = new ArrayList<>();
    ArrayList<Product> wishList = new ArrayList<>();
    boolean isLoved, isExist = false;
    String bargainId;
    DatabaseReference reference;
    ValueEventListener valueEventListener;
    Integer noOfTries, origPrice, bargainPrice, counterPrice;
    Boolean isBlocked, isCountered = false;
    Long timestamp, cutoff;
    String sellerId, status;
    ProgressDialog dialog;
    VPAdapter vpAdapter;
    Query query;
    String from, articleType;


    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d("oncreate", "created");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);

        price = getIntent().getIntExtra("pPrice", 0);
        pic = getIntent().getStringExtra("pPic");
        storeId = getIntent().getStringExtra("storeId");
        pId = getIntent().getStringExtra("pId");
        pDesc = getIntent().getStringExtra("pDesc");
        pName = getIntent().getStringExtra("pName");
        index = getIntent().getIntExtra("index", 0);
        from = getIntent().getStringExtra("from");
        articleType = getIntent().getStringExtra("articleType");

        if (from != null && from.equals("search")) {
            addProductToDB();
        }


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
//                for (int i = 0; i < sizes.size(); i++) {
//                    for (int j = 0; j < binding.chipGroup.getChildCount(); j++) {
//                        Chip chip = (Chip) binding.chipGroup.getChildAt(j);
//                        if (chip.getText().toString().equals(sizes.get(i).getSize())) {
//                            chip.setVisibility(View.VISIBLE);
//                            break;
//                        }
//                    }
//                }
                for (Size size : sizes) {
                    sortChips(size);
                }
                setHistoryChips(sizes);

                //  adding pic in slider

                // list.add(new SlideModel(product.getPhotos().get(i), null));
                // binding.imgSlider.setImageList(list);
                binding.viewPager.setCurrentItem(pos, true);
                //resetting wish icon to normal
                binding.imgWish.setImageResource(R.drawable.ic_love);
                binding.imgWish.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.gray_800));
            }

            @Override
            public void onProductClick(Product product, int pos) {

            }

        };


        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                //  binding.txtNoOfPics.setText(MessageFormat.format("{0}/"+list.size(),position+1));
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.txtNoOfPics.setText(MessageFormat.format("{0}/" + list.size(), position + 1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });


        initVariantRecycler();

        //fetching variants
        variantRef = database.getReference().child("Products").child(pId).child("variants");
        variantListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("variantListener", "called");
                variants.clear();
                list.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Variants product = snapshot1.getValue(Variants.class);
                    variants.add(product);
                }
                for (Variants product : variants) {
                    if (product.getPhotos() != null) {
                        list.addAll(product.getPhotos());
                    }
                }
                vpAdapter = new VPAdapter(list, ActivityProductDetails.this, true);
                binding.viewPager.setAdapter(vpAdapter);
                if (list.size() > 1) {
                    binding.txtNoOfPics.setVisibility(View.VISIBLE);
                    binding.txtNoOfPics.setText(MessageFormat.format("{0}/" + list.size(), 1));
                }

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
                                binding.imgWish.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.gray_800));
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
                    Snackbar.make(findViewById(R.id.parent), "Out of stock!", Snackbar.LENGTH_SHORT).setAnchorView(binding.bottom).show();
                }
            }
        });


        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.imgStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StorePageActivity.class);
                intent.putExtra("storeId", storeId);
                intent.putExtra("storePic", storePic);
                intent.putExtra("storeName", sName);
                startActivity(intent);
            }
        });

        binding.cartBadge.cartLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
            }
        });

        database.getReference().child("Cart").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                if (count == 0) {
                    binding.cartBadge.txtItemNumber.setVisibility(View.GONE);
                    binding.cartBadge.imgRound.setVisibility(View.GONE);
                } else {
                    binding.cartBadge.txtItemNumber.setVisibility(View.VISIBLE);
                    binding.cartBadge.imgRound.setVisibility(View.VISIBLE);
                    binding.cartBadge.txtItemNumber.setText(String.valueOf(count));
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        query = reference.orderByChild("buyerId").equalTo(auth.getUid());
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Bargain bargain = snapshot1.getValue(Bargain.class);
                        if (bargain.getProductId().equals(pId)) {
                            bargainId = bargain.getBargainId();
                            status = bargain.getStatus();
                            isExist = true;
                            noOfTries = bargain.getNoOfTries();
                            isBlocked = bargain.getBlocked();
                            timestamp = bargain.getTimestamp();
                            origPrice = bargain.getOriginalPrice();
                            bargainPrice = bargain.getBargainPrice();
                            if (bargain.getSellerPrice() != null) {
                                counterPrice = bargain.getSellerPrice();
                            }
                            if (bargain.getCountered() != null) {
                                isCountered = bargain.getCountered();
                            }
                            binding.btnBargain.setText(MessageFormat.format("Bargain Request : {0}", captializeAllFirstLetter(status)));

                            if (isBlocked) {
                                cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES); //1 day old
                                if (timestamp < cutoff) {
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("blocked", false);
                                    map.put("noOfTries", 5);
                                    database.getReference().child("Bargain").child(bargainId).updateChildren(map);
                                }
                            }

                            if (status.equals("accepted")) {
                                Long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS); //2 day old
                                if (timestamp < cutoff) {
                                    isExist = false;
                                    database.getReference().child("Bargain").child(bargainId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Query query = database.getReference().child("Cart").child(auth.getUid()).orderByChild("pId").equalTo(pId);
                                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                                                        Cart cart = snapshot11.getValue(Cart.class);
                                                        HashMap<String, Object> map = new HashMap<>();
                                                        map.put("bargainPrice", null);
                                                        database.getReference().child("Cart").child(auth.getUid()).child("Product Details")
                                                                .child(cart.getVariantPId()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                            }
                                                        });

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    });
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
        query.addValueEventListener(valueEventListener);

        binding.btnBargain.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (!isExist) {
                    openBargainDialog();
                } else {
                    if (status != null) {
                        switch (status) {
                            case "accepted":
                                openApprovedDialog();
                                break;
                            case "pending":
                            case "rejected":
                            case "countered":
                                openCancelBargainDialog();
                                break;
                            case "cancelled":
                                openBargainDialog();
                                break;
                        }
                    }
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

        binding.imgStorePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StorePageActivity.class);
                intent.putExtra("storeId", storeId);
                intent.putExtra("storePic", storePic);
                intent.putExtra("storeName", sName);
                startActivity(intent);
            }
        });

        binding.storeLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StorePageActivity.class);
                intent.putExtra("storeId", storeId);
                intent.putExtra("storePic", storePic);
                intent.putExtra("storeName", sName);
                startActivity(intent);
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

        ratingRef = database.getReference().child("Ratings").child(pId);
        ratingListener = new ValueEventListener() {
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
        };
        ratingRef.addValueEventListener(ratingListener);

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

        binding.imgChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("receiverId", storeId);
                intent.putExtra("senderId", auth.getUid());
                intent.putExtra("pic", storePic);
                intent.putExtra("receiverName", sName);
                intent.putExtra("from", "Buyer");
                startActivity(intent);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void openApprovedDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_bargain_accepted);
        TextView txtOriginalPrice = bottomSheetDialog.findViewById(R.id.txtOrigPrice);
        TextView txtBargainPrice = bottomSheetDialog.findViewById(R.id.txtBargainPrice);
        TextView txtBargainDate = bottomSheetDialog.findViewById(R.id.txtBargainDate);
        TextView txtValidity = bottomSheetDialog.findViewById(R.id.txtValidity);
        TextView txtStatus = bottomSheetDialog.findViewById(R.id.txtStatus);
        ImageView imgClose = bottomSheetDialog.findViewById(R.id.imgClose);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        LocalDateTime afterDate = dateTime.plusDays(2);
        // long longDate = (timestamp + TimeUnit.DAYS.toMillis(2));
        txtBargainDate.setText(Html.fromHtml(MessageFormat.format("Approved Date:&nbsp; {0}", dateTime.format(formatter))));
        txtValidity.setText(Html.fromHtml(MessageFormat.format("Validity:&nbsp; {0}", afterDate.format(formatter))));
        txtOriginalPrice.setText(Html.fromHtml(MessageFormat.format("Seller Price:&nbsp; <b>Rs. {0}</b>", origPrice)));
        txtBargainPrice.setText(Html.fromHtml(MessageFormat.format("Bargain Price:&nbsp; <b>Rs. {0}</b>", bargainPrice)));
        txtStatus.setText(Html.fromHtml(MessageFormat.format("Status:&nbsp; <b><span style=color:#09AEA3>{0}</span></b>", captializeAllFirstLetter(status))));

        assert imgClose != null;
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });


        bottomSheetDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void openCancelBargainDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_bargain_cancel);
        MaterialButton btnCancelRequest = bottomSheetDialog.findViewById(R.id.btnCancelRequest);
        MaterialButton btnChangePrice = bottomSheetDialog.findViewById(R.id.btnChangePrice);
        MaterialButton btnAccept = bottomSheetDialog.findViewById(R.id.btnAccept);
        TextView txtOriginalPrice = bottomSheetDialog.findViewById(R.id.txtOrigPrice);
        TextView txtCounterPrice = bottomSheetDialog.findViewById(R.id.txtCounterPrice);
        TextView txtBargainPrice = bottomSheetDialog.findViewById(R.id.txtBargainPrice);
        TextView txtBargainDate = bottomSheetDialog.findViewById(R.id.txtBargainDate);
        TextView txtRemainingTries = bottomSheetDialog.findViewById(R.id.txtRemainingTries);
        TextView txtMessage = bottomSheetDialog.findViewById(R.id.txtMessage);
        ImageView imgClose = bottomSheetDialog.findViewById(R.id.imgClose);
        EditText edPrice = bottomSheetDialog.findViewById(R.id.edPrice);
        LinearLayout parent = bottomSheetDialog.findViewById(R.id.bargainParent);
        LinearLayout linearOffer = bottomSheetDialog.findViewById(R.id.linearOffer);
        ImageView imgProduct = bottomSheetDialog.findViewById(R.id.imgProduct);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd hh:mm a");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        txtBargainDate.setText(Html.fromHtml(MessageFormat.format("Bargain Date:&nbsp;  {0}", dateTime.format(formatter))));
        txtOriginalPrice.setText(Html.fromHtml(MessageFormat.format("Original Price: &nbsp;  <b>Rs.{0}</b>", origPrice)));
        txtBargainPrice.setText(Html.fromHtml(MessageFormat.format("Bargain Price: &nbsp; <b>Rs.{0}</b>", bargainPrice)));
        txtRemainingTries.setText(MessageFormat.format("Remaining Tries: {0}", noOfTries));

        Glide.with(this).load(pic).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(imgProduct);

        if (isCountered) {
            linearOffer.setVisibility(View.VISIBLE);
        }
        if (status.equals("rejected")) {
            txtMessage.setVisibility(View.VISIBLE);
        }
        if (counterPrice != null) {
            txtCounterPrice.setVisibility(View.VISIBLE);
            txtCounterPrice.setText(Html.fromHtml(MessageFormat.format("Offered Price: &nbsp; <big> Rs.{0}</big>", counterPrice)));
        }

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                HashMap<String, Object> map = new HashMap<>();
                map.put("status", "accepted");
                map.put("timestamp", new Date().getTime());
                map.put("sellerPrice", null);
                map.put("bargainPrice", counterPrice);
                database.getReference().child("Bargain").child(bargainId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        bottomSheetDialog.dismiss();
                        Snackbar.make(findViewById(R.id.parent), "You can buy this product at Rs. " + counterPrice, Snackbar.LENGTH_SHORT).show();
                        Query query = database.getReference().child("Cart").child(auth.getUid()).orderByChild("pId").equalTo(pId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        Cart cart = snapshot1.getValue(Cart.class);
                                        HashMap<String, Object> map1 = new HashMap<>();
                                        map1.put("bargainPrice", counterPrice);
                                        database.getReference().child("Cart").child(auth.getUid())
                                                .child(cart.getVariantPId()).updateChildren(map1);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bottomSheetDialog.dismiss();
                        Snackbar.make(findViewById(R.id.parent), "Operation Failed !!", Snackbar.LENGTH_SHORT).show();
                    }
                });
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
                HashMap<String, Object> map = new HashMap<>();
                map.put("status", "cancelled");
                database.getReference().child("Bargain").child(bargainId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
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
                double wholesale = origPrice * (1 - 0.5); //calculating wholesale price
                double bargainLimit = wholesale * 1.3; //calculating bargain limit
                if (price.isEmpty()) {
                    Snackbar.make(parent, "Empty Field", Snackbar.LENGTH_SHORT).show();
                    edPrice.requestFocus();
                    return;
                }
                if (status.equals("rejected")) {
                    if (Integer.parseInt(price) <= bargainPrice) {
                        Snackbar.make(parent, "You cannot enter price lower than previous bargain rate!!", Snackbar.LENGTH_SHORT).show();
                        edPrice.requestFocus();
                        return;
                    }
                }
                if (Integer.parseInt(price) > origPrice) {
                    edPrice.requestFocus();
                    Snackbar.make(parent, "Price is too high !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (Integer.parseInt(price) < bargainLimit) {
                    edPrice.requestFocus();
                    Snackbar.make(parent, "Price is too low !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (!isBlocked) {
                    if (noOfTries > 0) {
                        dialog.show();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("bargainPrice", Integer.valueOf(price));
                        map.put("timestamp", new Date().getTime());
                        if (noOfTries == 1) {
                            map.put("blocked", true);
                        }
                        noOfTries = noOfTries - 1;
                        map.put("noOfTries", noOfTries);
                        map.put("status", "pending");
                        database.getReference().child("Bargain").child(bargainId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dialog.dismiss();
                                Snackbar.make(findViewById(R.id.parent), "Bargain request sent successfully", Snackbar.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }
                        });
                    }
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a");
                    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                    LocalDateTime afterDate = dateTime.plusDays(1);
                    Snackbar.make(parent, "Sorry! Try after " + afterDate.format(formatter), Snackbar.LENGTH_SHORT).show();
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
        LinearLayout parent = bottomSheetDialog.findViewById(R.id.parent);
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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                assert edPrice != null;
                String bargainPrice = edPrice.getText().toString();
                double wholesale = price * (1 - 0.5); //calculating wholesale price
                double bargainLimit = wholesale * 1.3; //calculating bargain limit
                if (bargainPrice.isEmpty()) {
                    edPrice.requestFocus();
                    Snackbar.make(parent, "Empty field!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (Integer.parseInt(bargainPrice) > price) {
                    edPrice.requestFocus();
                    Snackbar.make(parent, "Your Price is too high !!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (Integer.parseInt(bargainPrice) <= bargainLimit) {
                    edPrice.requestFocus();
                    Snackbar.make(parent, "Your Price is too low !!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                dialog.show();
                if (!isExist) {
                    String key = database.getReference().child("Bargain").push().getKey();
                    Bargain bargain = new Bargain(price, Integer.valueOf(bargainPrice), key);
                    bargain.setTimestamp(new Date().getTime());
                    bargain.setProductId(pId);
                    bargain.setBuyerId(auth.getUid());
                    bargain.setStoreId(storeId);
                    bargain.setStatus("pending");
                    bargain.setNoOfTries(4);
                    bargain.setBlocked(false);
                    bargain.setSellerId(sellerId);
                    assert key != null;
                    database.getReference().child("Bargain").child(key).setValue(bargain).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialog.dismiss();
                            bottomSheetDialog.dismiss();
                            Snackbar.make(findViewById(R.id.parent), "Bargain Request sent successfully", Snackbar.LENGTH_SHORT).setAction("Negotiate", new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onClick(View view) {
                                    openCancelBargainDialog();
                                }
                            }).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(findViewById(R.id.parent), "Bargain Request couldn't be sent. Try again!!", Snackbar.LENGTH_SHORT).show();
                            bottomSheetDialog.dismiss();
                        }
                    });
                } else {
                    if (noOfTries > 0) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("bargainPrice", Integer.valueOf(bargainPrice));
                        map.put("timestamp", new Date().getTime());
                        if (noOfTries == 1) {
                            map.put("blocked", true);
                        }
                        noOfTries = noOfTries - 1;
                        map.put("noOfTries", noOfTries);
                        map.put("status", "pending");
                        map.put("sellerPrice", null);
                        map.put("countered", false);
                        database.getReference().child("Bargain").child(bargainId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dialog.dismiss();
                                bottomSheetDialog.dismiss();
                                Snackbar.make(findViewById(R.id.parent), "Bargain Request sent successfully", Snackbar.LENGTH_SHORT).setAction("Negotiate", new View.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onClick(View view) {
                                        openCancelBargainDialog();
                                    }
                                }).show();
                            }
                        });
                    } else {
                        dialog.dismiss();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a");
                        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                        LocalDateTime afterDate = dateTime.plusDays(1);
                        Snackbar.make(parent, "Sorry! Try after " + afterDate.format(formatter), Snackbar.LENGTH_SHORT).show();
                    }
                }

            }
        });

        bottomSheetDialog.show();

    }


    private void initVariantRecycler() {
        Log.d("variantAdapter", "created");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.variantRecycler.setLayoutManager(layoutManager);
        variantAdapter = new VariantAdapter(variants, this, variantClickListener, index);
        binding.variantRecycler.setAdapter(variantAdapter);
    }

    private void addProductToCart() {

        Snackbar snackbar = getSnackbar();
        dialog.show();
        database.getReference().child("Cart").child(auth.getUid()).child(actualProductId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Cart product = snapshot.getValue(Cart.class);
                    quantity = product.getQuantity();
                    if (quantity < 5) {
                        updateCartQuantity(quantity);
                        updateStock(stock);
                        dialog.dismiss();
                        snackbar.show();
                        addProductToDB();
                    } else {
                        dialog.dismiss();
                        Snackbar.make(findViewById(R.id.parent), "Maximum Limit is reached!", Snackbar.LENGTH_SHORT).setAnchorView(binding.bottom).show();
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
                    product.setStock(stock);
                    product.setArticleType(articleType);
                    product.setTimeStamp(new Date().getTime());
                    if (status != null) {
                        if (status.equals("accepted")) {
                            product.setBargainPrice(bargainPrice);
                        }
                    }
                    database.getReference().child("Cart").child(auth.getUid()).child(actualProductId).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            addProductToDB();
                            updateStock(stock);
                            dialog.dismiss();
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

    private void addProductToDB() {
        ProductRecommendation productRecommendation = new ProductRecommendation(pId, pic, articleType, ActivityProductDetails.this);
        productRecommendation.recommend();
    }

    @NonNull
    private Snackbar getSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), "Added to cart",
                Snackbar.LENGTH_SHORT).setAction("Go to Cart", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityProductDetails.this, CartActivity.class));
            }
        }).setAnchorView(binding.bottom);
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
        FirebaseDatabase.getInstance().getReference().child("Cart").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(actualProductId)
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
        product.setStock(stock);
        product.setArticleType(articleType);
        product.setTimeStamp(new Date().getTime());
        if (status != null) {
            if (status.equals("accepted")) {
                product.setBargainPrice(bargainPrice);
            }
        }
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

    public void setHistoryChips(ArrayList<Size> sizes) {
        binding.chipGroup.removeAllViews();
        for (Size size : sizes) {
            Chip mChip = (Chip) this.getLayoutInflater().inflate(R.layout.item_chip_history, binding.chipGroup, false);
            mChip.setText(size.getSize());
            binding.chipGroup.addView(mChip);
        }
    }


    private void sortChips(Size s) {
        //find position of s in a list
        int index = sizes.indexOf(s);

        //first look at the front
        if (index == 0 && s.getSize().equals("L")) {
            //look at back
            for (Size size : sizes) {
                if (size.getSize().equals("S") || size.getSize().equals("M")) {
                    int nextIndex = sizes.indexOf(size);
                    Collections.swap(sizes, index, nextIndex);
                }
            }
        } else if (index != 0 && s.getSize().equals("L")) {
            //look at front
            for (int i = 0; i < index; i++) {
                Size size = sizes.get(i);
                if (size.getSize().equals("XL")) {
                    int nextIndex = sizes.indexOf(size);
                    Collections.swap(sizes, index, nextIndex);
                }
            }
            //look at back
            for (int i = index; i < sizes.size(); i++) {
                Size size = sizes.get(i);
                if (size.getSize().equals("S") || size.getSize().equals("M")) {
                    int nextIndex = sizes.indexOf(size);
                    Collections.swap(sizes, index, nextIndex);
                }
            }
        }

        //first look at the front
        if (index != 0 && s.getSize().equals("S")) {
            //look at front
            for (int i = 0; i < index; i++) {
                Size size = sizes.get(i);
                if (size.getSize().equals("M") || size.getSize().equals("L") || size.getSize().equals("XL")) {
                    int nextIndex = sizes.indexOf(size);
                    Collections.swap(sizes, index, nextIndex);
                }
            }
        }

        //first look at the front
        if (index == 0 && s.getSize().equals("M")) {
            //look at back
            for (Size size : sizes) {
                if (size.getSize().equals("S")) {
                    int nextIndex = sizes.indexOf(size);
                    Collections.swap(sizes, index, nextIndex);
                }
            }
        } else if (index != 0 && s.getSize().equals("M")) {
            //look at front
            for (int i = 0; i < index; i++) {
                Size size = sizes.get(i);
                if (size.getSize().equals("L") && size.getSize().equals("XL")) {
                    int nextIndex = sizes.indexOf(size);
                    Collections.swap(sizes, index, nextIndex);
                }
            }
            //look at back
            for (int i = index; i < sizes.size(); i++) {
                Size size = sizes.get(i);
                if (size.getSize().equals("S")) {
                    int nextIndex = sizes.indexOf(size);
                    Collections.swap(sizes, index, nextIndex);
                }
            }
        }
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
        if (query != null) {
            query.removeEventListener(valueEventListener);
        }
        if (ratingRef != null) {
            ratingRef.removeEventListener(ratingListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (variantRef != null) {
            variantRef.addValueEventListener(variantListener);
        }
        if (wishListRef != null) {
            wishListRef.addValueEventListener(wishListListener);
        }
        if (query != null) {
            query.addValueEventListener(valueEventListener);
        }
        if (ratingRef != null) {
            ratingRef.addValueEventListener(ratingListener);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (variantRef != null) {
            variantRef.addValueEventListener(variantListener);
        }
        if (wishListRef != null) {
            wishListRef.addValueEventListener(wishListListener);
        }
        if (query != null) {
            query.addValueEventListener(valueEventListener);
        }
        if (ratingRef != null) {
            ratingRef.addValueEventListener(ratingListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (variantRef != null) {
            variantRef.removeEventListener(variantListener);
        }
        if (wishListRef != null) {
            wishListRef.removeEventListener(wishListListener);
        }
        if (query != null) {
            query.removeEventListener(valueEventListener);
        }
        if (ratingRef != null) {
            ratingRef.removeEventListener(ratingListener);
        }
    }
}