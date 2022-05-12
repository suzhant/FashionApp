package com.sushant.fashionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DateKeyListener;
import android.text.method.DigitsKeyListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialCalendar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sushant.fashionapp.databinding.ActivityRegisterBinding;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ActivityRegister extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ActivityRegisterBinding binding;
    String gender;
    FirebaseAuth auth;
    FirebaseDatabase database;
    boolean isDobValid=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        //setting gender adapter for dropdown menu
        String[] genders= getResources().getStringArray(R.array.gender);
        binding.autoComplete.setAdapter(new ArrayAdapter<String>(getApplicationContext(),R.layout.drop_down_items,genders));

        binding.autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gender=binding.autoComplete.getText().toString();
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
                finishAfterTransition();
            }
        });

        binding.btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFieldEmpty() | !isChecked() | !nameValidation() | !genderValidation() | !validateDOB() | !validatePhoneNumber()){
                    return;
                }


            }
        });

        binding.edPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.ipPhoneNumber.setCounterEnabled(true);
                if (charSequence.length()<10){
                    binding.ipPhoneNumber.setCounterTextColor(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                }else {
                    binding.ipPhoneNumber.setCounterTextColor(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.edDOB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    binding.ipDOB.setErrorEnabled(false);
                }
            }
        });


    }

    private boolean isFieldEmpty(){
        String name=binding.edUserName.getText().toString();
        String Gender=binding.autoComplete.getText().toString();
        String DOB=binding.edDOB.getMasked();
        String PhoneNum=binding.edPhone.getText().toString();
        String email=binding.edMail.getText().toString();
        String Password=binding.edPass.getText().toString();
        String Repass=binding.edRePass.getText().toString();
        if (name.isEmpty() | Gender.isEmpty() | DOB.isEmpty() |PhoneNum.isEmpty()|email.isEmpty()|Password.isEmpty()|Repass.isEmpty()){
            binding.txtError.setVisibility(View.VISIBLE);
            binding.txtError.setText("Please fill all the fields");
            return true;
        }else {
            binding.txtError.setVisibility(View.GONE);
        }
        return false;
    }

    private boolean nameValidation(){
        String name=binding.edUserName.getText().toString();
        if (name.isEmpty()){
            binding.ipUserName.setErrorEnabled(true);
            binding.ipUserName.setError("Empty UserName!");
            return false;
        }else {
            binding.ipUserName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean isChecked(){
        if (!binding.chkBox.isChecked()){
            binding.txtError.setVisibility(View.VISIBLE);
            binding.txtError.setText("Please check Terms and Services");
            return false;
        }
        return binding.chkBox.isChecked();
    }

    private boolean genderValidation(){
        String gender=binding.autoComplete.getText().toString();
        if (gender.isEmpty()){
            binding.ipGender.setError("Empty Field!");
            return false;
        }else {
            binding.ipGender.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateDOB(){
        String DOB=binding.edDOB.getMasked();
        if (DOB.isEmpty()){
            binding.ipDOB.setError("Empty field!");
            return false;
        }
        if (DOB.length()<10){
            binding.ipDOB.setError("Incorrect DOB");
            return false;
        }
        if (!validateDOBFormat()){
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
            binding.ipPhoneNumber.setError("Empty Phone Number!");
            return false;
        }
        if (phone.length()<10){
            binding.ipPhoneNumber.setError("Length of the Phone Number Must be 10!");
            return false;
        }

        if (!isNumberValid(phone)){
            binding.ipPhoneNumber.setError("Invalid Phone Number!");
            return false;
        }
        binding.ipPhoneNumber.setErrorEnabled(false);
        return true;
    }

    private boolean isNumberValid(String phone){
        String countryCode="+977";
        phone=countryCode+phone;
        PhoneNumberUtil phoneNumberUtil=PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneNumberUtil.parse(phone, "np");
            return phoneNumberUtil.isValidNumber(numberProto);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }
        return false;
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar= Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        calendar.set(Calendar.MONTH,monthOfYear);
        calendar.set(Calendar.YEAR,year);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
        binding.edDOB.setText(simpleDateFormat.format(calendar.getTime()));
    }
}