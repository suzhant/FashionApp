package com.sushant.fashionapp.Buyer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.sushant.fashionapp.databinding.ActivityEditAddressBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class EditAddressActivity extends AppCompatActivity {

    ActivityEditAddressBinding binding;
    String addressId;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String label = "";
    Boolean isDefault = false;
    String id = "";
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        addressId = getIntent().getStringExtra("addressId");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);


        database.getReference().child("Shipping Address").child(addressId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Address address = snapshot.getValue(Address.class);
                String fullName = address.getName();
                String mobile = address.getMobile();
                String streetAddress = address.getStreetAddress();
                String city = address.getCity();
                String province = address.getProvince();
                if (address.getAddress() != null) {
                    String apt = address.getAddress();
                    binding.addressLayout.edAddress.setText(apt);
                }
                if (address.getLandmark() != null) {
                    String landmark = address.getLandmark();
                    binding.addressLayout.edLandmark.setText(landmark);
                }
                if (address.getLabel() != null) {
                    label = address.getLabel();
                    if (label.equals("Home")) {
                        binding.addressLayout.chipGroup.check(R.id.chipHome);
                    } else if (label.equals("Office")) {
                        binding.addressLayout.chipGroup.check(R.id.chipOffice);
                    }
                }
                if (address.getDefault() != null) {
                    Boolean isDefault = address.getDefault();
                    binding.addressLayout.switchShipping.setChecked(isDefault);
                }
                binding.addressLayout.edFullName.setText(fullName);
                binding.addressLayout.edPhone.setText(mobile);
                binding.addressLayout.edStreetAddress.setText(streetAddress);
                binding.addressLayout.edCity.setText(city);
                binding.addressLayout.autoProvince.setText(province);
                String[] provinceList = getResources().getStringArray(R.array.province);
                binding.addressLayout.autoProvince.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, provinceList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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
                for (int i = 0; i < binding.addressLayout.chipGroup.getChildCount(); i++) {
                    Chip child = (Chip) binding.addressLayout.chipGroup.getChildAt(i);
                    if (child.isChecked()) {
                        label = child.getText().toString();
                    }
                }
                if (binding.addressLayout.chipGroup.getCheckedChipIds().size() == 0) {
                    label = "";
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
    }

    private void confirmDialog() {
        new MaterialAlertDialogBuilder(this)
                .setMessage("Are you sure?")
                .setTitle("Edit Address")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("name", fullName);
                            map.put("mobile", phone);
                            map.put("streetAddress", streetAddress);
                            map.put("province", province);
                            map.put("default", isDefault);
                            if (!address.isEmpty()) {
                                map.put("address", address);
                            } else {
                                map.put("address", null);
                            }
                            if (!landMark.isEmpty()) {
                                map.put("landmark", landMark);
                            } else {
                                map.put("landmark", null);
                            }
                            if (!label.isEmpty()) {
                                map.put("label", label);
                            } else {
                                map.put("label", null);
                            }
                            dialog.show();

                            if (!isDefault || id.isEmpty()) {
                                database.getReference().child("Shipping Address").child(addressId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                            HashMap<String, Object> map1 = new HashMap<>();
                            map1.put("default", false);
                            database.getReference().child("Shipping Address").child(id).updateChildren(map1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("Shipping Address").child(addressId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.addressLayout.cpp.deregisterCarrierNumberEditText();
    }
}