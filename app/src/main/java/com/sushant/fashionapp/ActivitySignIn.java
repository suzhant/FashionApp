package com.sushant.fashionapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.Utils.TextFieldValidation;
import com.sushant.fashionapp.databinding.ActivitySignInBinding;

public class ActivitySignIn extends AppCompatActivity {

    ActivitySignInBinding binding;
    ProgressDialog dialog;
    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(this);

        binding.txtCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivitySignIn.this, ActivityRegister.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        binding.txtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivitySignIn.this, ActivityForgotPass.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });

        binding.btnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!CheckConnection.isOnline(ActivitySignIn.this)) {
                    CheckConnection.showCustomDialog(ActivitySignIn.this);
                    return;
                }
                String email = binding.edMail.getText().toString().trim();
                String pass = binding.edPass.getText().toString().trim();
                if (isFieldEmpty() | !TextFieldValidation.validateEmail(binding.ipEmail, email) | !passwordValidation()) {
                    return;
                }
                performAuth(email, pass);
            }
        });
    }

    private boolean isFieldEmpty() {
        String email = binding.edMail.getText().toString();
        String Password = binding.edPass.getText().toString();
        return email.isEmpty() | Password.isEmpty();
    }

    private boolean passwordValidation() {
        String pass = binding.edPass.getText().toString().trim();
        if (pass.isEmpty()) {
            binding.ipPass.setError("Empty Field!");
            binding.ipPass.requestFocus();
            return false;
        }
        binding.ipPass.setErrorEnabled(false);
        return true;
    }

    //Firebase Email Authentication
    public void performAuth(String email, String password) {
        dialog.setTitle("Login");
        dialog.setMessage("Login to your account");
        dialog.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            binding.edMail.setText("");
                            binding.edPass.setText("");
                            Intent intent = new Intent(getApplicationContext(), ActivityHomePage.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            Toast.makeText(ActivitySignIn.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ActivitySignIn.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}