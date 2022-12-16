package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.viewHolder> {

    ArrayList<Store> stores;
    Context context;
    ItemClickListener itemClickListener;

    public ShopAdapter(ArrayList<Store> stores, Context context, ItemClickListener itemClickListener) {
        this.stores = stores;
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_self_pick_shop_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Store store = stores.get(position);
        Glide.with(context).load(store.getStorePic()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(holder.imgStore);
        holder.txtStoreName.setText(store.getStoreName());
        holder.txtAddress.setText(store.getStoreAddress());

        holder.switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                itemClickListener.onAddressClick(store, b);
            }
        });

    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgStore;
        TextView txtStoreName, txtAddress;
        SwitchMaterial switchMaterial;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgStore = itemView.findViewById(R.id.imgShop);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtAddress = itemView.findViewById(R.id.txtLocation);
            switchMaterial = itemView.findViewById(R.id.switch_pickUp);
        }
    }
}
