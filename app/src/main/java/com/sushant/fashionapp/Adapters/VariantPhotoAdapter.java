package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class VariantPhotoAdapter extends RecyclerView.Adapter<VariantPhotoAdapter.viewHolder> {

    ArrayList<String> products;
    Context context;
    int type;

    public VariantPhotoAdapter(ArrayList<String> products, Context context, int type) {
        this.products = products;
        this.context = context;
        this.type = type;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        String image = products.get(position);
        Glide.with(context).load(image).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(holder.imgVariant);

        if (type == 1) {
            holder.imgDelete.setVisibility(View.VISIBLE);
        } else {
            holder.imgDelete.setVisibility(View.GONE);
        }
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                products.remove(holder.getAbsoluteAdapterPosition());
                notifyItemRemoved(holder.getAbsoluteAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {

        private final ShapeableImageView imgVariant;
        private final ImageView imgDelete;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgVariant = itemView.findViewById(R.id.imgColor);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}
