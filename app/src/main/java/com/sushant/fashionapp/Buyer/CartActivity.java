package com.sushant.fashionapp.Buyer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.ActivityHomePage;
import com.sushant.fashionapp.Adapters.CartAdapter;
import com.sushant.fashionapp.Inteface.ProductClickListener;
import com.sushant.fashionapp.Inteface.QuantityListener;
import com.sushant.fashionapp.Inteface.SwipeHelper;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.ImageUtils;
import com.sushant.fashionapp.databinding.ActivityCartBinding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CartActivity extends AppCompatActivity implements SwipeHelper.ItemSwipeListener {

    ActivityCartBinding binding;
    CartAdapter cartAdapter;
    ArrayList<Cart> products = new ArrayList<>();
    FirebaseDatabase database;
    FirebaseAuth auth;
    ProductClickListener productClickListener;
    public ArrayList<Product> checkedProducts = new ArrayList<>();
    ValueEventListener valueEventListener;
    DatabaseReference databaseReference;
    int size = 0;
    int sum;
    public boolean isActionMode = false;
    int stock;
    SwipeHelper helper;
    QuantityListener listener;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait..");
        dialog.setCancelable(false);


        binding.imgBack.setOnClickListener(view -> {
            if (isActionMode) {
                disableActionMode();
            } else {
                onBackPressed();
            }

        });




        productClickListener = (product, b) -> {
            if (b) {
                checkedProducts.add(product);
                size++;
            } else {
                checkedProducts.remove(product);
                size--;
            }
            updateToolbarText(size);
        };

        listener = (cart, quantity, type) -> {
            if (type.equals("plus")) {
                updateInventory(cart, cart.getStock() - 1, quantity + 1);
            } else if (type.equals("minus")) {
                updateInventory(cart, cart.getStock() + 1, quantity - 1);
            }

        };

        helper = new SwipeHelper(this, binding.cartRecycler,this) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                int deleteId = R.drawable.ic_fluent_delete_24_regular;
                Bitmap deleteIcon = ImageUtils.getBitmapFromVectorDrawable(getApplicationContext(), deleteId);
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Delete",
                        deleteIcon,
                        Color.parseColor("#FF3C30"),
                        pos -> {
                            // TODO: onDelete
                            Product product = products.get(pos);
                            //        showDeleteMessage(product);
                            deleteProductFromDB(product);
                        }
                ));
                int idWishList = R.drawable.fav_icon;
                Bitmap favIcon = ImageUtils.getBitmapFromVectorDrawable(getApplicationContext(), idWishList);
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "WishList",
                        favIcon,
                        Color.parseColor("#FF9502"),
                        pos -> {
                            // TODO: onWishList
                            Product product = products.get(pos);
                            addToWishList(product);
                        }
                ));
            }

        };


        initRecyclerView();
        databaseReference = database.getReference().child("Cart").child(Objects.requireNonNull(auth.getUid()));
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                sum = 0;
                HashSet<String> set = new HashSet<>();
                int shipping;
                int price;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Cart product = snapshot1.getValue(Cart.class);
                    assert product != null;
                    if (product.getpId() != null) {
                        products.add(product);
                        if (product.getBargainPrice() != null) {
                            price = product.getBargainPrice();
                        } else {
                            price = product.getpPrice();
                        }
                        sum = sum + price * product.getQuantity();
                        set.add(product.getStoreName());
                    }
                }
                Collections.sort(products, Cart.newToOld);
                shipping = set.size() * 70;
                sum = sum + shipping;
                binding.txtPrice.setText(Html.fromHtml(MessageFormat.format("Total: <span style=color:#09AEA3> <b>Rs. <big>{0}</big></b></span>", sum)));
                binding.txtShipping.setText(Html.fromHtml(MessageFormat.format("Shipping: <span style=color:#09AEA3> Rs. <big>{0}</big></span>", shipping)));
                cartAdapter.notifyDataSetChanged();
                showorhideCartBottomlyt();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);

        database.getReference().child("Bargain").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Bargain bargain = snapshot1.getValue(Bargain.class);
                        assert bargain != null;
                        if (bargain.getBuyerId().equals(auth.getUid()) && bargain.getStatus().equals("accepted")) {
                            Long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS); //2 day old
                            if (bargain.getTimestamp() < cutoff) {
                                Query query = database.getReference().child("Cart").child(auth.getUid()).orderByChild("pId").equalTo(bargain.getProductId());
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                                            Cart cart = snapshot11.getValue(Cart.class);
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("bargainPrice", null);
                                            database.getReference().child("Cart").child(auth.getUid())
                                                    .child(cart.getVariantPId()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    database.getReference().child("Bargain").child(bargain.getBargainId()).removeValue();
                                                }
                                            });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (size > 0) {
                    for (Product product : checkedProducts) {
                        deleteProductFromDB(product);
                    }

                    refreshAdapter();
                    size = 0;
                    checkedProducts.clear();
                    updateToolbarText(size);
                } else {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.cartLayout), "No Item Selected!!",
                            Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });

        binding.btnShopNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CartActivity.this, ActivityHomePage.class));
            }
        });

        binding.btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CheckOutAcitivity.class);
                intent.putExtra("type", "cart");
                startActivity(intent);
            }
        });


    }

    private void addToWishList(Product product) {
        deleteProductFromDB(product);
        database.getReference().child("WishList").child(Objects.requireNonNull(auth.getUid())).child(product.getVariantPId()).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Snackbar.make(findViewById(R.id.cartLayout), "Added to WishList", Snackbar.LENGTH_SHORT).setAction("Go to WishList", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getApplicationContext(), WishListActivity.class));
                    }
                }).setAnchorView(binding.cardView).show();
            }
        });
    }

    private void disableActionMode() {
        layoutInNormalMode();
        binding.cardView.setVisibility(View.VISIBLE);
        isActionMode = false;
        size = 0;
        checkedProducts.clear();
        cartAdapter.notifyDataSetChanged();
    }

    private void showDeleteMessage(Cart product) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.cartLayout), "Cart Deleted",
                Snackbar.LENGTH_LONG).setAnchorView(binding.cardView);
        snackbar.show();
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                //see Snackbar.Callback docs for event details
                checkedProducts.clear();
            }

            @Override
            public void onShown(Snackbar snackbar) {

            }
        });
    }

    private void showorhideCartBottomlyt() {
        if (products.size() == 0) {
            binding.emptyCartLyt.setVisibility(View.VISIBLE);
            binding.cardView.setVisibility(View.GONE);
        } else {
            binding.cardView.setVisibility(View.VISIBLE);
            binding.emptyCartLyt.setVisibility(View.GONE);
        }
        if (!isActionMode) {
            layoutInNormalMode();
        }
    }

    private void undoDeleteActionFromCart() {
        for (Product p : checkedProducts) {
            undoDeleteFromDB(p);
        }
        checkedProducts.clear();
    }

    private void undoDeleteFromDB(Product p) {
        FirebaseDatabase.getInstance().getReference().child("Products").child(p.getpId()).child("variants").child(String.valueOf(p.getVariantIndex()))
                .child("sizes")
                .child(String.valueOf(p.getSizeIndex())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("stock").exists()) {
                    stock = snapshot.child("stock").getValue(Integer.class);
                    updateStock(p, stock - p.getQuantity());
                }
                database.getReference().child("Cart").child(auth.getUid()).child(p.getVariantPId()).setValue(p);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    private void deleteProductFromCart() {
//        for (Product p : checkedProducts) {
//            deleteProductFromDB(p);
//        }
//    }

    private void deleteProductFromDB(Product p) {
        dialog.show();
        FirebaseDatabase.getInstance().getReference().child("Products").child(p.getpId()).child("variants").child(String.valueOf(p.getVariantIndex()))
                .child("sizes")
                .child(String.valueOf(p.getSizeIndex())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("stock").exists()) {
                    stock = snapshot.child("stock").getValue(Integer.class);
                    updateStock(p, stock + p.getQuantity());
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put(p.getVariantPId(), null);
                database.getReference().child("Cart").child(auth.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(CartActivity.this, LinearLayoutManager.VERTICAL, false);
        binding.cartRecycler.setLayoutManager(layoutManager);
        cartAdapter = new CartAdapter(products, CartActivity.this, productClickListener, listener);
        binding.cartRecycler.setAdapter(cartAdapter);
    }

    private void refreshAdapter() {
        cartAdapter = new CartAdapter(products, CartActivity.this, productClickListener, listener);
        binding.cartRecycler.setAdapter(cartAdapter);
    }

    public void selectedItem() {
        if (!isActionMode) {
            isActionMode = true;
            layoutInActionMode();
            binding.cardView.setVisibility(View.GONE);
            cartAdapter.notifyDataSetChanged();
        }
    }

    private void layoutInActionMode() {
        binding.toolbarTxt.setVisibility(View.GONE);
        binding.count.setVisibility(View.VISIBLE);
        binding.imgBack.setImageResource(R.drawable.ic_close_24);
        binding.imgDelete.setVisibility(View.VISIBLE);
    }

    private void layoutInNormalMode() {
        binding.imgBack.setImageResource(R.drawable.ic_arrow_back_24);
        binding.count.setVisibility(View.GONE);
        binding.toolbarTxt.setVisibility(View.VISIBLE);
        binding.imgDelete.setVisibility(View.GONE);
    }

    private void updateToolbarText(int size) {
        if (size < 1) {
            binding.count.setText(size + " item selected");
        } else {
            binding.count.setText(size + " items selected");
        }
    }


    @Override
    public void onDestroy() {
        if (databaseReference != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isActionMode) {
            disableActionMode();
        } else {
           super.onBackPressed();
        }
    }

    private void updateStock(Product product, int s) {
        HashMap<String, Object> stock = new HashMap<>();
        stock.put("stock", s);
        FirebaseDatabase.getInstance().getReference().child("Products").child(product.getpId()).child("variants").child(String.valueOf(product.getVariantIndex()))
                .child("sizes")
                .child(String.valueOf(product.getSizeIndex())).updateChildren(stock);
    }

    private void updateInventory(Product product, int s, int q) {
        dialog.show();
        HashMap<String, Object> stock = new HashMap<>();
        stock.put("stock", s);
        FirebaseDatabase.getInstance().getReference().child("Products").child(product.getpId()).child("variants").child(String.valueOf(product.getVariantIndex()))
                .child("sizes")
                .child(String.valueOf(product.getSizeIndex())).updateChildren(stock).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                HashMap<String, Object> quantity = new HashMap<>();
                quantity.put("quantity", q);
                FirebaseDatabase.getInstance().getReference().child("Cart").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .child(product.getVariantPId()).updateChildren(quantity).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onSwipe(int from, int to) {
        Collections.swap(products, from, to);
        cartAdapter.notifyItemMoved(from, to);
    }
}