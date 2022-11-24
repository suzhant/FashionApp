package com.sushant.fashionapp.Buyer;

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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.ActivityHomePage;
import com.sushant.fashionapp.Adapters.CartAdapter;
import com.sushant.fashionapp.Inteface.ProductClickListener;
import com.sushant.fashionapp.Inteface.SwipeHelper;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.ImageUtils;
import com.sushant.fashionapp.databinding.ActivityCartBinding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CartActivity extends AppCompatActivity {

    ActivityCartBinding binding;
    CartAdapter cartAdapter;
    ArrayList<Product> products = new ArrayList<>();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isActionMode) {
                    disableActionMode();
                } else {
                    onBackPressed();
                }

            }
        });



        productClickListener = new ProductClickListener() {
            @Override
            public void onClick(Product product, boolean b) {
                if (b) {
                    checkedProducts.add(product);
                    size++;
                } else {
                    checkedProducts.remove(product);
                    size--;
                }
                updateToolbarText(size);
            }
        };

        helper = new SwipeHelper(this, binding.cartRecycler) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                int deleteId = R.drawable.ic_fluent_delete_24_regular;
                Bitmap deleteIcon = ImageUtils.getBitmapFromVectorDrawable(getApplicationContext(), deleteId);
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "Delete",
                        deleteIcon,
                        Color.parseColor("#FF3C30"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                // TODO: onDelete
                                Product product = products.get(pos);
                                showDeleteMessage(product);
                                deleteProductFromDB(product);
                                products.remove(pos);
                                cartAdapter.notifyItemRemoved(pos);
                            }
                        }
                ));
                int idWishList = R.drawable.fav_icon;
                Bitmap favIcon = ImageUtils.getBitmapFromVectorDrawable(getApplicationContext(), idWishList);
                underlayButtons.add(new SwipeHelper.UnderlayButton(
                        "WishList",
                        favIcon,
                        Color.parseColor("#FF9502"),
                        new SwipeHelper.UnderlayButtonClickListener() {
                            @Override
                            public void onClick(int pos) {
                                // TODO: onWishList
                                Product product = products.get(pos);
                                addToWishList(product, pos);
                            }
                        }
                ));
            }

        };


        initRecyclerView();
        databaseReference = database.getReference().child("Cart").child(Objects.requireNonNull(auth.getUid())).child("Product Details");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                sum = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    assert product != null;
                    if (product.getpId() != null) {
                        products.add(product);
                        sum = sum + product.getpPrice() * product.getQuantity();
                    }
                }
                binding.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}</big>", sum)));
                cartAdapter.notifyDataSetChanged();
                showorhideCartBottomlyt();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);



        binding.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (size > 0) {
                    deleteProductFromCart();
                    refreshAdapter();
                    size = 0;
                    updateToolbarText(size);
                    showDeleteMessage(null);
                    isActionMode = false;
                    cartAdapter.notifyDataSetChanged();
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


    }

    private void addToWishList(Product product, int pos) {
        deleteProductFromDB(product);
        database.getReference().child("WishList").child(Objects.requireNonNull(auth.getUid())).child(product.getVariantPId()).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Snackbar.make(findViewById(R.id.cartLayout), "Added to WishList", Snackbar.LENGTH_SHORT).setAction("Go to WishList", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getApplicationContext(), WishListActivity.class));
                    }
                }).show();
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

    private void showDeleteMessage(Product product) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.cartLayout), "Cart Deleted",
                Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (product != null) {
                    undoDeleteFromDB(product);
                    products.add(product);
                } else {
                    undoDeleteActionFromCart();
                }

            }
        });
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
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
                database.getReference().child("Cart").child(auth.getUid()).child("Product Details").child(p.getVariantPId()).setValue(p);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteProductFromCart() {
        for (Product p : checkedProducts) {
            deleteProductFromDB(p);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void deleteProductFromDB(Product p) {
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
                database.getReference().child("Cart").child(auth.getUid()).child("Product Details").updateChildren(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(CartActivity.this, LinearLayoutManager.VERTICAL, false);
        binding.cartRecycler.setLayoutManager(layoutManager);
        cartAdapter = new CartAdapter(products, CartActivity.this, productClickListener);
        binding.cartRecycler.setAdapter(cartAdapter);
    }

    private void refreshAdapter() {
        cartAdapter = new CartAdapter(products, CartActivity.this, productClickListener);
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
        if (size == 0) {
            binding.count.setText(size + " item selected");
        } else if (size == 1) {
            binding.count.setText(size + " item selected");
        } else if (size > 1) {
            binding.count.setText(size + " items selected");
        }
    }


    @Override
    public void onDestroy() {
        databaseReference.removeEventListener(valueEventListener);
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
}