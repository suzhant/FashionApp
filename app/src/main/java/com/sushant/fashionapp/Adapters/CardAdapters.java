package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Buyer.ActivityProductDetails;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Rating;
import com.sushant.fashionapp.R;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;

public class CardAdapters extends RecyclerView.Adapter<CardAdapters.viewHolder> {

    ArrayList<Product> products;
    Context context;

    public CardAdapters(ArrayList<Product> products, Context context) {
        this.products = products;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Product product = products.get(position);
        Glide.with(context).load(product.getPreviewPic()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(holder.productImg);
        holder.productName.setText(product.getpName());
        holder.productPrice.setText(MessageFormat.format("Rs. {0}", product.getpPrice()));


        //    holder.ratingBar.setOnClickListener(null);
        FirebaseDatabase.getInstance().getReference().child("Ratings").child(product.getpId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = 0;
                    float sum = 0;
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Rating rating = snapshot1.getValue(Rating.class);
                        sum += rating.getRating();
                        count++;
                    }
                    float finalRating = sum / count;
                    float number = BigDecimal.valueOf(finalRating)
                            .setScale(1, BigDecimal.ROUND_HALF_UP)
                            .floatValue();
                    holder.txtNoOfRating.setText(MessageFormat.format("({0})", count));
                    // holder.ratingBar.setRating(finalRating);
                    holder.txtRating.setText(MessageFormat.format("{0}/5", number));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(context, ActivityProductDetails.class);
//                intent.putExtra("pPic", product.getPreviewPic());
//                intent.putExtra("pName", product.getpName());
//                intent.putExtra("pPrice", product.getpPrice());
//                intent.putExtra("pId", product.getpId());
//                intent.putExtra("pDesc", product.getDesc());
//                intent.putExtra("storeId", product.getStoreId());
//                intent.putExtra("index", product.getVariantIndex());
//                context.startActivity(intent);
//            }
//        });
        
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityProductDetails.class);
                intent.putExtra("pPic", product.getPreviewPic());
                intent.putExtra("pName", product.getpName());
                intent.putExtra("pPrice", product.getpPrice());
                intent.putExtra("pId", product.getpId());
                intent.putExtra("pDesc", product.getDesc());
                intent.putExtra("storeId", product.getStoreId());
                intent.putExtra("index", product.getVariantIndex());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final ShapeableImageView productImg;
        private final TextView productName;
        private final TextView productPrice;
        private final TextView txtNoOfRating;
        // private final SimpleRatingBar ratingBar;
        private final MaterialCardView cardView;
        private final TextView txtRating;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            productImg = itemView.findViewById(R.id.imgProductPic);
            productName = itemView.findViewById(R.id.txtProductName);
            productPrice = itemView.findViewById(R.id.txtProductPrice);
            //  ratingBar = itemView.findViewById(R.id.ratingBar);
            txtNoOfRating = itemView.findViewById(R.id.txtNumberOfRating);
            cardView = itemView.findViewById(R.id.cardLyt);
            txtRating = itemView.findViewById(R.id.txtRating);
        }
    }

}
