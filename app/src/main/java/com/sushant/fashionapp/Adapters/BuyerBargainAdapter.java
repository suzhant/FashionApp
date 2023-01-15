package com.sushant.fashionapp.Adapters;

import static com.sushant.fashionapp.Utils.TextUtils.captializeAllFirstLetter;

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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.sushant.fashionapp.Buyer.BargainHistoryActivity;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;

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

public class BuyerBargainAdapter extends RecyclerView.Adapter<BuyerBargainAdapter.viewHolder> {

    ArrayList<Bargain> list;
    Context context;
    int color;
    BargainHistoryActivity activity;

    public BuyerBargainAdapter(ArrayList<Bargain> list, Context context) {
        this.list = list;
        this.context = context;
        this.activity = (BargainHistoryActivity) context;
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bargain.getTimestamp()), ZoneId.systemDefault());
        holder.txtBargainDate.setText(Html.fromHtml(MessageFormat.format("Date:&nbsp;{0}", dateTime.format(formatter))));
//        holder.txtOrigPrice.setText(Html.fromHtml(MessageFormat.format("Original Price: &nbsp;Rs. {0}", bargain.getOriginalPrice())));
//        holder.txtBargainPrice.setText(Html.fromHtml(MessageFormat.format("Bargain Price: &nbsp;Rs. {0}", bargain.getBargainPrice())));
//        if (bargain.getSellerPrice() != null) {
//            holder.txtOfferedPrice.setVisibility(View.VISIBLE);
//            holder.txtOfferedPrice.setText(Html.fromHtml(MessageFormat.format("Offered Price: &nbsp;Rs. {0}", bargain.getSellerPrice())));
//        }

        switch (bargain.getStatus()) {
            case "pending":
                color = ContextCompat.getColor(context, R.color.yello_orange);
                break;
            case "accepted":
                color = ContextCompat.getColor(context, R.color.pistachio);
                break;
            case "rejected":
            case "cancelled":
                color = ContextCompat.getColor(context, R.color.red);
                break;
            case "countered":
                color = ContextCompat.getColor(context, R.color.royal_blue);
                break;
        }

        holder.txtStatus.setText(Html.fromHtml(MessageFormat.format("Status:&nbsp; <b><span style=color:" + color + ">{0}</span></b>",
                captializeAllFirstLetter(bargain.getStatus()))));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (bargain.getStatus()) {
                    case "pending":
                    case "rejected":
                    case "cancelled":
                    case "countered":
                        openNegotiateButton(bargain);
                        break;
                    case "accepted":
                        openApprovedDialog(bargain);
                        break;
                }

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void openApprovedDialog(Bargain bargain) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_bargain_accepted);
        TextView txtOriginalPrice = bottomSheetDialog.findViewById(R.id.txtOrigPrice);
        TextView txtBargainPrice = bottomSheetDialog.findViewById(R.id.txtBargainPrice);
        TextView txtBargainDate = bottomSheetDialog.findViewById(R.id.txtBargainDate);
        TextView txtValidity = bottomSheetDialog.findViewById(R.id.txtValidity);
        TextView txtStatus = bottomSheetDialog.findViewById(R.id.txtStatus);
        ImageView imgClose = bottomSheetDialog.findViewById(R.id.imgClose);
        LinearLayout linearLayout = bottomSheetDialog.findViewById(R.id.linear_store);
        TextView txtRequest = bottomSheetDialog.findViewById(R.id.txtRequest_to);
        CircleImageView imgStore = bottomSheetDialog.findViewById(R.id.imgStore);
        TextView txtStoreName = bottomSheetDialog.findViewById(R.id.txtStoreName);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        linearLayout.setVisibility(View.VISIBLE);
        txtRequest.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("Store").child(bargain.getStoreId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Store store = snapshot.getValue(Store.class);
                    assert store != null;
                    if (snapshot.child("storePic").exists()) {
                        String userPic = store.getStorePic();
                        Glide.with(context).load(userPic).placeholder(R.drawable.avatar).into(imgStore);
                    }
                    String storeName = store.getStoreName();
                    txtStoreName.setText(storeName);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bargain.getTimestamp()), ZoneId.systemDefault());
        LocalDateTime afterDate = dateTime.plusDays(2);
        // long longDate = (timestamp + TimeUnit.DAYS.toMillis(2));
        txtBargainDate.setText(Html.fromHtml(MessageFormat.format("Approved Date:&nbsp; {0}", dateTime.format(formatter))));
        txtValidity.setText(Html.fromHtml(MessageFormat.format("Validity:&nbsp; {0}", afterDate.format(formatter))));
        txtOriginalPrice.setText(Html.fromHtml(MessageFormat.format("Seller Price:&nbsp; <b>Rs. {0}</b>", bargain.getOriginalPrice())));
        txtBargainPrice.setText(Html.fromHtml(MessageFormat.format("Bargain Price:&nbsp; <b>Rs. {0}</b>", bargain.getBargainPrice())));
        txtStatus.setText(Html.fromHtml(MessageFormat.format("Status:&nbsp; <b><span style=color:#09AEA3>{0}</span></b>", captializeAllFirstLetter(bargain.getStatus()))));

        assert imgClose != null;
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });


        bottomSheetDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void openNegotiateButton(Bargain bargain) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_bargain_cancel);
        MaterialButton btnCancelRequest = bottomSheetDialog.findViewById(R.id.btnCancelRequest);
        MaterialButton btnChangePrice = bottomSheetDialog.findViewById(R.id.btnChangePrice);
        MaterialButton btnAccept = bottomSheetDialog.findViewById(R.id.btnAccept);
        TextView txtOriginalPrice = bottomSheetDialog.findViewById(R.id.txtOrigPrice);
        TextView txtCounterPrice = bottomSheetDialog.findViewById(R.id.txtCounterPrice);
        TextView txtBargainPrice = bottomSheetDialog.findViewById(R.id.txtBargainPrice);
        TextView txtBargainDate = bottomSheetDialog.findViewById(R.id.txtBargainDate);
        TextView txtRemainingTries = bottomSheetDialog.findViewById(R.id.txtRemainingTries);
        TextView txtMessage = bottomSheetDialog.findViewById(R.id.txtMessage);
        ImageView imgClose = bottomSheetDialog.findViewById(R.id.imgClose);
        EditText edPrice = bottomSheetDialog.findViewById(R.id.edPrice);
        LinearLayout parent = bottomSheetDialog.findViewById(R.id.bargainParent);
        LinearLayout linearOffer = bottomSheetDialog.findViewById(R.id.linearOffer);
        ImageView imgProduct = bottomSheetDialog.findViewById(R.id.imgProduct);
        LinearLayout linearLayout = bottomSheetDialog.findViewById(R.id.linear_store);
        TextView txtRequest = bottomSheetDialog.findViewById(R.id.txtRequest_to);
        CircleImageView imgStore = bottomSheetDialog.findViewById(R.id.imgStore);
        TextView txtStoreName = bottomSheetDialog.findViewById(R.id.txtStoreName);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        linearLayout.setVisibility(View.VISIBLE);
        txtRequest.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("Store").child(bargain.getStoreId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Store store = snapshot.getValue(Store.class);
                    assert store != null;
                    if (snapshot.child("storePic").exists()) {
                        String userPic = store.getStorePic();
                        Glide.with(context).load(userPic).placeholder(R.drawable.avatar).into(imgStore);
                    }
                    String storeName = store.getStoreName();
                    txtStoreName.setText(storeName);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd hh:mm a");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bargain.getTimestamp()), ZoneId.systemDefault());
        txtBargainDate.setText(Html.fromHtml(MessageFormat.format("Bargain Date:&nbsp;  {0}", dateTime.format(formatter))));
        txtOriginalPrice.setText(Html.fromHtml(MessageFormat.format("Original Price: &nbsp; <b> Rs.{0}</b>", bargain.getOriginalPrice())));
        txtBargainPrice.setText(Html.fromHtml(MessageFormat.format("Bargain Price: &nbsp; <b>Rs.{0}</b>", bargain.getBargainPrice())));
        txtRemainingTries.setText(MessageFormat.format("Remaining Tries: {0}", bargain.getNoOfTries()));

        FirebaseDatabase.getInstance().getReference().child("Products").child(bargain.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                Glide.with(context).load(product.getPreviewPic()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(imgProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if (bargain.getCountered() != null && bargain.getCountered()) {
            linearOffer.setVisibility(View.VISIBLE);
        }
        if (bargain.getStatus().equals("rejected")) {
            txtMessage.setVisibility(View.VISIBLE);
        }
        if (bargain.getSellerPrice() != null) {
            txtCounterPrice.setVisibility(View.VISIBLE);
            txtCounterPrice.setText(Html.fromHtml(MessageFormat.format("Offered Price: &nbsp; <big> Rs.{0}</big>", bargain.getSellerPrice())));
        }
        if (bargain.getStatus().equals("cancelled")) {
            btnCancelRequest.setVisibility(View.GONE);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait..");
        dialog.setCancelable(false);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                HashMap<String, Object> map = new HashMap<>();
                map.put("status", "accepted");
                map.put("timestamp", new Date().getTime());
                map.put("sellerPrice", null);
                map.put("bargainPrice", bargain.getSellerPrice());
                database.getReference().child("Bargain").child(bargain.getBargainId()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        bottomSheetDialog.dismiss();
                        Snackbar.make(activity.findViewById(R.id.parent), "You can buy this product at Rs. " + bargain.getSellerPrice(), Snackbar.LENGTH_SHORT).show();
                        Query query = database.getReference().child("Cart").child(auth.getUid()).orderByChild("pId").equalTo(bargain.getProductId());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        Cart cart = snapshot1.getValue(Cart.class);
                                        HashMap<String, Object> map1 = new HashMap<>();
                                        map1.put("bargainPrice", bargain.getSellerPrice());
                                        database.getReference().child("Cart").child(auth.getUid())
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

        assert imgClose != null;
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        assert btnCancelRequest != null;
        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("status", "cancelled");
                database.getReference().child("Bargain").child(bargain.getBargainId()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Snackbar.make(activity.findViewById(R.id.parent), "Bargain Request cancelled successfully", Snackbar.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });

        assert btnChangePrice != null;
        btnChangePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String price = edPrice.getText().toString();
                double wholesale = bargain.getOriginalPrice() * (1 - 0.5); //calculating wholesale price
                double bargainLimit = wholesale * 1.3; //calculating bargain limit
                if (price.isEmpty()) {
                    Snackbar.make(parent, "Empty Field", Snackbar.LENGTH_SHORT).show();
                    edPrice.requestFocus();
                    return;
                }
                if (bargain.getStatus().equals("rejected")) {
                    if (Integer.parseInt(price) <= bargain.getBargainPrice()) {
                        Snackbar.make(parent, "You cannot enter price lower than previous bargain rate!!", Snackbar.LENGTH_SHORT).show();
                        edPrice.requestFocus();
                        return;
                    }
                }
                if (Integer.parseInt(price) > bargain.getOriginalPrice()) {
                    edPrice.requestFocus();
                    Snackbar.make(parent, "You cannot enter price higher than the original price", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (Integer.parseInt(price) <= bargainLimit) {
                    edPrice.requestFocus();
                    Snackbar.make(parent, "Price is too low !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (!bargain.getBlocked()) {
                    int noOfTries = bargain.getNoOfTries();
                    if (noOfTries > 0) {
                        dialog.show();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("bargainPrice", Integer.valueOf(price));
                        map.put("timestamp", new Date().getTime());
                        if (bargain.getNoOfTries() == 1) {
                            map.put("blocked", true);
                        }
                        noOfTries = noOfTries - 1;
                        map.put("noOfTries", noOfTries);
                        map.put("status", "pending");
                        database.getReference().child("Bargain").child(bargain.getBargainId()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dialog.dismiss();
                                Snackbar.make(activity.findViewById(R.id.parent), "Bargain request sent successfully", Snackbar.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }
                        });
                    }
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a");
                    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bargain.getTimestamp()), ZoneId.systemDefault());
                    LocalDateTime afterDate = dateTime.plusDays(1);
                    Snackbar.make(parent, "Sorry! Try after " + afterDate.format(formatter), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        bottomSheetDialog.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtProductName, txtBargainDate, txtStatus;
        LinearLayout mainLayout;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProdName);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtBargainDate = itemView.findViewById(R.id.txtBargainDate);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
