package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Buyer.ActivityProductDetails;
import com.sushant.fashionapp.Buyer.CartActivity;
import com.sushant.fashionapp.Inteface.ProductClickListener;
import com.sushant.fashionapp.Inteface.QuantityListener;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Size;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.TextUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.viewHolder> {
    ArrayList<Cart> products;
    Context context;
    ProductClickListener productClickListener;
    private final CartActivity cartActivity;
    QuantityListener listener;

    public CartAdapter(ArrayList<Cart> products, Context context, ProductClickListener productClickListener, QuantityListener listener) {
        this.products = products;
        this.context = context;
        this.productClickListener = productClickListener;
        this.cartActivity = (CartActivity) context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_items_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Cart product = products.get(position);
        Glide.with(context).load(product.getpPic()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(holder.imgProduct);
        holder.txtStoreName.setText(TextUtils.captializeAllFirstLetter(product.getStoreName()));
        if (product.getBargainPrice() != null) {
            holder.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}</big>", product.getBargainPrice())));
        } else {
            holder.txtPrice.setText(Html.fromHtml(MessageFormat.format("Rs. <big>{0}</big>", product.getpPrice())));
        }

        holder.txtProductName.setText(TextUtils.captializeAllFirstLetter(product.getpName()));
        holder.txtSize.setText(MessageFormat.format("Size: {0}", product.getSize()));
        holder.txtQuantity.setText(String.valueOf(product.getQuantity()));
        holder.txtColor.setText(MessageFormat.format("Color: {0}", product.getColor()));


        Query query = FirebaseDatabase.getInstance().getReference().child("Products").child(product.getpId()).child("variants")
                .orderByChild("color").equalTo(product.getColor());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        product.setVariantIndex(Integer.valueOf(snapshot1.getKey()));
                        Query query1 = FirebaseDatabase.getInstance().getReference().child("Products").child(product.getpId()).child("variants").child(snapshot1.getKey())
                                .child("sizes").orderByChild("size").equalTo(product.getSize());
                        query1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                                        Size size = snapshot11.getValue(Size.class);
                                        product.setSizeIndex(Integer.valueOf(snapshot11.getKey()));
                                        product.setStock(size.getStock());
                                        int remainingStock = product.getStock();
                                        if (remainingStock < 5) {
                                            holder.txtStock.setVisibility(View.VISIBLE);
                                            holder.txtStock.setText(MessageFormat.format("{0} item(s) left in stock", remainingStock));
                                        } else {
                                            holder.txtStock.setVisibility(View.GONE);
                                        }
                                        if (product.getStock() == 0) {
                                            holder.imgPlus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.hintColor)));
                                        }
                                        if (product.getQuantity() < product.getStock()) {
                                            holder.imgPlus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.skyBlue)));
                                        }
                                    }
                                    holder.txtUnavailabe.setVisibility(View.GONE);
                                } else {
                                    holder.txtUnavailabe.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        FirebaseDatabase.getInstance().getReference().child("Products").child(product.getpId()).child("variants").child(String.valueOf(product.getVariantIndex()))
//                .child("sizes")
//                .child(String.valueOf(product.getSizeIndex())).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    stock = snapshot.child("stock").getValue(Integer.class);
//                    if (stock < 5) {
//                        holder.txtStock.setVisibility(View.VISIBLE);
//                        holder.txtStock.setText(MessageFormat.format("only {0} item(s) in stock", stock));
//                    } else {
//                        holder.txtStock.setVisibility(View.GONE);
//                    }
//                    if (stock == 0) {
//                        holder.imgPlus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.hintColor)));
//                    }
//                    if (product.getQuantity() < stock) {
//                        holder.imgPlus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.skyBlue)));
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        if (product.getQuantity() > 1) {
            holder.imgMinus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
        }

        if (product.getQuantity() < 2) {
            holder.imgMinus.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.hintColor)));
        }
        if (cartActivity.checkedProducts.isEmpty()) {
            holder.cardView.setStrokeWidth(1);
        }

        if (cartActivity.isActionMode) {
            holder.quantityLayout.setVisibility(View.GONE);
        } else {
            holder.quantityLayout.setVisibility(View.VISIBLE);
        }

        holder.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cartActivity.isActionMode) {
                    if (holder.txtUnavailabe.getVisibility() == View.GONE) {
                        if (product.getStock() != 0) {
                            if (product.getQuantity() < 5) {
                                //    product.setQuantity(product.getQuantity() + 1);
                                //  updateStock(product, stock - 1,product.getQuantity()+1);
                                listener.onClick(product, product.getQuantity(), "plus");
                                //    updateCartQuantity(product);
                            } else {
                                Snackbar.make(cartActivity.findViewById(R.id.cartLayout), "Max limit is 5", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });

        holder.imgMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cartActivity.isActionMode) {
                    if (holder.txtUnavailabe.getVisibility() == View.GONE) {
                        if (product.getQuantity() > 1) {
                            //   product.setQuantity(product.getQuantity() - 1);
                            //  updateCartQuantity(product);
                            listener.onClick(product, product.getQuantity(), "minus");
                            //  updateStock(product, stock + 1,product.getQuantity()-1);
                        }
                    }
                }
            }
        });

        holder.imgProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cartActivity.isActionMode) {
                    if (holder.txtUnavailabe.getVisibility() == View.GONE) {
                        Intent intent = new Intent(context, ActivityProductDetails.class);
                        intent.putExtra("pPic", product.getPreviewPic());
                        intent.putExtra("pName", product.getpName());
                        intent.putExtra("pPrice", product.getpPrice());
                        intent.putExtra("pId", product.getpId());
                        intent.putExtra("storeId", product.getStoreId());
                        intent.putExtra("sName", product.getStoreName());
                        intent.putExtra("pDesc", product.getDesc());
                        intent.putExtra("articleType", product.getArticleType());
                        intent.putExtra("index", product.getVariantIndex());
                        context.startActivity(intent);
                    }
                }

            }
        });

        holder.txtProductName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cartActivity.isActionMode) {
                    if (holder.txtUnavailabe.getVisibility() == View.GONE) {
                        Intent intent = new Intent(context, ActivityProductDetails.class);
                        intent.putExtra("pPic", product.getpPic());
                        intent.putExtra("pName", product.getpName());
                        intent.putExtra("pPrice", product.getpPrice());
                        intent.putExtra("pId", product.getpId());
                        intent.putExtra("storeId", product.getStoreId());
                        intent.putExtra("sName", product.getStoreName());
                        intent.putExtra("articleType", product.getArticleType());
                        intent.putExtra("index", product.getVariantIndex());
                        context.startActivity(intent);
                    }
                }

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartActivity.isActionMode) {
                    if (holder.cardView.getStrokeWidth() == 1) {
                        holder.cardView.setStrokeWidth(5);
                        productClickListener.onClick(product, true);
                    } else {
                        holder.cardView.setStrokeWidth(1);
                        productClickListener.onClick(product, false);
                    }

                }
            }
        });

