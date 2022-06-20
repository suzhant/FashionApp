package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sushant.fashionapp.Models.Category;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.viewHolder> {

    ArrayList<Category> categories;
    Context context;

    public CategoryAdapter(ArrayList<Category> categories, Context context) {
        this.categories = categories;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_items, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Category category = categories.get(position);
        Glide.with(context).load(category.getImage()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(holder.catimg);
        holder.catName.setText(category.getName());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final ImageView catimg;
        private final TextView catName;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            catimg = itemView.findViewById(R.id.imgCategory);
            catName = itemView.findViewById(R.id.txtCatName);

        }
    }
}
