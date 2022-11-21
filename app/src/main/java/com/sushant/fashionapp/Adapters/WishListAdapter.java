package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Buyer.ActivityProductDetails;
import com.sushant.fashionapp.Buyer.CartActivity;
import com.sushant.fashionapp.Buyer.WishListActivity;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.TextUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.viewHolder> {

    ArrayList<Product> products;
    Context context;
    WishListActivity wishListActivity;
    int stock;

    public WishListAdapter(ArrayList<Product> products, Context context) {
        this.products = products;
        this.context = context;
        this.wishListActivity = (WishListActivity) context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wishlist_item_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Product product = products.get(position);
        Glide.with(context).load(product.getpPic()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(holder.imgProduct);
        holder.txtStoreName.setText(TextUtils.captializeAllFirstLetter(product.getStoreName()));
        holder.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}</big>", product.getpPrice())));
        holder.txtProductName.setText(TextUtils.captializeAllFirstLetter(product.getpName()));
        holder.txtSize.setText(MessageFormat.format("Size: {0}", product.getSize()));
        holder.txtColor.setText(MessageFormat.format("Color: {0}", product.getColor()));


        FirebaseDatabase.getInstance().getReference().child("Products").child(product.getpId()).child("variants").child(String.valueOf(product.getVariantIndex()))
                .child("sizes")
                .child(String.valueOf(product.getSizeIndex())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("stock").exists()) {
                    stock = snapshot.child("stock").getValue(Integer.class);
                    if (stock < 5) {
                        holder.txtStock.setVisibility(View.VISIBLE);
                        if (stock == 0) {
                            holder.txtStock.setText("out of stock!");
                        } else {
                            holder.txtStock.setText(MessageFormat.format("only {0} item(s) in stock", stock));
                        }
                    } else {
                        holder.txtStock.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.imgProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityProductDetails.class);
                intent.putExtra("pPic", product.getPreviewPic());
                intent.putExtra("pName", product.getpName());
                intent.putExtra("pPrice", product.getpPrice());
                intent.putExtra("pId", product.getpId());
                intent.putExtra("sName", product.getStoreName());
                intent.putExtra("pDesc", product.getDesc());
                intent.putExtra("index", product.getVariantIndex());
                context.startActivity(intent);
            }
        });

        holder.txtProductName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityProductDetails.class);
                intent.putExtra("pPic", product.getpPic());
                intent.putExtra("pName", product.getpName());
                intent.putExtra("pPrice", product.getpPrice());
                intent.putExtra("pId", product.getpId());
                intent.putExtra("stock", product.getStock());
                intent.putExtra("sName", product.getStoreName());
                intent.putExtra("index", product.getVariantIndex());
                context.startActivity(intent);
            }
        });

        holder.btnDeleteFromWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProductFromWishList(product);
            }
        });

        holder.btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar.make(wishListActivity.findViewById(R.id.wishListLyt), "Added to Cart", Snackbar.LENGTH_SHORT)
                        .setAnchorView(wishListActivity.findViewById(R.id.fabAddToCart)).setAction("Go to Cart", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                context.startActivity(new Intent(context, CartActivity.class));
                            }
                        });
                FirebaseDatabase.getInstance().getReference().child("Cart").child(FirebaseAuth.getInstance().getUid()).child("Product Details")
                        .child(product.getVariantPId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int quantity = snapshot.child("quantity").getValue(Integer.class);
                            if (stock != 0) {
                                if (quantity < 5) {
                                    updateCartQuantity(quantity, product);
                                    updateStock(product);
                                    snackbar.show();
                                } else {
                                    Snackbar.make(wishListActivity.findViewById(R.id.wishListLyt), "Max limit exceeded!", Snackbar.LENGTH_SHORT)
                                            .setAnchorView(wishListActivity.findViewById(R.id.fabAddToCart)).show();
                                }
                            } else {
                                Snackbar.make(wishListActivity.findViewById(R.id.wishListLyt), "out of stock!", Snackbar.LENGTH_SHORT)
                                        .setAnchorView(wishListActivity.findViewById(R.id.fabAddToCart)).show();
                            }
                        } else {
                            Product item = new Product(product.getpId(), product.getpName(), product.getpPic(), product.getpPrice(), product.getStoreName(), product.getStock());
                            item.setVariantPId(product.getVariantPId());
                            item.setVariantIndex(product.getVariantIndex());
                            item.setSizeIndex(product.getSizeIndex());
                            item.setDesc(product.getDesc());
                            item.setSize(product.getSize());
                            item.setMaxLimit(product.getMaxLimit());
                            item.setColor(product.getColor());
                            item.setQuantity(1);
                            if (stock != 0) {
                                updateStock(product);
                            }
                            FirebaseDatabase.getInstance().getReference().child("Cart").child(FirebaseAuth.getInstance().getUid()).child("Product Details")
                                    .child(item.getVariantPId()).setValue(item).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        });

    }

    private void deleteProductFromWishList(Product product) {
        FirebaseDatabase.getInstance().getReference().child("WishList").child(FirebaseAuth.getInstance().getUid()).child(product.getVariantPId()).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Snackbar.make(wishListActivity.findViewById(R.id.wishListLyt), "Deleted Successfully", Snackbar.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final MaterialButton btnAddToCart;
        private final MaterialButton btnDeleteFromWishList;
        private final ImageView imgProduct;
        private final TextView txtStoreName;
        private final TextView txtProductName;
        private final TextView txtPrice;
        private final TextView txtSize;
        private final TextView txtColor;
        private final TextView txtStock;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            btnAddToCart = itemView.findViewById(R.id.btnAddCart);
            btnDeleteFromWishList = itemView.findViewById(R.id.btnDeleteFromWishList);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtProductName = itemView.findViewById(R.id.txtProdName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtSize = itemView.findViewById(R.id.txtSize);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtStock = itemView.findViewById(R.id.txtStock);
        }
    }

    private void updateStock(Product product) {
        int s = stock - 1;
        HashMap<String, Object> stock = new HashMap<>();
        stock.put("stock", s);
        FirebaseDatabase.getInstance().getReference().child("Products").child(product.getpId()).child("variants").child(String.valueOf(product.getVariantIndex())).child("sizes")
                .child(String.valueOf(product.getSizeIndex())).updateChildren(stock);
    }

    private void updateCartQuantity(int q, Product product) {
        q = q + 1;
        HashMap<String, Object> quantity = new HashMap<>();
        quantity.put("quantity", q);
        FirebaseDatabase.getInstance().getReference().child("Cart").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("Product Details")
                .child(product.getVariantPId())
                .updateChildren(quantity);
    }
}
