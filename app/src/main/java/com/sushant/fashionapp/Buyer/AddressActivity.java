package com.sushant.fashionapp.Buyer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityAddressBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddressActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 2045;
    private static final int REQUEST_CHECK_SETTINGS = 4543;
    ActivityAddressBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<String> provinceList = new ArrayList<>();
    String label = "";
    ProgressDialog dialog;
    Boolean isDefault = false;
    String id = "";
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);

        database.getReference().child("Area").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                provinceList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String province = snapshot1.getKey();
                    provinceList.add(province);
                }
                binding.addressLayout.autoProvince.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, provinceList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (getLocation()) {
            binding.addressLayout.btnGetCurrentLocation.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.royal_blue)));
        }

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        binding.addressLayout.cpp.registerCarrierNumberEditText(binding.addressLayout.edPhone);
        binding.addressLayout.chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                binding.addressLayout.chipGroup.getCheckedChipId();
                for (int i = 0; i < binding.addressLayout.chipGroup.getChildCount(); i++) {
                    Chip child = (Chip) binding.addressLayout.chipGroup.getChildAt(i);
                    if (child.isChecked()) {
                        label = child.getText().toString();
                    }
                }
            }
        });

        binding.addressLayout.switchShipping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isDefault = b;
            }
        });

        Query query = database.getReference().child("Shipping Address").orderByChild("uId").equalTo(auth.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Address address = snapshot1.getValue(Address.class);
                        assert address != null;
                        if (address.getDefault()) {
                            id = address.getAddressId();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog();
            }
        });

        binding.addressLayout.btnGetCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AddressActivity.this);
                if (ActivityCompat.checkSelfPermission(AddressActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(AddressActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddressActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST);

                } else {
                    displayLocationSettingsRequest();
                }

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
                                        AddressActivity.this,
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
                Geocoder geocoder = new Geocoder(AddressActivity.this, Locale.getDefault());
                List<android.location.Address> addresses = geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude(), 1);
                String adminArea = addresses.get(0).getAdminArea();
                String subAdminArea = addresses.get(0).getSubAdminArea();
                String subLocality = addresses.get(0).getSubLocality();
                String postalCode = addresses.get(0).getPostalCode();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.addressLayout.edStreetAddress.setText(String.join(",", subLocality, postalCode));
                }
                binding.addressLayout.edCity.setText(subAdminArea);
                binding.addressLayout.autoProvince.setText(adminArea);
                binding.addressLayout.autoProvince.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, provinceList));
            } catch (IOException e) {
                e.printStackTrace();
            }
            stopLocationUpdates();
        }
    };

    private void confirmDialog() {
        new MaterialAlertDialogBuilder(this)
                .setMessage("Are you sure?")
                .setTitle("Add Address")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        {
                            String phone = binding.addressLayout.edPhone.getText().toString();
                            String fullName = binding.addressLayout.edFullName.getText().toString();
                            String streetAddress = binding.addressLayout.edStreetAddress.getText().toString();
                            String city = binding.addressLayout.edCity.getText().toString();
                            String province = binding.addressLayout.autoProvince.getText().toString();
                            String address = binding.addressLayout.edAddress.getText().toString();
                            String landMark = binding.addressLayout.edLandmark.getText().toString();
                            if (phone.isEmpty() | fullName.isEmpty() | streetAddress.isEmpty() | city.isEmpty() | province.isEmpty()) {
                                Snackbar.make(findViewById(R.id.parent), "Please fill all the fields", Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            boolean isValid = validatePhoneNumber(binding.addressLayout.ipPhoneNumber, binding.addressLayout.cpp);
                            if (isValid) {
                                String key = database.getReference().child("Shipping Address").push().getKey();
                                Address address1 = new Address(fullName, phone, streetAddress, city, province);
                                address1.setuId(auth.getUid());
                                address1.setDefault(isDefault);
                                address1.setAddressId(key);
                                if (!address.isEmpty()) {
                                    address1.setAddress(address);
                                }
                                if (!landMark.isEmpty()) {
                                    address1.setLandmark(landMark);
                                }
                                if (!label.isEmpty()) {
                                    address1.setLabel(label);
                                }
                                dialog.show();

                                if (!isDefault || id.isEmpty()) {
                                    database.getReference().child("Shipping Address").child(key).setValue(address1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(getApplicationContext(), CheckOutAcitivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    });
                                    return;
                                }

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("default", false);
                                database.getReference().child("Shipping Address").child(id).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        database.getReference().child("Shipping Address").child(key).setValue(address1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                dialog.dismiss();
                                                Intent intent = new Intent(getApplicationContext(), CheckOutAcitivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        });
                                    }
                                });

                            }
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private boolean validatePhoneNumber(TextInputLayout ipPhone, CountryCodePicker cpp) {
        if (!cpp.isValidFullNumber()) {
            ipPhone.requestFocus();
            ipPhone.setError("Invalid Phone Number!");
            return false;
        }
        ipPhone.setErrorEnabled(false);
        return true;
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
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
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    binding.addressLayout.btnGetCurrentLocation.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.royal_blue)));
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to

                    break;
            }
            dialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.addressLayout.cpp.deregisterCarrierNumberEditText();
    }
}