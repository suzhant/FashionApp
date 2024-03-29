package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.sushant.fashionapp.Inteface.VariantClickListener;
import com.sushant.fashionapp.Models.Variants;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.viewHolder> {

    ArrayList<Variants> products;
    Context context;
    VariantClickListener productClickListener;
    int selectedItemPos;

    public VariantAdapter(ArrayList<Variants> products, Context context, VariantClickListener productClickListener, int selectedItemPos) {
        this.products = products;
        this.context = context;
        this.productClickListener = productClickListener;
        this.selectedItemPos = selectedItemPos;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.color_variant_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Variants product = products.get(position);
        if (product.getPhotos() != null) {
            Glide.with(context).load(product.getPhotos().get(0)).placeholder(com.denzcoskun.imageslider.R.drawable.loading).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.imgVariant);
        }
        holder.txtColor.setText(product.getColor());
        if (holder.getAbsoluteAdapterPosition() == selectedItemPos) {
            holder.cardVariant.setStrokeColor(ContextCompat.getColorStateList(context, R.color.black));
            productClickListener.onClick(product, holder.getAbsoluteAdapterPosition());
        } else {
            holder.cardVariant.setStrokeColor(ContextCompat.getColorStateList(context, R.color.medium_gray));
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
        private final TextView txtColor;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgVariant = itemView.findViewById(R.id.imgVariant);
            cardVariant = itemView.findViewById(R.id.cardVariant);
            txtColor = itemView.findViewById(R.id.txtColor);
        }
    }
}
