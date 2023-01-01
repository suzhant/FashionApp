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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.ChatAdapter;
import com.sushant.fashionapp.Models.ChatModel;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.databinding.FragmentChatBinding;

import java.util.ArrayList;


public class ChatFragment extends Fragment {

    FragmentChatBinding binding;
    ChatAdapter adapter;
    ArrayList<ChatModel> chatModels = new ArrayList<>();
    FirebaseDatabase database;
    FirebaseAuth auth;
    String storeId;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        Query query = database.getReference().child("Store").orderByChild("buyerId").equalTo(auth.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Store store = snapshot1.getValue(Store.class);
                    storeId = store.getStoreId();
                    database.getReference().child("Chats").child("Store").child(storeId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            chatModels.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                ChatModel chatModel = snapshot1.getValue(ChatModel.class);
                                chatModels.add(chatModel);
                            }
                            adapter.notifyItemRangeChanged(0, chatModels.size());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                initRecycler();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return binding.getRoot();
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        binding.recyclerChat.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(requireContext(), chatModels, storeId, "Store");
        binding.recyclerChat.setAdapter(adapter);
    }
}