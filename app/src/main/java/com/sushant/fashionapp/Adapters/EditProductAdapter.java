package com.sushant.fashionapp.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Seller.EditProductDetailsActivity;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.Utils.TextUtils;

import java.text.MessageFormat;
import java.util.ArrayList;

public class EditProductAdapter extends RecyclerView.Adapter<EditProductAdapter.viewHolder> {

    ArrayList<Product> list;
    Context context;

    public EditProductAdapter(ArrayList<Product> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_product, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Product product = list.get(position);
        Glide.with(context).load(product.getPreviewPic()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(holder.imgProduct);
        holder.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}</big>", product.getpPrice())));
        holder.txtBrand.setText(Html.fromHtml(MessageFormat.format("Brand: {0}", product.getBrandName())));
        holder.txtProductName.setText(TextUtils.captializeAllFirstLetter(product.getpName()));

        holder.btnEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditProductDetailsActivity.class);
                intent.putExtra("pId", product.getpId());
                intent.putExtra("origVariant", product.getVariants());
                context.startActivity(intent);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog(holder, product);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        private final MaterialButton btnEditProduct;
        private final MaterialButton btnDelete;
        private final ImageView imgProduct;
        private final TextView txtBrand;
        private final TextView txtProductName;
        private final TextView txtPrice;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            btnEditProduct = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtBrand = itemView.findViewById(R.id.txtBrand);
            txtProductName = itemView.findViewById(R.id.txtProdName);
            txtPrice = itemView.findViewById(R.id.txtPrice);

        }
    }

    private void confirmDialog(viewHolder holder, Product product) {
        if (CheckConnection.isOnline(context)) {
            new MaterialAlertDialogBuilder(context, R.style.RoundShapeTheme)
                    .setMessage("Are you sure?")
                    .setTitle("Confirmation")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ProgressDialog dialog = new ProgressDialog(context);
                            dialog.setTitle("Uploading..");
                            dialog.setMessage("Please wait while we are adding your product");
                            dialog.setCancelable(false);
                            dialog.show();

                            FirebaseDatabase.getInstance().getReference().child("Products").child(product.getpId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    list.remove(holder.getAbsoluteAdapterPosition());
                                    notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                                    dialog.dismiss();
                                }
                            });
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        } else {
            CheckConnection.showCustomDialog(context);
        }
    }
}
