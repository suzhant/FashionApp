package com.sushant.fashionapp.Adapters;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Buyer;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Seller.MessageActivity;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BargainUserAdapter extends RecyclerView.Adapter<BargainUserAdapter.viewHolder> {

    ArrayList<Bargain> list;
    Context context;
    MessageActivity activity;

    public BargainUserAdapter(ArrayList<Bargain> list, Context context) {
        this.list = list;
        this.context = context;
        this.activity = (MessageActivity) context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seller_bargain_layout, parent, false);
        return new viewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Bargain bargain = list.get(position);
        FirebaseDatabase.getInstance().getReference().child("Users").child(bargain.getBuyerId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Buyer buyer = snapshot.getValue(Buyer.class);
                    assert buyer != null;
                    if (snapshot.child("userPic").exists()) {
                        String userPic = buyer.getUserPic();
                        Glide.with(context).load(userPic).placeholder(R.drawable.avatar).into(holder.imgUser);
                    }
                    String userName = buyer.getUserName();
                    holder.txtUserName.setText(userName);
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
                Glide.with(context).load(productImage).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL).onlyRetrieveFromCache(true).into(holder.imgProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.txtOriginalPrice.setText(Html.fromHtml(MessageFormat.format("Original Price: <b> Rs.{0}</b>", bargain.getOriginalPrice().toString())));
        holder.txtBargainPrice.setText(Html.fromHtml(MessageFormat.format("Bargain Price: <b> Rs.{0}</b>", bargain.getBargainPrice().toString())));
        if (bargain.getSellerPrice() != null) {
            holder.txtSellerPrice.setVisibility(View.VISIBLE);
            holder.txtSellerPrice.setText(Html.fromHtml(MessageFormat.format("Your Price: <b> Rs.{0}</b>", bargain.getSellerPrice().toString())));
        }

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("status", "accepted");
                map.put("timestamp", new Date().getTime());
                FirebaseDatabase.getInstance().getReference().child("Bargain").child(bargain.getBargainId()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Snackbar.make(activity.findViewById(R.id.parent), "Approved", Snackbar.LENGTH_SHORT).show();
                        Query query = FirebaseDatabase.getInstance().getReference().child("Cart").child(FirebaseAuth.getInstance().getUid())
                                .orderByChild("pId").equalTo(bargain.getProductId());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        Cart cart = snapshot1.getValue(Cart.class);
                                        HashMap<String, Object> map1 = new HashMap<>();
                                        map1.put("bargainPrice", bargain.getBargainPrice());
                                        FirebaseDatabase.getInstance().getReference().child("Cart").child(FirebaseAuth.getInstance().getUid())
                                                .child(cart.getVariantPId()).updateChildren(map1);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(activity.findViewById(R.id.parent), "Operation Failed !!", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        holder.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("status", "rejected");
                map.put("timestamp", new Date().getTime());
                FirebaseDatabase.getInstance().getReference().child("Bargain").child(bargain.getBargainId()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Snackbar.make(activity.findViewById(R.id.parent), "Rejected", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });

        holder.btnNegotiate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                bottomSheetDialog.setContentView(R.layout.bottomsheet_seller_negotiate);
                MaterialButton btnChangePrice = bottomSheetDialog.findViewById(R.id.btnChangePrice);
                ImageView imgClose = bottomSheetDialog.findViewById(R.id.imgClose);
                EditText edPrice = bottomSheetDialog.findViewById(R.id.edPrice);
                LinearLayout parent = bottomSheetDialog.findViewById(R.id.bargainParent);
                Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                assert imgClose != null;
                imgClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });

                assert btnChangePrice != null;
                btnChangePrice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String price = edPrice.getText().toString();
                        assert parent != null;
                        if (price.isEmpty()) {
                            Snackbar.make(parent, "Empty Field", Snackbar.LENGTH_SHORT).show();
                            edPrice.requestFocus();
                            return;
                        }
                        if (Integer.parseInt(price) > bargain.getOriginalPrice()) {
                            edPrice.requestFocus();
                            Snackbar.make(parent, "You cannot enter price higher than the original price", Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        ProgressDialog dialog = new ProgressDialog(context);
                        dialog.setMessage("Please wait..");
                        dialog.setCancelable(false);
                        dialog.show();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("sellerPrice", Integer.valueOf(price));
                        map.put("timestamp", new Date().getTime());
                        map.put("status", "pending");
                        map.put("countered", true);
                        FirebaseDatabase.getInstance().getReference().child("Bargain").child(bargain.getBargainId()).updateChildren(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialog.dismiss();
                                        Snackbar.make(activity.findViewById(R.id.parent), "Counter price sent successfully", Snackbar.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
                                    }
                                });
                    }
                });


                bottomSheetDialog.show();
            }
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bargain.getTimestamp()), ZoneId.systemDefault());
        holder.txtTry.setText(MessageFormat.format("Remaining tries : {0}", bargain.getNoOfTries()));
        holder.txtBargainDate.setText(MessageFormat.format("Date : {0}", dateTime.format(formatter)));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgUser;
        TextView txtUserName, txtProductName, txtOriginalPrice, txtBargainPrice, txtTry, txtBargainDate, txtSellerPrice;
        ImageView imgProduct;
        MaterialButton btnAccept, btnReject, btnNegotiate;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtOriginalPrice = itemView.findViewById(R.id.txtOrigPrice);
            txtBargainPrice = itemView.findViewById(R.id.txtBargainPrice);
            imgUser = itemView.findViewById(R.id.imgProfile);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnNegotiate = itemView.findViewById(R.id.btnNegotiate);
            txtTry = itemView.findViewById(R.id.txtTry);
            txtBargainDate = itemView.findViewById(R.id.txtBargainDate);
            txtSellerPrice = itemView.findViewById(R.id.txtSellerPrice);
        }
    }
}
