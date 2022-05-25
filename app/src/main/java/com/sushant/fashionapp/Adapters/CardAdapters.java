package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.sushant.fashionapp.ActivityProductDetails;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;

import java.text.MessageFormat;
import java.util.ArrayList;

public class CardAdapters extends RecyclerView.Adapter<CardAdapters.viewHolder> {

    ArrayList<Product> products;
    Context context;

    public CardAdapters(ArrayList<Product> products, Context context) {
        this.products = products;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Product product = products.get(position);
        Glide.with(context).load(product.getpPic()).placeholder(R.drawable.avatar).into(holder.productImg);
        holder.productName.setText(product.getpName());
        holder.productPrice.setText(MessageFormat.format("Rs {0}", product.getpPrice()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityProductDetails.class);
                intent.putExtra("pPic", product.getpPic());
                intent.putExtra("pName", product.getpName());
                intent.putExtra("pPrice", product.getpPrice());
                intent.putExtra("pId", product.getpId());
                context.startActivity(intent);
            }
        });

        holder.imgLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (product.getLove() == 0) {
                    holder.imgLove.setImageResource(R.drawable.ic_favorite_24);
                    product.setLove(1);
                } else {
                    holder.imgLove.setImageResource(R.drawable.ic_favorite_border_24);
                    product.setLove(0);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final ShapeableImageView productImg;
        private final TextView productName;
        private final TextView productPrice;
        private final ImageView imgLove;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            productImg = itemView.findViewById(R.id.imgProductPic);
            productName = itemView.findViewById(R.id.txtProductName);
            productPrice = itemView.findViewById(R.id.txtProductPrice);
            imgLove = itemView.findViewById(R.id.imgLove);
        }
    }

}
