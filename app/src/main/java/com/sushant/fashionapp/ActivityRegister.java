package com.sushant.fashionapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;
import com.sushant.fashionapp.Models.Buyer;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.databinding.ActivityRegisterBinding;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivityRegister extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ActivityRegisterBinding binding;
    String gender;
    FirebaseAuth auth;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //setting gender adapter for dropdown menu
        String[] genders = getResources().getStringArray(R.array.gender);
        binding.autoComplete.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, genders));

        binding.autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gender = binding.autoComplete.getText().toString();
            }
        });

        //setting up datePicker dialog
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                 ActivityRegister.this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        );
        dpd.setMaxDate(Calendar.getInstance());


        binding.ipDOB.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getSupportFragmentManager(), "Datepickerdialog");
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityRegister.this, ActivitySignIn.class));
                finishAfterTransition();
            }
        });

        binding.btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!CheckConnection.isOnline(ActivityRegister.this)) {
                    CheckConnection.showCustomDialog(ActivityRegister.this);
                    return;
                }
                if (isFieldEmpty() | !validateRePass() | !validatePass() | !validateEmail() | !validatePhoneNumber() | !nameValidation()) {
                    return;
                }
                boolean check = binding.chkBox.isChecked();
                if (!check) {
                    Snackbar snackbar;
                    snackbar = Snackbar.make(binding.ActivityRegisterParent, "Please agree to the terms and conditions", Snackbar.LENGTH_SHORT);
                    TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_error_24, 0, 0, 0);
                    textView.setCompoundDrawableTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.skyBlue)));
                    textView.setCompoundDrawablePadding(getResources().getDimensionPixelOffset(com.google.android.material.R.dimen.design_snackbar_padding_horizontal));
                    textView.setGravity(Gravity.CENTER);
                    snackbar.show();
                    return;
                }
                hideSoftKeyboard();
                binding.btnCreateAcc.setVisibility(View.GONE);
                binding.circularProgressIndicator.setVisibility(View.VISIBLE);
                String name = binding.edUserName.getText().toString();
                String PhoneNum = binding.edPhone.getText().toString().trim();
                String email = binding.edMail.getText().toString().trim();
                String Password = binding.edPass.getText().toString().trim();
                auth.createUserWithEmailAndPassword(email, Password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // dialog.dismiss();
                                binding.circularProgressIndicator.setVisibility(View.GONE);
                                binding.btnCreateAcc.setVisibility(View.VISIBLE);
                                if (task.isSuccessful()) {
                                    showCreatingDialog();
                                    FirebaseUser users = auth.getCurrentUser();
                                    Buyer user = new Buyer(name, email, PhoneNum);
                                    String id = task.getResult().getUser().getUid();
                                    user.setUserId(id);
                                    database.getReference().child("Users").child(id).setValue(user);
                                    resetAllFields();
                                    assert users != null;

                                    users.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // Toast.makeText(getApplicationContext(), "Verification link sent to " + users.getEmail(), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Snackbar.make(binding.ActivityRegisterParent, "Verification link couldn't be sent", Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Snackbar.make(binding.ActivityRegisterParent, Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()), Snackbar.LENGTH_SHORT)
                                            .setTextMaxLines(2).show();
                                }

                            }
                        });


            }
        });

        binding.cpp.registerCarrierNumberEditText(binding.edPhone);
        binding.ipPhoneNumber.setPrefixText(binding.cpp.getSelectedCountryCodeWithPlus());
        binding.cpp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                binding.ipPhoneNumber.setPrefixText(binding.cpp.getSelectedCountryCodeWithPlus());
            }
        });

        binding.edDOB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    binding.edDOB.setHint("dd/mm/yyyy");
                } else {
                    binding.edDOB.setHint("");
                }
            }
        });

    }

    private void resetAllFields() {
        binding.edUserName.setText("");
        binding.autoComplete.setText("");
        binding.edDOB.setText("");
        binding.edPhone.setText("");
        binding.edMail.setText("");
        binding.edPass.setText("");
        binding.edRePass.setText("");
    }

    private boolean isFieldEmpty() {
        String name = binding.edUserName.getText().toString();
        String PhoneNum = binding.edPhone.getText().toString();
        String email = binding.edMail.getText().toString();
        String Password = binding.edPass.getText().toString();
        String Repass = binding.edRePass.getText().toString();

        if (name.isEmpty() | PhoneNum.isEmpty() | email.isEmpty() | Password.isEmpty() | Repass.isEmpty()) {
//            binding.txtError.setVisibility(View.VISIBLE);
//            binding.txtError.setText("Please fill all the fields");
            Snackbar snackbar;
            snackbar = Snackbar.make(binding.ActivityRegisterParent, "Please fill all the fields", Snackbar.LENGTH_SHORT);
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

    private boolean nameValidation(){
        String name=binding.edUserName.getText().toString();
        if (name.isEmpty()) {
            binding.ipUserName.requestFocus();
            //    binding.ipUserName.setErrorEnabled(true);
            //    binding.ipUserName.setError("Empty UserName!");
            return false;
        }else {
            binding.ipUserName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean genderValidation(){
        String gender=binding.autoComplete.getText().toString();
        if (gender.isEmpty()){
            binding.ipGender.requestFocus();
            //    binding.ipGender.setError("Empty Field!");
            return false;
        }
        binding.ipGender.setErrorEnabled(false);
        return true;
    }

    private boolean validateDOB(){
        String DOB=binding.edDOB.getMasked();
        if (DOB.isEmpty()){
            binding.ipDOB.requestFocus();
            //    binding.ipDOB.setError("Empty field!");
            return false;
        }
        if (DOB.length()<10){
            binding.ipDOB.requestFocus();
            binding.ipDOB.setError("Incorrect DOB");
            return false;
        }
        if (!validateDOBFormat()){
            binding.ipDOB.requestFocus();
            binding.ipDOB.setError("Incorrect DOB");
            return false;
        }
        binding.ipDOB.setErrorEnabled(false);
        return true;
    }

    private boolean validateDOBFormat(){
        String DOB=binding.edDOB.getMasked();
        int day= Integer.parseInt(DOB.substring(0,2));
        int month= Integer.parseInt(DOB.substring(3,5));
        int year= Integer.parseInt(DOB.substring(6,10));
        return !(day > 31 | month > 12 | year > Calendar.getInstance().get(Calendar.YEAR) | day < 1 | month < 1 | year < 1);
    }

    private boolean validatePhoneNumber(){
        String phone=binding.edPhone.getText().toString();
        if (phone.isEmpty()){
            binding.ipPhoneNumber.requestFocus();
            //   binding.ipPhoneNumber.setError("Empty Phone Number!");
            return false;
        }

        if (!binding.cpp.isValidFullNumber()){
            binding.ipPhoneNumber.requestFocus();
            binding.ipPhoneNumber.setError("Invalid Phone Number!");
            return false;
        }
        binding.ipPhoneNumber.setErrorEnabled(false);
        return true;
    }
    private boolean validateEmail(){
        String email = binding.edMail.getText().toString().trim();
        if (email.isEmpty()){
            binding.ipEmail.requestFocus();
            //   binding.ipEmail.setError("Empty Email!");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.ipEmail.requestFocus();
            binding.ipEmail.setError("Invalid Email Address!");
            return false;
        }
        binding.ipEmail.setErrorEnabled(false);
        return true;
    }

    private boolean validatePass(){
        String pass=binding.edPass.getText().toString();
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(pass);
        if (pass.isEmpty()){
            binding.ipPass.requestFocus();
            //    binding.ipPass.setError("Empty Password!");
            return false;
        }
        if (!m.matches()) {
            binding.ipPass.requestFocus();
            binding.ipPass.setError("Password should contain minimum 8 character,at least 1 letter and 1 number ");
            return false;
        }
        binding.ipPass.setErrorEnabled(false);
        return true;
    }

    private boolean validateRePass(){
        String RePass=binding.edRePass.getText().toString();
        String pass=binding.edPass.getText().toString();
        if (RePass.isEmpty()){
            binding.ipRePass.requestFocus();
            //     binding.ipRePass.setError("Empty Field!");
            return false;
        }
        if (!RePass.equals(pass)){
            binding.ipRePass.requestFocus();
            binding.ipRePass.setError("Password doesn't match!");
            return false;
        }
        binding.ipRePass.setErrorEnabled(false);
        return true;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.YEAR, year);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        binding.edDOB.setText(simpleDateFormat.format(calendar.getTime()));
    }


    private void showCreatingDialog() {
        Dialog dialog;
        MaterialButton btnLogin;
        ImageView imgClose;
        TextView txtVerification;
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_dialog);
        LottieAnimationView lottieAnimationView = dialog.findViewById(R.id.lottie_create);
        btnLogin = dialog.findViewById(R.id.btnLogin);
        imgClose = dialog.findViewById(R.id.imgClose);
        txtVerification = dialog.findViewById(R.id.txtVerification);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
        txtVerification.setText("Please verify your account at " + binding.edMail.getText());

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (auth.getCurrentUser() != null) {
                    auth.signOut();
                }
                startActivity(new Intent(ActivityRegister.this, ActivitySignIn.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

            }
        });
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
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

    @Override
    public void onBackPressed() {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }
        finishAfterTransition();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }
        super.onDestroy();
    }
}