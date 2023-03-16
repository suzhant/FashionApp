package com.sushant.fashionapp;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.NotificationAdapter;
import com.sushant.fashionapp.Models.NotificationModel;
import com.sushant.fashionapp.databinding.ActivityNotificationBinding;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationActivity extends AppCompatActivity {

    ActivityNotificationBinding binding;
    NotificationAdapter adapter;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<NotificationModel> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        database.getReference().child("Notification").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notifications.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    NotificationModel notification = snapshot1.getValue(NotificationModel.class);
                    notifications.add(notification);
                }
                Collections.sort(notifications, NotificationModel.latestTime);
                adapter.notifyItemInserted(notifications.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        initRecycler();

    }

    private void initRecycler() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerNotification.setLayoutManager(layoutManager);
        adapter = new NotificationAdapter(this, notifications);
        binding.recyclerNotification.setAdapter(adapter);
    }
}