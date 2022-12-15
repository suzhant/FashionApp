package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.R;

import java.text.MessageFormat;
import java.util.ArrayList;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.viewHolder> {

    ArrayList<Cart> products;
    Context context;

    public OrderItemsAdapter(ArrayList<Cart> products, Context context) {
        this.products = products;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_summary_product, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Cart product = products.get(position);
        holder.txtProductName.setText(product.getpName());
        Glide.with(context).load(product.getpPic()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).diskCacheStrategy(DiskCacheStrategy.ALL)
                .onlyRetrieveFromCache(true).into(holder.imgProduct);

        holder.txtSize.setText(MessageFormat.format("Size: {0}", product.getSize()));
        holder.txtQuantity.setText(MessageFormat.format("Qty: {0}", product.getQuantity()));
        holder.txtColor.setText(MessageFormat.format("Color: {0}", product.getColor()));
        if (product.getBargainPrice() != null) {
            holder.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. {0}", product.getBargainPrice())));
        } else {
            holder.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. {0}", product.getpPrice())));
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView txtProductName, txtSize, txtColor, txtPrice, txtQuantity;
        ImageView imgProduct;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            txtProductName = itemView.findViewById(R.id.txtProdName);
            txtSize = itemView.findViewById(R.id.txtSize);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }
}
