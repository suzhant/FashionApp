package com.sushant.fashionapp.Buyer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.sushant.fashionapp.Models.Users;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.ImageUtils;
import com.sushant.fashionapp.databinding.ActivityBuyerProfileBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BuyerProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 102;
    private static final int LOCATION_PERMISSION_REQUEST = 104;
    ActivityBuyerProfileBinding binding;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog dialog;
    String buyerPic, buyerId, buyerName;
    ActivityResultLauncher<Intent> imageLauncher;
    DatabaseReference userRef;
    ValueEventListener userListener;
    int LOCATION_REFRESH_TIME = 5000; // 15 seconds to update
    int LOCATION_REFRESH_DISTANCE = 5; // 500 meters to update
    LocationManager mLocationManager;
    TextInputEditText edAddress;
    MaterialButton btnGetCurrentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    BottomSheetDialog bottomSheetOTPDialog;
    SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuyerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);

        userRef = database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()));
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users buyer = snapshot.getValue(Users.class);
                assert buyer != null;
                buyerId = buyer.getUserId();

                if (snapshot.child("userName").exists()) {
                    buyerName = buyer.getUserName();
                    binding.txtName.setText(buyerName);
                } else {
                    binding.txtName.setText("Name not registered yet");
                }

                if (snapshot.child("userPhone").exists()) {
                    String phone = buyer.getUserPhone();
                    binding.txtPhone.setText(phone);
                } else {
                    binding.txtPhone.setText("Phone number not registered yet");
                }

                if (snapshot.child("userAddress").exists()) {
                    String address = buyer.getUserAddress();
                    binding.txtAddress.setText(address);
                } else {
                    binding.txtAddress.setText("Address not registered yet");
                }

                if (snapshot.child("userSecondaryEmail").exists()) {
                    String email = buyer.getUserSecondaryEmail();
                    binding.txtEmail.setText(email);
                } else {
                    binding.txtEmail.setText("Secondary email not registered yet");
                }

                if (snapshot.child("userPic").exists()) {
                    buyerPic = snapshot.child("userPic").getValue(String.class);
                    Glide.with(getApplicationContext()).load(buyerPic)
                            .placeholder(R.drawable.avatar)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.imgChangePhoto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userRef.addValueEventListener(userListener);


        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.imgChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetPhotoDialog();
            }
        });

        binding.txtChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetPhotoDialog();
            }
        });

        imageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                createImageBitmap(selectedImage);
                            }
                        }
                    }
                });

        binding.linearChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetNameDialog();
            }
        });

        binding.linearChangePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetPhoneDialog();
            }
        });

        binding.linearChangeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetAddressDialog();
            }
        });

        binding.linearChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetEmailDialog();
            }
        });


    }

    private void showBottomSheetEmailDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_email_secondary);
        MaterialButton btnVerify = bottomSheetDialog.findViewById(R.id.btnVerify);
        TextInputEditText edEmail = bottomSheetDialog.findViewById(R.id.edEmail);
        TextInputLayout ipEmail = bottomSheetDialog.findViewById(R.id.ipEmail);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        bottomSheetDialog.show();

        assert btnVerify != null;
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edEmail.getText().toString();
                boolean isValid = validateEmail(email, ipEmail);
                if (isValid) {
                    dialog.setTitle("Setting email");
                    dialog.setMessage("Please wait..");
                    dialog.show();

                    HashMap<String, Object> emailObj = new HashMap<>();
                    emailObj.put("userSecondaryEmail", email);
                    database.getReference().child("Users").child(buyerId).updateChildren(emailObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            bottomSheetDialog.dismiss();
                            dialog.dismiss();
                            Snackbar.make(binding.getRoot(), "Secondary Email Changed successfully", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });

        edEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    ipEmail.setErrorEnabled(false);
                }

            }
        });

    }

    private boolean validateEmail(String email, TextInputLayout ipEmail) {
        if (email.isEmpty()) {
            ipEmail.requestFocus();
            ipEmail.setError("Email cannot be empty");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ipEmail.requestFocus();
            ipEmail.setError("Invalid Email Address!");
            return false;
        }
        ipEmail.setErrorEnabled(false);
        return true;
    }

    private void showBottomSheetAddressDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_address);
        MaterialButton btnSave = bottomSheetDialog.findViewById(R.id.btnSave);
        btnGetCurrentLocation = bottomSheetDialog.findViewById(R.id.btnGetCurrentLocation);
        edAddress = bottomSheetDialog.findViewById(R.id.edAddress);
        TextInputLayout ipAddress = bottomSheetDialog.findViewById(R.id.ipAddress);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!getLocation()) {
            btnGetCurrentLocation.setText("Turn on Location");
        }

        assert btnSave != null;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = edAddress.getText().toString();
                if (address.isEmpty()) {
                    ipAddress.requestFocus();
                    ipAddress.setError("Address cannot be empty");
                    return;
                }
                assert ipAddress != null;
                ipAddress.setErrorEnabled(false);

                dialog.setTitle("Saving location");
                dialog.setMessage("Please wait..");
                dialog.show();

                HashMap<String, Object> addressObj = new HashMap<>();
                addressObj.put("userAddress", address);
                database.getReference().child("Users").child(buyerId).updateChildren(addressObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        bottomSheetDialog.dismiss();
                        dialog.dismiss();
                        Snackbar.make(binding.getRoot(), "Address Changed successfully", Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
        });

        assert btnGetCurrentLocation != null;
        btnGetCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(BuyerProfileActivity.this);
                if (ActivityCompat.checkSelfPermission(BuyerProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(BuyerProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BuyerProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST);

                } else {
                    displayLocationSettingsRequest();
                }

            }
        });

        edAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    ipAddress.setErrorEnabled(false);
                }

            }
        });

        bottomSheetDialog.show();

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                stopLocationUpdates();
            }
        });

    }

    private void displayLocationSettingsRequest() {
        dialog.setTitle("Fetching location");
        dialog.setMessage("Please wait...");
        dialog.show();
        com.google.android.gms.location.LocationRequest locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
//                    // requests here.
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                            locationCallback,
                            Looper.getMainLooper());

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        BuyerProfileActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.

                            break;
                    }
                }
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            dialog.dismiss();
            try {
                Geocoder geocoder = new Geocoder(BuyerProfileActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude(), 1);
                String adminArea = addresses.get(0).getAdminArea();
                String subAdminArea = addresses.get(0).getSubAdminArea();
                String countryName = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String subLocality = addresses.get(0).getSubLocality();
                String address = adminArea + "," + subAdminArea + "," + subLocality + "," + countryName + "," + postalCode;
                edAddress.setText(address);
            } catch (IOException e) {
                e.printStackTrace();
            }
            stopLocationUpdates();
        }
    };

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    public boolean getLocation() {
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        // exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            networkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return gpsEnabled && networkEnabled;
    }




    private void showBottomSheetPhoneDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_phone);
        MaterialButton btnSave = bottomSheetDialog.findViewById(R.id.btnSave);
        TextInputEditText edphone = bottomSheetDialog.findViewById(R.id.edPhone);
        TextInputLayout ipPhone = bottomSheetDialog.findViewById(R.id.ipPhoneNumber);
        CountryCodePicker cpp = bottomSheetDialog.findViewById(R.id.cpp);
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        assert cpp != null;
        cpp.registerCarrierNumberEditText(edphone);
        ipPhone.setPrefixText(cpp.getSelectedCountryCodeWithPlus());
        cpp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                ipPhone.setPrefixText(cpp.getSelectedCountryCodeWithPlus());
            }
        });

        assert btnSave != null;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = edphone.getText().toString();
                boolean isValid = validatePhoneNumber(phone, ipPhone, cpp);
                if (isValid) {
                    dialog.setTitle("Sending SMS code");
                    dialog.setMessage("Please wait...");
                    dialog.show();

                    callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                            Toast.makeText(BuyerProfileActivity.this, credential.getSmsCode(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            // This callback is invoked in response to invalid requests for
                            // verification, like an incorrect phone number.
                            dialog.dismiss();
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // Invalid request
                                Snackbar.make(binding.getRoot(), "Invalid Request: " + e.getMessage(), Snackbar.LENGTH_LONG).setTextMaxLines(2).show();
                            } else if (e instanceof FirebaseTooManyRequestsException) {
                                // The SMS quota for the project has been exceeded
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
                            dialog.dismiss();
                            bottomSheetDialog.dismiss();
                            bottomSheetOTPDialog = new BottomSheetDialog(BuyerProfileActivity.this);
                            bottomSheetOTPDialog.setContentView(R.layout.bottomsheet_verify_otp);
                            MaterialButton btnVerify = bottomSheetOTPDialog.findViewById(R.id.btnVerify);
                            TextInputEditText edOTP = bottomSheetOTPDialog.findViewById(R.id.edOTP);
                            TextInputLayout ipOTP = bottomSheetOTPDialog.findViewById(R.id.ipOTP);
                            LinearLayout linearLayout = bottomSheetOTPDialog.findViewById(R.id.otpParent);
                            TextView txtBack = bottomSheetOTPDialog.findViewById(R.id.txt);
                            Objects.requireNonNull(bottomSheetOTPDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                            bottomSheetOTPDialog.show();


                            txtBack.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    bottomSheetOTPDialog.dismiss();
                                    bottomSheetDialog.show();
                                }
                            });
                            assert btnVerify != null;
                            btnVerify.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String code = edOTP.getText().toString();
                                    if (code.isEmpty()) {
                                        ipOTP.setError("Empty Field!!");
                                        ipOTP.requestFocus();
                                        return;
                                    }
                                    ipOTP.setErrorEnabled(false);

                                    dialog.setTitle("Changing Phone Number");
                                    dialog.setMessage("Please wait");
                                    dialog.show();

                                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

                                    auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                auth.signOut();
                                                sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                                                String email = sharedPreferences.getString("email", "");
                                                String pass = sharedPreferences.getString("password", "");
                                                if (!email.isEmpty() || !pass.isEmpty()) {
                                                    performAuth(email, pass, phone);
                                                }
                                            } else {
                                                dialog.dismiss();
                                                Snackbar.make(linearLayout, "Phone number cannot be verified", Snackbar.LENGTH_SHORT).show();
                                            }

                                        }
                                    });


                                }
                            });

                        }
                    };

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber("+977" + phone)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(BuyerProfileActivity.this)   // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        edphone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    ipPhone.setErrorEnabled(false);
                }
            }
        });

        bottomSheetDialog.show();

    }

    //Firebase Email Authentication
    public void performAuth(String email, String password, String phone) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            HashMap<String, Object> phoneObj = new HashMap<>();
                            phoneObj.put("userPhone", phone);
                            database.getReference().child("Users").child(buyerId).updateChildren(phoneObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    bottomSheetOTPDialog.dismiss();
                                    dialog.dismiss();
                                    Snackbar.make(binding.getRoot(), "Phone number Changed successfully", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            bottomSheetOTPDialog.dismiss();
                            dialog.dismiss();
                            Snackbar.make(binding.getRoot(), "Something went wrong! Please try again", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validatePhoneNumber(String phone, TextInputLayout ipPhone, CountryCodePicker cpp) {
        if (phone.isEmpty()) {
            ipPhone.requestFocus();
            ipPhone.setError("Phone Number cannot be empty!");
            return false;
        }

        if (!cpp.isValidFullNumber()) {
            ipPhone.requestFocus();
            ipPhone.setError("Invalid Phone Number!");
            return false;
        }
        ipPhone.setErrorEnabled(false);
        return true;
    }

    private void showBottomSheetNameDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_name);
        MaterialButton btnSave = bottomSheetDialog.findViewById(R.id.btnSave);
        TextInputEditText edFirstName = bottomSheetDialog.findViewById(R.id.edFirstName);
        TextInputEditText edLastName = bottomSheetDialog.findViewById(R.id.edLastName);
        LinearLayout parent = bottomSheetDialog.findViewById(R.id.nameParent);
        //putting bottomsheetdialog above keyboard
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        assert btnSave != null;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setTitle("Changing Name");
                dialog.setMessage("Please wait...");
                dialog.show();
                String firstName = edFirstName.getText().toString();
                String lastName = edLastName.getText().toString();
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    Snackbar.make(parent, "Please fill both fields", Snackbar.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                HashMap<String, Object> name = new HashMap<>();
                name.put("userName", firstName + " " + lastName);
                database.getReference().child("Users").child(buyerId).updateChildren(name).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        bottomSheetDialog.dismiss();
                        dialog.dismiss();
                        Snackbar.make(binding.getRoot(), "Name Changed successfully", Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
        });
        bottomSheetDialog.show();
    }

    private void showBottomSheetPhotoDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_photo);
        MaterialButton btnTakePhoto = bottomSheetDialog.findViewById(R.id.btnTakePhoto);
        MaterialButton btnUploadFromGallery = bottomSheetDialog.findViewById(R.id.btnUploadFromGallery);
        assert btnTakePhoto != null;
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(BuyerProfileActivity.this)
                        .cameraOnly()
                        .crop()
                        .start(REQUEST_IMAGE_CAPTURE);
                bottomSheetDialog.dismiss();
            }
        });
        assert btnUploadFromGallery != null;

        btnUploadFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                imageLauncher.launch(intent);
            }
        });

        bottomSheetDialog.show();
    }


    private void createImageBitmap(Uri imageUrl) {
        Bitmap bitmap = null;
        try {
            bitmap = ImageUtils.handleSamplingAndRotationBitmap(BuyerProfileActivity.this, imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        int length = bytes.length / 1024;
        uploadImageToFirebase(bytes, length);
    }

    private void uploadImageToFirebase(byte[] uri, int length) {
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Profile Images").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        if (length > 256) {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        dialog.setProgress(0);
        dialog.setMessage("Please wait");
        dialog.show();
        UploadTask uploadTask = reference.putBytes(uri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {
                    dialog.dismiss();
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();
                            HashMap<String, Object> image = new HashMap<>();
                            image.put("userPic", filePath);
                            database.getReference().child("Users").child(buyerId).updateChildren(image);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //only works if image size is greater than 256kb!
                if (length > 256) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    int currentProgress = (int) progress;
                    dialog.setProgress(currentProgress);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    displayLocationSettingsRequest();
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (data.getData() != null) {
                        Uri selectedImage = data.getData();
                        createImageBitmap(selectedImage);
                    }
                }
            }
        }

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        btnGetCurrentLocation.setText("Get Current Location");
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to

                        break;
                }
                dialog.dismiss();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null) {
            userRef.removeEventListener(userListener);
        }
    }
}