//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                if (!cartActivity.isActionMode) {
//                    productClickListener.onClick(product, true);
//                    holder.cardView.setStrokeWidth(5);
//                    cartActivity.selectedItem();
//                }
//                return false;
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public static class viewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;
        private final TextView txtStoreName;
        private final TextView txtProductName;
        private final TextView txtPrice;
        private final ImageView imgMinus;
        private final ImageView imgPlus;
        private final TextView txtQuantity;
        private final TextView txtSize;
        private final TextView txtColor;
        private final TextView txtStock;
        private final TextView txtUnavailabe;
        private final MaterialCardView cardView;
        private final LinearLayout quantityLayout;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtProductName = itemView.findViewById(R.id.txtProdName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgMinus = itemView.findViewById(R.id.imgMinus);
            imgPlus = itemView.findViewById(R.id.imgPlus);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            cardView = itemView.findViewById(R.id.cart_layout_parent);
            txtSize = itemView.findViewById(R.id.txtSize);
            quantityLayout = itemView.findViewById(R.id.quantityLyt);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtStock = itemView.findViewById(R.id.txtStock);
            txtUnavailabe = itemView.findViewById(R.id.txtUnavailable);
        }
    }

    private void updateCartQuantity(Product product) {
        HashMap<String, Object> quantity = new HashMap<>();
        quantity.put("quantity", product.getQuantity());
        FirebaseDatabase.getInstance().getReference().child("Cart").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .child(product.getVariantPId()).updateChildren(quantity);

    }

    private void updateStock(Product product, int s, int q) {
        HashMap<String, Object> stock = new HashMap<>();
        stock.put("stock", s);
        FirebaseDatabase.getInstance().getReference().child("Products").child(product.getpId()).child("variants").child(String.valueOf(product.getVariantIndex()))
                .child("sizes")
                .child(String.valueOf(product.getSizeIndex())).updateChildren(stock).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                HashMap<String, Object> quantity = new HashMap<>();
                quantity.put("quantity", q);
                FirebaseDatabase.getInstance().getReference().child("Cart").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .child(product.getVariantPId()).updateChildren(quantity);
            }
        });
    }

}
