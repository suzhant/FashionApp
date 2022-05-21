package com.sushant.fashionapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

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
    Button btnBack;
    Dialog email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        email = new Dialog(ActivityForgotPass.this);
        email.requestWindowFeature(Window.FEATURE_NO_TITLE);
        email.setContentView(R.layout.custom_email_sent);
        btnBack = email.findViewById(R.id.btnBack);

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
                hideSoftKeyboard();
                String email = binding.edMail.getText().toString().trim();
                if (!TextFieldValidation.validateEmail(binding.ipEmail, email)) {
                    return;
                }
                binding.btnResetPass.setVisibility(View.GONE);
                binding.circularProgressIndicator.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            binding.circularProgressIndicator.setVisibility(View.GONE);
                            binding.btnResetPass.setVisibility(View.VISIBLE);
                            Objects.requireNonNull(binding.edMail.getText()).clear();
                            showEmailDialog();
                        } else {
                            binding.circularProgressIndicator.setVisibility(View.GONE);
                            binding.btnResetPass.setVisibility(View.VISIBLE);
                            binding.ipEmail.setErrorEnabled(true);
                            binding.ipEmail.setError("Email doesn't exist");
                            binding.ipEmail.requestFocus();
                        }
                    }
                });
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        email.dismiss();
                    }
                }, 500);

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

    private void showEmailDialog() {
        email.setCanceledOnTouchOutside(false);
        email.show();
        email.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        email.getWindow().setGravity(Gravity.CENTER);

    }
}