package com.sushant.fashionapp.Adapters;


import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Buyer;
import com.sushant.fashionapp.R;

import java.text.MessageFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BargainUserAdapter extends RecyclerView.Adapter<BargainUserAdapter.viewHolder> {

    ArrayList<Bargain> list;
    Context context;

    public BargainUserAdapter(ArrayList<Bargain> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Bargain bargain = list.get(position);
        FirebaseDatabase.getInstance().getReference().child("Users").child(bargain.getBuyerId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("userPic").exists()) {
                        Buyer buyer = snapshot.getValue(Buyer.class);
                        assert buyer != null;
                        String userPic = buyer.getUserPic();
                        String userName = buyer.getUserName();
                        Glide.with(context).load(userPic).placeholder(R.drawable.avatar).into(holder.imgUser);
                        holder.txtUserName.setText(userName);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("Products").child(bargain.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String productName = snapshot.child("pName").getValue(String.class);
                holder.txtProductName.setText(productName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.txtOriginalPrice.setText(Html.fromHtml(MessageFormat.format("Your Price: &nbsp; <big> Rs.{0}</big>", bargain.getOriginalPrice().toString())));
        holder.txtBargainPrice.setText(Html.fromHtml(MessageFormat.format("Bargain Price: &nbsp; <big> Rs.{0}</big>", bargain.getBargainPrice().toString())));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgUser;
        TextView txtUserName, txtProductName, txtOriginalPrice, txtBargainPrice;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.imgProfile);
            txtUserName = itemView.findViewById(R.id.txtName);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtOriginalPrice = itemView.findViewById(R.id.txtOrigPrice);
            txtBargainPrice = itemView.findViewById(R.id.txtBargainPrice);
        }
    }
}