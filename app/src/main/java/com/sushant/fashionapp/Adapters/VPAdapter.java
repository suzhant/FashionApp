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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sushant.fashionapp.ImageActivity;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class VPAdapter extends RecyclerView.Adapter<VPAdapter.viewHolder> {

    ArrayList<String> list;
    Context context;
    Boolean touchable;

    public VPAdapter(ArrayList<String> list, Context context, Boolean touchable) {
        this.list = list;
        this.context = context;
        this.touchable = touchable;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_viewpager_pics, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        String pics = list.get(position);
        Glide.with(context).load(pics).placeholder(com.denzcoskun.imageslider.R.drawable.loading).diskCacheStrategy(DiskCacheStrategy.ALL)
                .onlyRetrieveFromCache(true).into(holder.imgProduct);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (touchable) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putStringArrayListExtra("images", list);
                    intent.putExtra("position", holder.getAbsoluteAdapterPosition());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtProduct;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            //   txtProduct=itemView.findViewById(R.id.txtPic);
        }
    }
}
