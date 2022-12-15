package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.Buyer.EditAddressActivity;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.viewHolder> {

    ArrayList<Address> list;
    ArrayList<String> listAddress;
    Context context;
    String extraInfo, label, landMark;
    private int mCheckedPostion = -1;
    ItemClickListener itemClickListener;

    public AddressAdapter(ArrayList<Address> list, Context context, ItemClickListener itemClickListener) {
        this.list = list;
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address_chip, parent, false);
        return new viewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Address address = list.get(position);
        label = address.getLabel();
        if (label != null) {
            holder.txtLabel.setText(label);
        }
        extraInfo = address.getAddress();
        landMark = address.getLandmark();

        listAddress = new ArrayList<>();
        listAddress.add(address.getProvince());
        listAddress.add(address.getCity());
        listAddress.add(address.getStreetAddress());
        if (extraInfo != null) {
            listAddress.add(extraInfo);
        }
        if (landMark != null) {
            listAddress.add(landMark);
        }

        String fullAddress = String.join(", ", listAddress);
        holder.txtAddress.setText(fullAddress);
        holder.txtPhone.setText(address.getMobile());

        if (holder.getAbsoluteAdapterPosition() == mCheckedPostion) {
            holder.imgCheck.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.skyBlue)));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.skyBlue));
        } else {
            holder.imgCheck.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, com.khalti.R.color.material_on_background_disabled)));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.white));
        }



        holder.linearAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAbsoluteAdapterPosition() == mCheckedPostion) {
                    holder.imgCheck.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, com.khalti.R.color.material_on_background_disabled)));
                    holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.white));
                    mCheckedPostion = -1;
                    itemClickListener.onAddressClick(address, false);
                } else {
                    mCheckedPostion = holder.getAbsoluteAdapterPosition();
                    itemClickListener.onAddressClick(address, true);
                    notifyDataSetChanged();
                }
            }
        });

        if (address.getLabel() == null) {
            holder.txtLabel.setText("UnLabeled");
        }

        holder.txtName.setText(address.getName());
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(holder, address.getAddressId());
            }
        });

        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditAddressActivity.class);
                intent.putExtra("addressId", address.getAddressId());
                context.startActivity(intent);
            }
        });

        if (address.getDefault()) {
            holder.txtDefault.setVisibility(View.VISIBLE);
        }

    }

    private void showDialog(viewHolder holder, String addressId) {
        new MaterialAlertDialogBuilder(context, R.style.RoundShapeTheme)
                .setMessage("Are you sure?")
                .setTitle("Delete Address")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        list.remove(holder.getAbsoluteAdapterPosition());
                        notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Shipping Address");
                        reference.child(addressId).removeValue();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView txtAddress, txtPhone, txtLabel, txtName, txtDefault;
        ImageView imgCheck, imgDelete, imgEdit;
        MaterialCardView cardView;
        LinearLayout linearAddress;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtLabel = itemView.findViewById(R.id.txtLabel);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            imgCheck = itemView.findViewById(R.id.imgCheck);
            cardView = itemView.findViewById(R.id.parent);
            linearAddress = itemView.findViewById(R.id.linear_address);
            txtName = itemView.findViewById(R.id.txtName);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            txtDefault = itemView.findViewById(R.id.txtDefault);
            imgEdit = itemView.findViewById(R.id.imgEdit);
        }
    }
}
