package com.sushant.fashionapp.fragments.Buyer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.OrderAdapter;
import com.sushant.fashionapp.Models.Order;
import com.sushant.fashionapp.databinding.FragmentOrderCancelledBinding;

import java.util.ArrayList;

public class OrderCancelledFragment extends Fragment {

    FragmentOrderCancelledBinding binding;
    OrderAdapter adapter;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Order> orders = new ArrayList<>();

    public OrderCancelledFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOrderCancelledBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        database.getReference().child("Orders").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orders.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order.getOrderStatus().equals("CANCELLED")) {
                        orders.add(order);
                    }
                }
                adapter.notifyItemInserted(orders.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        initRecycler();

        return binding.getRoot();
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.recyclerOrderCancelledFragment.setLayoutManager(layoutManager);
        adapter = new OrderAdapter(orders, getActivity());
        binding.recyclerOrderCancelledFragment.setAdapter(adapter);
    }
}