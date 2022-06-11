package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.ActivityProductDetails;
import com.sushant.fashionapp.CartActivity;
import com.sushant.fashionapp.Inteface.ProductClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.CartDiffUtils;
import com.sushant.fashionapp.Utils.TextUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.viewHolder> {
    ArrayList<Product> products;
    Context context;
    ProductClickListener productClickListener;
    private CartActivity cartActivity;

    public CartAdapter(ArrayList<Product> products, Context context, ProductClickListener productClickListener) {
        this.products = products;
        this.context = context;
        this.productClickListener = productClickListener;
        this.cartActivity = (CartActivity) context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_items_layout, parent, false);
        return new viewHolder(view, cartActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            Bundle bundle = (Bundle) payloads.get(0);
            for (String key : bundle.keySet()) {
                switch (key) {
                    case "newName":
                        holder.txtProductName.setText(bundle.getString("newName"));
                        break;
                    case "newPrice":
                        holder.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}</big>", bundle.getInt("newPrice"))));
                        break;
                    case "newStoreName":
                        holder.txtStoreName.setText(TextUtils.captializeAllFirstLetter(bundle.getString("newStoreName")));
                        break;
                    case "newQuantity":
                        holder.txtQuantity.setText(String.valueOf(bundle.getInt("newQuantity")));
                        break;
                }
            }
        }

    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Product product = products.get(position);
        Glide.with(context).load(product.getpPic()).placeholder(R.drawable.avatar).into(holder.imgProduct);
        holder.txtStoreName.setText(TextUtils.captializeAllFirstLetter(product.getStoreName()));
        holder.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}</big>", product.getpPrice())));
        holder.txtProductName.setText(TextUtils.captializeAllFirstLetter(product.getpName()));
        //     holder.txtStock.setText(MessageFormat.format("Stock: {0}", product.getStock()));
        holder.txtSize.setText(MessageFormat.format("Size: {0}", product.getSize()));
        holder.txtQuantity.setText(String.valueOf(product.getQuantity()));


        if (product.getQuantity() > 1) {
            holder.imgMinus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
        }
        if (product.getQuantity() == product.getStock()) {
            holder.imgPlus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.hintColor)));
        }
        if (product.getQuantity() < product.getStock()) {
            holder.imgPlus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.skyBlue)));
        }
        if (product.getQuantity() < 2) {
            holder.imgMinus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.hintColor)));
        }
        if (cartActivity.checkedProducts.isEmpty()) {
            holder.cardView.setStrokeWidth(1);
        }

        if (cartActivity.isActionMode) {
            holder.quantityLayout.setVisibility(View.GONE);
        } else {
            holder.quantityLayout.setVisibility(View.VISIBLE);
        }

        holder.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cartActivity.isActionMode) {
                    if (product.getStock() > product.getQuantity()) {
                        product.setQuantity(product.getQuantity() + 1);
                        updateCartQuantity(product);
                    }
                }

            }
        });

        holder.imgMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cartActivity.isActionMode) {
                    if (product.getQuantity() > 1) {
                        product.setQuantity(product.getQuantity() - 1);
                        updateCartQuantity(product);
                    }
                }
            }
        });

        holder.imgProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cartActivity.isActionMode) {
                    Intent intent = new Intent(context, ActivityProductDetails.class);
                    intent.putExtra("pPic", product.getpPic());
                    intent.putExtra("pName", product.getpName());
                    intent.putExtra("pPrice", product.getpPrice());
                    intent.putExtra("pId", product.getpId());
                    intent.putExtra("stock", product.getStock());
                    intent.putExtra("sName", product.getStoreName());
                    context.startActivity(intent);
                }

            }
        });

        holder.txtProductName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cartActivity.isActionMode) {
                    {
                        Intent intent = new Intent(context, ActivityProductDetails.class);
                        intent.putExtra("pPic", product.getpPic());
                        intent.putExtra("pName", product.getpName());
                        intent.putExtra("pPrice", product.getpPrice());
                        intent.putExtra("pId", product.getpId());
                        intent.putExtra("stock", product.getStock());
                        intent.putExtra("sName", product.getStoreName());
                        context.startActivity(intent);
                    }
                }

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartActivity.isActionMode) {
                    if (holder.cardView.getStrokeWidth() == 1) {
                        holder.cardView.setStrokeWidth(5);
                        productClickListener.onClick(product, true);
                    } else {
                        holder.cardView.setStrokeWidth(1);
                        productClickListener.onClick(product, false);
                    }

                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!cartActivity.isActionMode) {
                    cartActivity.selectedItem();
                    productClickListener.onClick(product, true);
                    holder.cardView.setStrokeWidth(5);
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtStoreName;
        private final TextView txtProductName;
        private final TextView txtPrice;
        private final ImageView imgMinus;
        private final ImageView imgPlus;
        private final TextView txtQuantity;
        private final TextView txtSize;
        private final MaterialCardView cardView;
        private final LinearLayout quantityLayout;
        private CartActivity cartActivity;

        public viewHolder(@NonNull View itemView, CartActivity cartActivity) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtProductName = itemView.findViewById(R.id.txtProdName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgMinus = itemView.findViewById(R.id.imgMinus);
            imgPlus = itemView.findViewById(R.id.imgPlus);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            //      txtStock = itemView.findViewById(R.id.txtStock);
            cardView = itemView.findViewById(R.id.cart_layout_parent);
            txtSize = itemView.findViewById(R.id.txtSize);
            quantityLayout = itemView.findViewById(R.id.quantityLyt);
            this.cartActivity = cartActivity;
        }
    }

    private void updateCartQuantity(Product product) {
        HashMap<String, Object> quantity = new HashMap<>();
        quantity.put("quantity", product.getQuantity());
        FirebaseDatabase.getInstance().getReference().child("Cart").child(FirebaseAuth.getInstance().getUid()).
                child("Product Details").child(product.getpId() + product.getSize()).updateChildren(quantity);

    }

    public void updateCartList(ArrayList<Product> products) {
        final CartDiffUtils diffCallback = new CartDiffUtils(this.products, products);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.products.clear();
        this.products.addAll(products);
        diffResult.dispatchUpdatesTo(this);
    }
}
