package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
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

    public CartAdapter(ArrayList<Product> products, Context context, ProductClickListener productClickListener) {
        this.products = products;
        this.context = context;
        this.productClickListener = productClickListener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_items_layout, parent, false);
        return new viewHolder(view);
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
                    case "newStoreName":
                        holder.txtStoreName.setText(bundle.getString("newStoreName"));
                        break;
                    case "newPrice":
                        holder.txtPrice.setText(MessageFormat.format("Rs {0}", bundle.getInt("newPrice")));
                        break;
                    case "newStock":
                        holder.txtStock.setText(MessageFormat.format("Stocks: {0}", bundle.getInt("newStock")));
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
        holder.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}</big>", product.getQuantity() * product.getpPrice())));
        holder.txtProductName.setText(TextUtils.captializeAllFirstLetter(product.getpName()));
        holder.txtStock.setText(MessageFormat.format("Stock: {0}", product.getStock()));
        holder.txtQuantity.setText(String.valueOf(product.getQuantity()));

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                productClickListener.onClick(product, b);
                if (b) {
                    holder.cardView.setStrokeWidth(5);
                    //    holder.cardView.setStrokeColor(ContextCompat.getColor(context,R.color.skyBlue));
                } else {
                    holder.cardView.setStrokeWidth(1);
                }
            }
        });


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

        holder.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (product.getStock() > product.getQuantity()) {
                    product.setQuantity(product.getQuantity() + 1);
                    updateCartQuantity(product);
                }

            }
        });

        holder.imgMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (product.getQuantity() > 1) {
                    product.setQuantity(product.getQuantity() - 1);
                    updateCartQuantity(product);
                }
            }
        });

        holder.imgProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityProductDetails.class);
                intent.putExtra("pPic", product.getpPic());
                intent.putExtra("pName", product.getpName());
                intent.putExtra("pPrice", product.getpPrice());
                intent.putExtra("pId", product.getpId());
                intent.putExtra("stock", product.getStock());
                intent.putExtra("sName", product.getStoreName());
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
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {

        private final CheckBox checkBox;
        private final ImageView imgProduct;
        private final TextView txtStoreName;
        private final TextView txtProductName;
        private final TextView txtPrice;
        private final ImageView imgMinus;
        private final ImageView imgPlus;
        private final TextView txtQuantity;
        private final TextView txtStock;
        private final MaterialCardView cardView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtProductName = itemView.findViewById(R.id.txtProdName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgMinus = itemView.findViewById(R.id.imgMinus);
            imgPlus = itemView.findViewById(R.id.imgPlus);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtStock = itemView.findViewById(R.id.txtStock);
            cardView = itemView.findViewById(R.id.cart_layout_parent);
        }
    }

    public void updateProductList(ArrayList<Product> products) {
        final CartDiffUtils diffCallback = new CartDiffUtils(this.products, products);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.products.clear();
        this.products.addAll(products);
        diffResult.dispatchUpdatesTo(this);
    }

    private void updateCartQuantity(Product product) {
        HashMap<String, Object> quantity = new HashMap<>();
        quantity.put("quantity", product.getQuantity());
        FirebaseDatabase.getInstance().getReference().child("Cart").child(FirebaseAuth.getInstance().getUid()).child(product.getpId())
                .updateChildren(quantity);
    }

}
