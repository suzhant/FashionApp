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
import com.sushant.fashionapp.databinding.FragmentOrderPendingBinding;

import java.util.ArrayList;

public class OrderPendingFragment extends Fragment {

    FragmentOrderPendingBinding binding;
    OrderAdapter adapter;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Order> orders = new ArrayList<>();

    public OrderPendingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOrderPendingBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        database.getReference().child("Orders").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orders.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order.getOrderStatus().equals("PENDING")) {
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
        binding.recyclerOrderPendingFragment.setLayoutManager(layoutManager);
        adapter = new OrderAdapter(orders, getActivity());
        binding.recyclerOrderPendingFragment.setAdapter(adapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}