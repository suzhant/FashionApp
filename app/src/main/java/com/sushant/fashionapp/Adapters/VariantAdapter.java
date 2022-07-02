package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.sushant.fashionapp.Inteface.VariantClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.viewHolder> {

    ArrayList<Product> products;
    Context context;
    VariantClickListener productClickListener;
    int selectedItemPos = 0;

    public VariantAdapter(ArrayList<Product> products, Context context) {
        this.products = products;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.color_variant_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Product product = products.get(position);
        Glide.with(context).load(product.getpPic()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(holder.imgVariant);
        if (holder.getAbsoluteAdapterPosition() == selectedItemPos) {
            holder.cardVariant.setStrokeColor(ContextCompat.getColorStateList(context, R.color.black));
            //     productClickListener.onClick(products.get(position),true);
        } else {
            holder.cardVariant.setStrokeColor(ContextCompat.getColorStateList(context, R.color.medium_gray));
            //     productClickListener.onClick(products.get(position),false);
        }
        holder.cardVariant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedItemPos = holder.getAbsoluteAdapterPosition();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgVariant;
        private final MaterialCardView cardVariant;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgVariant = itemView.findViewById(R.id.imgVariant);
            cardVariant = itemView.findViewById(R.id.cardVariant);
        }
    }
}
