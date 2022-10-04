package com.sushant.fashionapp.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class VariantSummaryAdapter extends RecyclerView.Adapter<VariantSummaryAdapter.viewHolder> {

    ArrayList<Product> products;
    Context context;
    VariantPhotoAdapter adapter;
    SizeSummaryAdapter sizeSummaryAdapter;
    int i;

    public VariantSummaryAdapter(ArrayList<Product> products, Context context) {
        this.products = products;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.variant_summary_layout, parent, false);
        return new viewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Product variant = products.get(position);
        //   holder.txtStock.setText(String.format("Stock: %s", variant.getStock().toString()));
        holder.txtColorName.setText(String.format("Color: %s", variant.getColor()));
        //   holder.txtSize.setText(String.format("Sizes: %s",chips.toString()));
        if (variant.getPhotos() != null) {
            adapter = new VariantPhotoAdapter(variant.getPhotos(), context, 2);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context.getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);
            holder.recyclerView.setAdapter(adapter);
        }
        if (variant.getSizes() != null) {
            sizeSummaryAdapter = new SizeSummaryAdapter(variant.getSizes(), context, 2);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context.getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerViewSizes.setLayoutManager(layoutManager);
            holder.recyclerViewSizes.setAdapter(sizeSummaryAdapter);
        }
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog dialog = new ProgressDialog(context);
                dialog.setTitle("Delete");
                dialog.setMessage("Please wait..");
                dialog.setCancelable(false);

                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                dialog.show();
                i = 0;
                for (String product : variant.getPhotos()) {
                    i++;
                    StorageReference storageReference = firebaseStorage.getReferenceFromUrl(product);
                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (i == variant.getPhotos().size()) {
                                dialog.dismiss();
                            }
                        }
                    });
                }

                products.remove(holder.getAbsoluteAdapterPosition());
                notifyItemRemoved(holder.getAbsoluteAdapterPosition());
            }
        });

        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final TextView txtColorName;
        //        private final TextView txtSize;
//        private final TextView txtStock;
        private final RecyclerView recyclerView;
        private final RecyclerView recyclerViewSizes;
        private final ImageView imgDelete, imgEdit;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            txtColorName = itemView.findViewById(R.id.txtColorName);
//            txtSize=itemView.findViewById(R.id.txtSize);
//            txtStock=itemView.findViewById(R.id.txtStock);
            recyclerView = itemView.findViewById(R.id.recyclerImages);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            recyclerViewSizes = itemView.findViewById(R.id.recyclerSizes);
        }
    }
}
