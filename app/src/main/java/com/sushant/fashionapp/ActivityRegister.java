package com.sushant.fashionapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.databinding.ActivityRegisterBinding;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
                if (isFieldEmpty() |!validateRePass() |!validatePass() |!validateEmail() |!validatePhoneNumber()
                        | !validateDOB() |!genderValidation() |!nameValidation()){
                    return;
                }


            }
        });

        binding.cpp.registerCarrierNumberEditText(binding.edPhone);


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
        boolean check=binding.chkBox.isChecked();
        if (!check | name.isEmpty() | Gender.isEmpty() | DOB.isEmpty() |PhoneNum.isEmpty()|email.isEmpty()|Password.isEmpty()|Repass.isEmpty()){
            binding.txtError.setVisibility(View.VISIBLE);
            binding.txtError.setText("Please fill all the fields");
            return true;
        }
        binding.txtError.setVisibility(View.GONE);
        return false;
    }

    private boolean nameValidation(){
        String name=binding.edUserName.getText().toString();
        if (name.isEmpty()){
            binding.ipUserName.requestFocus();
            binding.ipUserName.setErrorEnabled(true);
            binding.ipUserName.setError("Empty UserName!");
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
            binding.ipGender.setError("Empty Field!");
            return false;
        }
        binding.ipGender.setErrorEnabled(false);
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
            binding.ipPhoneNumber.requestFocus();
            binding.ipPhoneNumber.setError("Empty Phone Number!");
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
        String email=binding.edMail.getText().toString();
        if (email.isEmpty()){
            binding.ipEmail.requestFocus();
            binding.ipEmail.setError("Empty Email!");
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
            binding.ipPass.setError("Empty Password!");
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
            binding.ipRePass.setError("Empty Field!");
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
        Calendar calendar= Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        calendar.set(Calendar.MONTH,monthOfYear);
        calendar.set(Calendar.YEAR,year);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy");
        binding.edDOB.setText(simpleDateFormat.format(calendar.getTime()));
    }
}