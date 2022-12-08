package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.viewHolder> {

    ArrayList<Address> list;
    Context context;
    String extraInfo, label, landMark;
    private int mCheckedPostion = -1;

    public AddressAdapter(ArrayList<Address> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address_chip, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Address address = list.get(position);
        label = address.getLabel();
        if (label != null) {
            holder.txtLabel.setText(label);
        }
        extraInfo = address.getAddress();
        if (extraInfo == null) {
            extraInfo = "";
        } else {
            extraInfo = extraInfo + ",";
        }
        landMark = address.getLandmark();
        if (landMark == null) {
            landMark = "";
        }

        String fullAddress = address.getProvince() + "," + address.getCity().trim() + "," + address.getStreetAddress().trim() + "," + extraInfo + landMark;
        holder.txtAddress.setText(fullAddress);
        holder.txtPhone.setText(address.getMobile());

        if (holder.getAbsoluteAdapterPosition() == mCheckedPostion) {
            holder.imgCheck.setVisibility(View.VISIBLE);
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.skyBlue));
        } else {
            holder.imgCheck.setVisibility(View.GONE);
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.white));
        }


        holder.linearAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAbsoluteAdapterPosition() == mCheckedPostion) {
                    holder.imgCheck.setVisibility(View.GONE);
                    holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.white));
                    mCheckedPostion = -1;
                } else {
                    mCheckedPostion = holder.getAbsoluteAdapterPosition();
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView txtAddress, txtPhone, txtLabel;
        ImageView imgCheck;
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
        }
    }
}
