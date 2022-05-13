package com.sushant.fashionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.databinding.ActivityHomePageBinding;

public class ActivityHomePage extends AppCompatActivity {

    ActivityHomePageBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent intentLogout = new Intent(getApplicationContext(), WelcomeScreen.class);
                intentLogout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentLogout);
                Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
            }
        });
    }
}