package com.sushant.fashionapp.fragments.Seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.BargainUserAdapter;
import com.sushant.fashionapp.Models.Bargain;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.databinding.FragmentBargainBinding;

import java.util.ArrayList;
import java.util.Collections;

public class BargainFragment extends Fragment {


    BargainUserAdapter adapter;
    ArrayList<Bargain> bargains = new ArrayList<>();
    FirebaseDatabase database;
    FirebaseAuth auth;
    String sellerId;
    ValueEventListener valueEventListener;
    DatabaseReference reference;
    Store store = new Store();

    FragmentBargainBinding binding;

    public BargainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBargainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        reference = database.getReference().child("Users").child(auth.getUid());
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sellerId = snapshot.child("sellerId").getValue(String.class);
                Query query = database.getReference().child("Bargain").orderByChild("sellerId").equalTo(sellerId);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bargains.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Bargain bargain = snapshot1.getValue(Bargain.class);
                            assert bargain != null;
                            if (bargain.getStatus().equals("pending") || bargain.getStatus().equals("countered")) {
                                bargains.add(bargain);
                            }
                        }
                        Collections.sort(bargains, Bargain.latestTime);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reference.addValueEventListener(valueEventListener);

        initRecycler();

    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.recyclerBargain.setLayoutManager(layoutManager);
        adapter = new BargainUserAdapter(bargains, getActivity());
        binding.recyclerBargain.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reference != null) {
            reference.removeEventListener(valueEventListener);
        }
    }
}