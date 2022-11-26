package com.sushant.fashionapp.Seller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.location.LocationRequest;
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
import com.google.firebase.auth.FirebaseAuth;
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
import com.sushant.fashionapp.Models.Seller;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.ImageUtils;
import com.sushant.fashionapp.databinding.ActivitySellerProfileBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SellerProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_CHECK_SETTINGS = 102;
    private static final int LOCATION_PERMISSION_REQUEST = 104;
    ActivitySellerProfileBinding binding;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog dialog;
    String storePic, sellerId, storeName, storeId;
    ActivityResultLauncher<Intent> imageLauncher;
    DatabaseReference userRef, storeRef;
    ValueEventListener userListener, storeListener;
    LocationManager mLocationManager;
    TextInputEditText edAddress;
    MaterialButton btnGetCurrentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerProfileBinding.inflate(getLayoutInflater());
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
                Seller seller = snapshot.getValue(Seller.class);
                assert seller != null;
                sellerId = seller.getSellerId();
                storeId = seller.getStoreId();
                storeRef = database.getReference().child("Store").child(storeId);
                storeListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Store store = snapshot.getValue(Store.class);
                        assert store != null;
                        if (snapshot.child("storeName").exists()) {
                            storeName = store.getStoreName();
                            binding.txtName.setText(storeName);
                        } else {
                            binding.txtName.setText("Name not registered yet");
                        }

                        if (snapshot.child("storePhone").exists()) {
                            String phone = store.getStorePhone();
                            binding.txtPhone.setText(phone);
                        } else {
                            binding.txtPhone.setText("Phone number not registered yet");
                        }

                        if (snapshot.child("storeAddress").exists()) {
                            String address = store.getStoreAddress();
                            binding.txtAddress.setText(address);
                        } else {
                            binding.txtAddress.setText("Address not registered yet");
                        }

                        if (snapshot.child("storeSecondaryEmail").exists()) {
                            String email = store.getStoreSecondaryEmail();
                            binding.txtEmail.setText(email);
                        } else {
                            binding.txtEmail.setText("Secondary email not registered yet");
                        }

                        if (snapshot.child("storePic").exists()) {
                            storePic = store.getStorePic();
                            Glide.with(getApplicationContext()).load(storePic)
                                    .placeholder(R.drawable.avatar)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(binding.imgChangePhoto);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                storeRef.addValueEventListener(storeListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userRef.addListenerForSingleValueEvent(userListener);


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


    private void showBottomSheetPhotoDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_photo);
        MaterialButton btnTakePhoto = bottomSheetDialog.findViewById(R.id.btnTakePhoto);
        MaterialButton btnUploadFromGallery = bottomSheetDialog.findViewById(R.id.btnUploadFromGallery);
        assert btnTakePhoto != null;
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(SellerProfileActivity.this)
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
            bitmap = ImageUtils.handleSamplingAndRotationBitmap(SellerProfileActivity.this, imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        uploadImageToFirebase(bytes);
    }

    private void uploadImageToFirebase(byte[] uri) {
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Store Images").child(storeId).child(calendar.getTimeInMillis() + "");
        dialog.setTitle("Changing store picture");
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
                            image.put("storePic", filePath);
                            database.getReference().child("Store").child(storeId).updateChildren(image);
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
            }
        });
    }

    private void showBottomSheetNameDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_store_name);
        MaterialButton btnSave = bottomSheetDialog.findViewById(R.id.btnSave);
        TextInputEditText edName = bottomSheetDialog.findViewById(R.id.edName);
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
                String Name = edName.getText().toString();
                if (Name.isEmpty()) {
                    Snackbar.make(parent, "Field cannot be empty!!", Snackbar.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                HashMap<String, Object> name = new HashMap<>();
                name.put("storeName", Name);
                database.getReference().child("Store").child(storeId).updateChildren(name).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                    dialog.setTitle("Changing Phone Number");
                    dialog.setMessage("Please wait");
                    dialog.show();

                    HashMap<String, Object> phoneObj = new HashMap<>();
                    phoneObj.put("storePhone", phone);
                    database.getReference().child("Store").child(storeId).updateChildren(phoneObj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            bottomSheetDialog.dismiss();
                            dialog.dismiss();
                            Snackbar.make(binding.getRoot(), "Phone number Changed successfully", Snackbar.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

        assert edphone != null;
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
                addressObj.put("storeAddress", address);
                database.getReference().child("Store").child(storeId).updateChildren(addressObj).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SellerProfileActivity.this);
                if (ActivityCompat.checkSelfPermission(SellerProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(SellerProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SellerProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
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

    private void displayLocationSettingsRequest() {
        dialog.setTitle("Fetching location");
        dialog.setMessage("Please wait...");
        dialog.show();
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(LocationRequest.Builder.IMPLICIT_MIN_UPDATE_INTERVAL)
                .setMaxUpdateAgeMillis(20000)
                .build();

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
                                        SellerProfileActivity.this,
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
                Geocoder geocoder = new Geocoder(SellerProfileActivity.this, Locale.getDefault());
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
                    emailObj.put("storeSecondaryEmail", email);
                    database.getReference().child("Store").child(storeId).updateChildren(emailObj).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
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
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        if (data.getData() != null) {
                            Uri selectedImage = data.getData();
                            createImageBitmap(selectedImage);
                        }
                    }
                }
                break;
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
        if (storeRef != null) {
            storeRef.removeEventListener(storeListener);
        }
    }

}