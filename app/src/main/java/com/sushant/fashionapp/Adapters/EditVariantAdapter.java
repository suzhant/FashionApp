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
import com.google.android.material.button.MaterialButton;
import com.sushant.fashionapp.Inteface.VariantClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.seller.EditVariantActivity;

import java.util.ArrayList;

public class EditVariantAdapter extends RecyclerView.Adapter<EditVariantAdapter.viewHolder> {

    ArrayList<Product> list;
    Context context;
    String pId;
    VariantClickListener productClickListener;

    public EditVariantAdapter(ArrayList<Product> list, Context context, String pId, VariantClickListener productClickListener) {
        this.list = list;
        this.context = context;
        this.pId = pId;
        this.productClickListener = productClickListener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_variant_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Product product = list.get(position);
        Glide.with(context).load(product.getPhotos().get(0)).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(holder.imgProduct);
        holder.txtColor.setText(product.getColor());

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pId != null) {
                    Intent intent = new Intent(context, EditVariantActivity.class);
                    intent.putExtra("pId", pId);
                    intent.putExtra("variantIndex", holder.getAbsoluteAdapterPosition());
                    intent.putExtra("color", product.getColor());
                    intent.putExtra("photos", product.getPhotos());
                    intent.putExtra("sizes", product.getSizes());
                    intent.putExtra("origVariant", list);
                    context.startActivity(intent);
                } else {
                    if (productClickListener != null) {
                        productClickListener.onClick(product, holder.getAbsoluteAdapterPosition());
                    }
                }

            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(holder.getAbsoluteAdapterPosition());
                notifyItemRemoved(holder.getAbsoluteAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtColor;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtColor = itemView.findViewById(R.id.txtColor);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
        }
    }
}
