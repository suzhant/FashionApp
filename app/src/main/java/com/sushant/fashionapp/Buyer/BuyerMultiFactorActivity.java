package com.sushant.fashionapp.Buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityBuyerMutiFactorBinding;

import java.util.concurrent.TimeUnit;

public class BuyerMultiFactorActivity extends AppCompatActivity {

    ActivityBuyerMutiFactorBinding binding;
    FirebaseAuth auth;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuyerMutiFactorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = binding.edPhone.getText().toString();
                binding.circularProgressIndicator.setVisibility(View.VISIBLE);
                binding.btnVerify.setVisibility(View.GONE);

                if (phoneNumber.isEmpty()) {
                    binding.circularProgressIndicator.setVisibility(View.GONE);
                    binding.btnVerify.setVisibility(View.VISIBLE);
                    binding.ipPhoneNumber.requestFocus();
                    binding.ipPhoneNumber.setError("Phone number cannot be empty!!");
                    return;
                }
                binding.ipPhoneNumber.setErrorEnabled(false);

                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber("+977" + phoneNumber)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(BuyerMultiFactorActivity.this)   // Activity (for callback binding)
                                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    // This callback is invoked in response to invalid requests for
                    // verification, like an incorrect phone number.
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        binding.circularProgressIndicator.setVisibility(View.GONE);
                        binding.btnVerify.setVisibility(View.VISIBLE);
                        Snackbar.make(binding.getRoot(), "Invalid Request: " + e.getMessage(), Snackbar.LENGTH_LONG).setTextMaxLines(2).show();
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        binding.circularProgressIndicator.setVisibility(View.GONE);
                        binding.btnVerify.setVisibility(View.VISIBLE);
                        Snackbar.make(binding.getRoot(), "The SMS quota for the project has been exceeded: " + e.getMessage(), Snackbar.LENGTH_SHORT).setTextMaxLines(2).show();
                    }
                    // Show a message and update the UI
                    // ...
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number.
                    // We now need to ask the user to enter the code and then construct a
                    // credential by combining the code with a verification ID.
                    // Save the verification ID and resending token for later use.
                    binding.circularProgressIndicator.setVisibility(View.GONE);
                    binding.btnVerify.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(BuyerMultiFactorActivity.this, OTPActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                }
            };
}