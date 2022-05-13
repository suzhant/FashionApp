package com.sushant.fashionapp;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.sushant.fashionapp.Utils.TextFieldValidation;
import com.sushant.fashionapp.databinding.ActivityForgotPassBinding;

import java.util.Objects;

public class ActivityForgotPass extends AppCompatActivity {

    ActivityForgotPassBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });

        binding.btnResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.edMail.getText().toString().trim();
                if (!TextFieldValidation.validateEmail(binding.ipEmail, email)) {
                    return;
                }
                auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Objects.requireNonNull(binding.edMail.getText()).clear();
                            hideSoftKeyboard();
                            Toast.makeText(getApplicationContext(), "Check your email", Toast.LENGTH_SHORT).show();
                        } else {
                            binding.ipEmail.setErrorEnabled(true);
                            binding.ipEmail.setError("Email doesn't exist");
                            binding.ipEmail.requestFocus();
                        }
                    }
                });
            }
        });
    }

    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}