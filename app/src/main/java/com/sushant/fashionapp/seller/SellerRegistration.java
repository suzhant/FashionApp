package com.sushant.fashionapp.seller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.TextFieldValidation;
import com.sushant.fashionapp.databinding.ActivitySellerRegistrationBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SellerRegistration extends AppCompatActivity {

    ActivitySellerRegistrationBinding binding;
    ActivityResultLauncher<Intent> imgCitizenFrontLauncher;
    ActivityResultLauncher<Intent> imgCitizenBackLauncher;
    ActivityResultLauncher<Intent> imgPANFrontLauncher;
    ActivityResultLauncher<Intent> imgPANBackLauncher;
    android.app.DatePickerDialog.OnDateSetListener setListener;
    FirebaseDatabase database;
    FirebaseAuth auth;
    int year, month, day;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellerRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("userName").getValue(String.class);
                String phoneNo = snapshot.child("userPhone").getValue(String.class);
                String email = snapshot.child("userEmail").getValue(String.class);
                binding.sellerLayout.edUserName.setText(name);
                binding.sellerLayout.edPhone.setText(phoneNo);
                binding.sellerLayout.edMail.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.storeLayout.edShopDescription.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (view.getId() == R.id.edShopDescription) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                return false;
            }
        });

        binding.sellerLayout.cardCitizenFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_STREAM, true);
                imgCitizenFrontLauncher.launch(intent);
            }
        });

        binding.sellerLayout.cardCitizenBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_STREAM, true);
                imgCitizenBackLauncher.launch(intent);
            }
        });

        binding.sellerLayout.cardPANFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_STREAM, true);
                imgPANFrontLauncher.launch(intent);
            }
        });

        binding.sellerLayout.cardPANBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_STREAM, true);
                imgPANBackLauncher.launch(intent);
            }
        });

        binding.sellerLayout.imgCitizenFrontClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage(binding.sellerLayout.imgCitizenFront, binding.sellerLayout.linearCitizenFront, binding.sellerLayout.cardCitizenFront, binding.sellerLayout.imgCitizenFrontClose);
            }
        });

        binding.sellerLayout.imgCitizenBackClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage(binding.sellerLayout.imgCitizenBack, binding.sellerLayout.linearCitizenBack, binding.sellerLayout.cardCitizenBack, binding.sellerLayout.imgCitizenBackClose);
            }
        });

        binding.sellerLayout.imgPANFrontClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage(binding.sellerLayout.imgPANFront, binding.sellerLayout.linearPANFront, binding.sellerLayout.cardPANFront, binding.sellerLayout.imgPANFrontClose);
            }
        });

        binding.sellerLayout.imgPANBackClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage(binding.sellerLayout.imgPANBack, binding.sellerLayout.linearPANBack, binding.sellerLayout.cardPANBack, binding.sellerLayout.imgPANBackClose);
            }
        });


        binding.storeLayout.edShopDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.storeLayout.ipShopDescription.setCounterEnabled(editable.length() > 0);

            }
        });
        imgCitizenFrontLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                binding.sellerLayout.linearCitizenFront.setVisibility(View.GONE);
                                binding.sellerLayout.imgCitizenFront.setImageURI(selectedImage);
                                binding.sellerLayout.cardCitizenFront.setEnabled(false);
                                binding.sellerLayout.imgCitizenFrontClose.setVisibility(View.VISIBLE);
                                //     createImageBitmap(selectedImage);
                            }
                        }
                    }
                });

        imgCitizenBackLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                binding.sellerLayout.linearCitizenBack.setVisibility(View.GONE);
                                binding.sellerLayout.imgCitizenBack.setImageURI(selectedImage);
                                binding.sellerLayout.cardCitizenBack.setEnabled(false);
                                binding.sellerLayout.imgCitizenBackClose.setVisibility(View.VISIBLE);
                                //     createImageBitmap(selectedImage);
                            }
                        }
                    }
                });

        imgPANFrontLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                binding.sellerLayout.linearPANFront.setVisibility(View.GONE);
                                binding.sellerLayout.imgPANFront.setImageURI(selectedImage);
                                binding.sellerLayout.cardPANFront.setEnabled(false);
                                binding.sellerLayout.imgPANFrontClose.setVisibility(View.VISIBLE);
                                //     createImageBitmap(selectedImage);
                            }
                        }
                    }
                });

        imgPANBackLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                binding.sellerLayout.linearPANBack.setVisibility(View.GONE);
                                binding.sellerLayout.imgPANBack.setImageURI(selectedImage);
                                binding.sellerLayout.cardPANBack.setEnabled(false);
                                binding.sellerLayout.imgPANBackClose.setVisibility(View.VISIBLE);
                                //     createImageBitmap(selectedImage);
                            }
                        }
                    }
                });

        binding.btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validDOB = TextFieldValidation.validateDOB(binding.sellerLayout.ipDOB, binding.sellerLayout.edDOB.getMasked());
                boolean validSellerEmail = TextFieldValidation.validateEmail(binding.sellerLayout.ipEmail, binding.sellerLayout.edMail.getText().toString());
                boolean validStoreEmail = TextFieldValidation.validateEmail(binding.storeLayout.ipEmail, binding.storeLayout.edMail.getText().toString());
                boolean validSellerPhone = TextFieldValidation.validatePhoneNumber(binding.sellerLayout.ipPhoneNumber, binding.sellerLayout.edPhone.getText().toString(),
                        binding.sellerLayout.cpp);
                boolean validStorePhone = TextFieldValidation.validatePhoneNumber(binding.storeLayout.ipPhoneNumber, binding.storeLayout.edPhone.getText().toString(),
                        binding.storeLayout.cpp);
                if (isFieldEmpty() | !validStorePhone | !validStoreEmail | !validSellerPhone | !validDOB | !validSellerEmail) {
                    return;
                }
            }
        });


        binding.sellerLayout.edDOB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    binding.sellerLayout.edDOB.setHint("dd/mm/yyyy");
                } else {
                    binding.sellerLayout.edDOB.setHint("");
                }
            }
        });


        //setting up datePicker dialog
        final Calendar now = Calendar.getInstance();
        year = now.get(Calendar.YEAR);
        month = now.get(Calendar.MONTH);
        day = now.get(Calendar.DAY_OF_MONTH);

        binding.sellerLayout.ipDOB.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.DatePickerDialog datePickerDialog;
                System.out.println(binding.sellerLayout.edDOB.getText());
                if (binding.sellerLayout.edDOB.getUnMasked().length() == 8) {
                    int d = Integer.parseInt(binding.sellerLayout.edDOB.getUnMasked().substring(0, 2));
                    int m = Integer.parseInt(binding.sellerLayout.edDOB.getUnMasked().substring(2, 4));
                    int y = Integer.parseInt(binding.sellerLayout.edDOB.getUnMasked().substring(4, 8));
                    day = d;
                    month = m - 1;
                    year = y;
                }
                datePickerDialog = new android.app.DatePickerDialog(SellerRegistration.this
                        , android.R.style.Theme_Holo_Light_Dialog_MinWidth, setListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.setCancelable(false);
                datePickerDialog.show();
            }
        });

        setListener = new android.app.DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int dayOfMonth) {
                now.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                now.set(Calendar.MONTH, m);
                now.set(Calendar.YEAR, y);
                String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now.getTime());
                binding.sellerLayout.edDOB.setText(date);
                year = y;
                month = m;
                day = dayOfMonth;
            }
        };

        //store phone number prefix generator
        binding.storeLayout.cpp.registerCarrierNumberEditText(binding.storeLayout.edPhone);
        binding.storeLayout.ipPhoneNumber.setPrefixText(binding.storeLayout.cpp.getSelectedCountryCodeWithPlus());
        binding.storeLayout.cpp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                binding.storeLayout.ipPhoneNumber.setPrefixText(binding.storeLayout.cpp.getSelectedCountryCodeWithPlus());
            }
        });

        //seller phone number prefix generator
        binding.sellerLayout.cpp.registerCarrierNumberEditText(binding.sellerLayout.edPhone);
        binding.sellerLayout.ipPhoneNumber.setPrefixText(binding.sellerLayout.cpp.getSelectedCountryCodeWithPlus());
        binding.sellerLayout.cpp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                binding.sellerLayout.ipPhoneNumber.setPrefixText(binding.sellerLayout.cpp.getSelectedCountryCodeWithPlus());
            }
        });


    }

    private boolean isFieldEmpty() {
        String storeName = binding.storeLayout.edStoreName.getText().toString();
        String DOB = binding.sellerLayout.edDOB.getMasked();
        String PhoneNum = binding.storeLayout.edPhone.getText().toString();
        String email = binding.storeLayout.edMail.getText().toString();
        String shopAddress = binding.storeLayout.edShopAddress.getText().toString();
        String vatNo = binding.storeLayout.edVatNo.getText().toString();
        String panNo = binding.sellerLayout.edPanNo.getText().toString();
        String citizenNo = binding.sellerLayout.edCitizenNo.getText().toString();
        String shopDesc = binding.storeLayout.edShopDescription.getText().toString();
        Drawable citizenFront = binding.sellerLayout.imgCitizenFront.getDrawable();
        Drawable citizenBack = binding.sellerLayout.imgCitizenBack.getDrawable();
        Drawable panFront = binding.sellerLayout.imgPANFront.getDrawable();
        Drawable panBack = binding.sellerLayout.imgPANBack.getDrawable();


        if (storeName.isEmpty() | DOB.isEmpty() | PhoneNum.isEmpty() | email.isEmpty() | shopAddress.isEmpty() | vatNo.isEmpty()
                | panNo.isEmpty() | citizenNo.isEmpty() | shopDesc.isEmpty() | citizenFront == null | citizenBack == null | panFront == null | panBack == null) {
            Snackbar snackbar;
            snackbar = Snackbar.make(binding.parent, "Please fill all the fields", Snackbar.LENGTH_SHORT);
            TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error_24, 0, 0, 0);
            textView.setCompoundDrawableTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.skyBlue)));
            textView.setCompoundDrawablePadding(getResources().getDimensionPixelOffset(com.google.android.material.R.dimen.design_snackbar_padding_horizontal));
            textView.setGravity(Gravity.CENTER);
            snackbar.show();
            return true;
        }
//        binding.txtError.setVisibility(View.GONE);
        return false;
    }

    private void deleteImage(ImageView image, LinearLayout linearLayout, MaterialCardView cardView, ImageView close) {
        image.setImageURI(null);
        linearLayout.setVisibility(View.VISIBLE);
        cardView.setEnabled(true);
        close.setVisibility(View.GONE);
    }
}