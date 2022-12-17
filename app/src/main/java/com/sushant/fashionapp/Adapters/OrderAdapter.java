package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sushant.fashionapp.Models.Order;
import com.sushant.fashionapp.R;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.viewHolder> {

    ArrayList<Order> orders;
    Context context;

    public OrderAdapter(ArrayList<Order> orders, Context context) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_pending, parent, false);
        return new viewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Order order = orders.get(position);

        holder.txtOrderId.setText(MessageFormat.format("Order ID: {0}", order.getOrderId()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(order.getOrderDate()), ZoneId.systemDefault());
        holder.txtPlaceOn.setText(MessageFormat.format("Placed on {0}", formatter.format(dateTime)));
        holder.txtTotal.setText(Html.fromHtml(MessageFormat.format("Total: <big><b><span style=color:#09AEA3>Rs. {0}</span></b></big>", order.getAmount())));


        initRecycler(holder, order);
    }

    private void initRecycler(viewHolder holder, Order order) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        holder.recyclerView.setLayoutManager(linearLayoutManager);
        OrderHistoryItemAdapter adapter = new OrderHistoryItemAdapter(order.getProducts(), context);
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView txtOrderId, txtPlaceOn, txtTotal;
        RecyclerView recyclerView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recycler_products);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtPlaceOn = itemView.findViewById(R.id.txtPlaceOn);
            txtTotal = itemView.findViewById(R.id.txtTotal);
        }
    }
}
