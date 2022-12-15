package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;

import java.text.MessageFormat;
import java.util.ArrayList;

public class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.viewHolder> {

    ArrayList<Store> stores;
    Context context;
    int sum = 0;
    int price = 0;

    public OrderSummaryAdapter(ArrayList<Store> stores, Context context) {
        this.stores = stores;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_summary, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Store store = stores.get(position);

        FirebaseDatabase.getInstance().getReference().child("Store").child(store.getStoreId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String storeName = snapshot.child("storeName").getValue(String.class);
                holder.txtStoreName.setText(storeName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        for (Product product : store.getProducts()) {
            if (product.getBargainPrice() != null) {
                price = product.getBargainPrice();
            } else {
                price = product.getpPrice();
            }
            int total = price * product.getQuantity();
            sum = sum + total;
        }
        holder.txtSubtotal.setText(MessageFormat.format("Subtotal: Rs. {0}", sum + 70));
        sum = 0;


        //init recycler
        initRecycler(holder, store);


    }

    private void initRecycler(viewHolder holder, Store store) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        holder.recyclerView.setLayoutManager(layoutManager);
        OrderItemsAdapter orderItemsAdapter = new OrderItemsAdapter(store.getProducts(), context);
//        Log.d("summary",store.getProducts().toString());
        holder.recyclerView.setAdapter(orderItemsAdapter);
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;
        TextView txtStoreName, txtSubtotal;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerView = itemView.findViewById(R.id.recycler_items);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtSubtotal = itemView.findViewById(R.id.txtSubtotal);
        }
    }
}
