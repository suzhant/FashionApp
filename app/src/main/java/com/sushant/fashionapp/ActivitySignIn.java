package com.sushant.fashionapp;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.databinding.ActivitySignInBinding;

import java.util.concurrent.Executor;

public class ActivitySignIn extends AppCompatActivity {


    ActivitySignInBinding binding;
    ProgressDialog dialog;
    FirebaseAuth auth;
    FirebaseDatabase database;
    Executor executor;
    private androidx.biometric.BiometricPrompt biometricPrompt;
    private androidx.biometric.BiometricPrompt.PromptInfo promptInfo;
    SharedPreferences sharedPreferences;
    boolean flag;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(this);

        //sign in using biometrics
        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                break;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                break;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                Toast.makeText(this, "Your device doesn't have fingerprint saved", Toast.LENGTH_SHORT).show();
                final Intent enrollIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startActivity(new Intent(enrollIntent));
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    startActivity(new Intent(Settings.ACTION_FINGERPRINT_ENROLL));
                } else {
                    startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                }
                // startActivityForResult(enrollIntent, REQUEST_CODE);
                break;
        }
        executor = ContextCompat.getMainExecutor(ActivitySignIn.this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                boolean isLogin = sharedPreferences.getBoolean("isGoogle", flag);
                if (!isLogin) {
                    String email = sharedPreferences.getString("email", "");
                    String pass = sharedPreferences.getString("password", "");
                    if (!email.isEmpty() || !pass.isEmpty()) {
                        performAuth(email, pass);
                    } else {
                        Toast.makeText(getApplicationContext(), "This is the first time you're using Biometric!! Please sign in manually", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();


        binding.btnFingerprint.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        boolean isLogin = sharedPreferences.getBoolean("isLogin", false);
        if (isLogin) {
            binding.btnFingerprint.setVisibility(View.VISIBLE);
        }


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
                if (isFieldEmpty()) {
                    return;
                }
                performAuth(email, pass);
            }
        });
    }

    private boolean isFieldEmpty() {
        String email = binding.edMail.getText().toString();
        String Password = binding.edPass.getText().toString();
        if (email.isEmpty() | Password.isEmpty()) {
            Snackbar.make(binding.parent, "Please fill all the fields", Snackbar.LENGTH_SHORT).show();
        }
        return email.isEmpty() | Password.isEmpty();
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
                            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.putBoolean("isLogin", true);
                            editor.putBoolean("isGoogle", flag);
                            editor.apply();

                            Intent intent = new Intent(getApplicationContext(), ActivityHomePage.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            Toast.makeText(ActivitySignIn.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(binding.parent, task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                            //   Toast.makeText(ActivitySignIn.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}