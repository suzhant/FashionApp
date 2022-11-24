package com.sushant.fashionapp.fragments.Seller;

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
import com.sushant.fashionapp.Adapters.BargainUserAdapter;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.databinding.FragmentBargainBinding;

import java.util.ArrayList;

public class BargainFragment extends Fragment {


    BargainUserAdapter adapter;
    ArrayList<Bargain> bargains = new ArrayList<>();
    FirebaseDatabase database;
    FirebaseAuth auth;
    String sellerId;

    FragmentBargainBinding binding;

    public BargainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBargainBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sellerId = snapshot.child("sellerId").getValue(String.class);
                database.getReference().child("Bargain").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bargains.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Bargain bargain = snapshot1.getValue(Bargain.class);
                            assert bargain != null;
                            if (sellerId.equals(bargain.getSellerId())) {
                                bargains.add(bargain);
                            }
                        }
                        adapter.notifyItemInserted(bargains.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
        binding.recyclerBargain.setLayoutManager(layoutManager);
        adapter = new BargainUserAdapter(bargains, getActivity());
        binding.recyclerBargain.setAdapter(adapter);
    }
}