package com.sushant.fashionapp.Adapters;

import static com.sushant.fashionapp.Utils.TextUtils.captializeAllFirstLetter;

import android.content.Context;
import android.os.Build;
import android.text.Html;
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

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BuyerBargainAdapter extends RecyclerView.Adapter<BuyerBargainAdapter.viewHolder> {

    ArrayList<Bargain> list;
    Context context;
    int color;
    boolean isExpanded = false;

    public BuyerBargainAdapter(ArrayList<Bargain> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bargain_history_layout, parent, false);
        return new viewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Bargain bargain = list.get(position);
        FirebaseDatabase.getInstance().getReference().child("Store").child(bargain.getStoreId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Store store = snapshot.getValue(Store.class);
                    assert store != null;
                    if (snapshot.child("storePic").exists()) {
                        String userPic = store.getStorePic();
                        Glide.with(context).load(userPic).placeholder(R.drawable.avatar).into(holder.imgStore);
                    }
                    String storeName = store.getStoreName();
                    holder.txtStoreName.setText(storeName);
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
                String productImage = snapshot.child("previewPic").getValue(String.class);
                holder.txtProductName.setText(productName);
                Glide.with(context).load(productImage).placeholder(R.drawable.avatar).into(holder.imgProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bargain.getTimestamp()), ZoneId.systemDefault());
        holder.txtBargainDate.setText(Html.fromHtml(MessageFormat.format("Bargain Date:&nbsp;{0}", dateTime.format(formatter))));
        holder.txtOrigPrice.setText(Html.fromHtml(MessageFormat.format("Original Price: &nbsp;Rs. {0}", bargain.getOriginalPrice())));
        holder.txtBargainPrice.setText(Html.fromHtml(MessageFormat.format("Bargain Price: &nbsp;Rs. {0}", bargain.getBargainPrice())));
        switch (bargain.getStatus()) {
            case "pending":
                color = ContextCompat.getColor(context, R.color.yello_orange);
                holder.linearLayout.setVisibility(View.VISIBLE);
                break;
            case "accepted":
                color = ContextCompat.getColor(context, R.color.pistachio);
                holder.linearLayout.setVisibility(View.GONE);
                break;
            case "rejected":
                color = ContextCompat.getColor(context, R.color.red);
                holder.linearLayout.setVisibility(View.VISIBLE);
                break;
        }
        holder.txtStatus.setText(Html.fromHtml(MessageFormat.format("Status:&nbsp; <b><span style=color:" + color + ">{0}</span></b>", captializeAllFirstLetter(bargain.getStatus()))));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct, imgDrop;
        TextView txtProductName, txtOrigPrice, txtBargainPrice, txtBargainDate, txtStatus;
        CircleImageView imgStore;
        TextView txtStoreName, txtAvailability;
        MaterialButton btnNegotiate, btnCancel;
        LinearLayout childLayout, mainLayout, linearLayout;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProdName);
            txtOrigPrice = itemView.findViewById(R.id.txtOrigPrice);
            txtBargainPrice = itemView.findViewById(R.id.txtBargainPrice);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtBargainDate = itemView.findViewById(R.id.txtBargainDate);
            txtAvailability = itemView.findViewById(R.id.txtAvailability);
            imgStore = itemView.findViewById(R.id.imgStore);
            btnNegotiate = itemView.findViewById(R.id.btnNegotiate);
            btnCancel = itemView.findViewById(R.id.btnCancelRequest);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            childLayout = itemView.findViewById(R.id.childLayout);
            imgDrop = itemView.findViewById(R.id.imgDrop);

            mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isExpanded = !isExpanded;
                    if (isExpanded) {
                        childLayout.setVisibility(View.VISIBLE);
                        imgDrop.setImageResource(com.hbb20.R.drawable.ccp_ic_arrow_drop_down);
                    } else {
                        childLayout.setVisibility(View.GONE);
                        imgDrop.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                    }
                    //  notifyItemChanged(getAbsoluteAdapterPosition());
                }
            });
        }
    }
}
