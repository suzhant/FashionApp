package com.sushant.fashionapp.Buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.ActivityHomePage;
import com.sushant.fashionapp.Models.Users;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityOtpactivityBinding;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    ActivityOtpactivityBinding binding;
    String phoneNumber, verificationId;
    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        binding.txtOTP.setText("Enter the OTP sent to" + phoneNumber);

        binding.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.circularProgressIndicator.setVisibility(View.VISIBLE);
                binding.btnVerify.setVisibility(View.GONE);
                String code = binding.edOTP.getText().toString();
                if (code.isEmpty()) {
                    binding.ipOTP.requestFocus();
                    binding.ipOTP.setError("OTP cannot be empty.");
                    return;
                }
                if (verificationId != null) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    auth.signInWithCredential(credential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    binding.circularProgressIndicator.setVisibility(View.GONE);
                                    binding.btnVerify.setVisibility(View.VISIBLE);
                                    if (task.isSuccessful()) {
                                        Users user = new Users();
                                        String id = task.getResult().getUser().getUid();
                                        user.setUserId(id);
                                        user.setUserPhone(phoneNumber);
                                        database.getReference().child("Users").child(id).setValue(user);


                                        Intent intent = new Intent(getApplicationContext(), ActivityHomePage.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                                        Toast.makeText(OTPActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                            // The verification code entered was invalid
                                            Toast.makeText(OTPActivity.this, "Verification code entered was Invalid", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }
            }
        });

        binding.btnResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.circularProgressIndicator.setVisibility(View.VISIBLE);
                binding.btnVerify.setVisibility(View.GONE);
                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber("+977" + phoneNumber)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(OTPActivity.this)   // Activity (for callback binding)
                                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
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
                        Toast.makeText(OTPActivity.this, "Invalid Request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        binding.circularProgressIndicator.setVisibility(View.GONE);
                        binding.btnVerify.setVisibility(View.VISIBLE);
                        Toast.makeText(OTPActivity.this, "The SMS quota for the project has been exceeded: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(OTPActivity.this, "OTP Received", Toast.LENGTH_SHORT).show();
                }
            };

}