package com.sushant.fashionapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.WelcomeScreen;
import com.sushant.fashionapp.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {

    FragmentAccountBinding binding;
    FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();

        binding.cardLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckConnection.isOnline(getContext())) {
                    auth.signOut();
                    Intent intentLogout = new Intent(getContext(), WelcomeScreen.class);
                    intentLogout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentLogout);
                    Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                } else {
                    CheckConnection.showCustomDialog(getContext());
                }

            }
        });
        return binding.getRoot();
    }
}