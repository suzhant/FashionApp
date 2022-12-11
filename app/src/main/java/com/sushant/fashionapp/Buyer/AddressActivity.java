package com.sushant.fashionapp.Buyer;

import android.app.ProgressDialog;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AddressActivity extends AppCompatActivity {

    ActivityAddressBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<String> provinceList = new ArrayList<>();
    String label = "";
    ProgressDialog dialog;
    Boolean isDefault = false;
    String id = "";

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
                binding.autoProvince.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, provinceList));
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


        binding.cpp.registerCarrierNumberEditText(binding.edPhone);
        binding.chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                binding.chipGroup.getCheckedChipId();
                for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                    Chip child = (Chip) binding.chipGroup.getChildAt(i);
                    if (child.isChecked()) {
                        label = child.getText().toString();
                    }
                }
            }
        });

        binding.switchShipping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                String phone = binding.edPhone.getText().toString();
                String fullName = binding.edFullName.getText().toString();
                String streetAddress = binding.edStreetAddress.getText().toString();
                String city = binding.edCity.getText().toString();
                String province = binding.autoProvince.getText().toString();
                String address = binding.edAddress.getText().toString();
                String landMark = binding.edLandmark.getText().toString();
                if (phone.isEmpty() | fullName.isEmpty() | streetAddress.isEmpty() | city.isEmpty() | province.isEmpty()) {
                    Snackbar.make(findViewById(R.id.parent), "Please fill all the fields", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                boolean isValid = validatePhoneNumber(binding.ipPhoneNumber, binding.cpp);
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
        });
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
        binding.cpp.deregisterCarrierNumberEditText();
    }
}