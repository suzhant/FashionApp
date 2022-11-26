package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sushant.fashionapp.Models.Size;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class SizeSummaryAdapter extends RecyclerView.Adapter<SizeSummaryAdapter.viewHolder> {

    ArrayList<Size> sizes;
    Context context;
    int type;

    public SizeSummaryAdapter(ArrayList<Size> sizes, Context context, int type) {
        this.sizes = sizes;
        this.context = context;
        this.type = type;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.size_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Size product = sizes.get(position);
        holder.txtStock.setText(String.format("Stock: %s", product.getStock().toString()));
        holder.txtSize.setText(String.format("Size: %s", product.getSize()));

        if (type == 1) {
            holder.imgDelete.setVisibility(View.VISIBLE);
        } else {
            holder.imgDelete.setVisibility(View.GONE);
        }
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sizes.remove(holder.getAbsoluteAdapterPosition());
                notifyItemRemoved(holder.getAbsoluteAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return sizes.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final TextView txtSize;
        private final TextView txtStock;
        private final ImageView imgDelete;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            txtSize = itemView.findViewById(R.id.txtSize);
            txtStock = itemView.findViewById(R.id.txtStock);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}
