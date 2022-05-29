package com.sushant.fashionapp;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseCache extends Application {
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            database.setPersistenceEnabled(true);
        }
    }
}