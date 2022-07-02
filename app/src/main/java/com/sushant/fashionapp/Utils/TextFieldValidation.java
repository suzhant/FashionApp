package com.sushant.fashionapp.Utils;

import android.util.Patterns;

import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFieldValidation {

    public static boolean validateName(TextInputLayout input, String name) {
        if (name.isEmpty()) {
            input.requestFocus();
            //   input.setError("Empty "+message+"!");
            return false;
        }
        input.setErrorEnabled(false);
        return true;
    }

    public static boolean validateEmail(TextInputLayout input, String email) {
        if (email.isEmpty()) {
            // input.setError("Empty Email!");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input.setError("Invalid Email Address!");
            return false;
        }
        input.setErrorEnabled(false);
        return true;
    }

    public static boolean validatePass(TextInputLayout input, String pass) {
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(pass);
        if (pass.isEmpty()) {
            input.requestFocus();
            //  input.setError("Empty Password!");
            return false;
        }
        if (!m.matches()) {
            input.requestFocus();
            input.setError("Password should contain minimum 8 character,at least 1 letter and 1 number ");
            return false;
        }
        input.setErrorEnabled(false);
        return true;
    }

    public static boolean validateDOB(TextInputLayout ipDOB, String DOB) {
        if (DOB.isEmpty()) {

            //    binding.ipDOB.setError("Empty field!");
            return false;
        }
        if (DOB.length() < 10) {
            ipDOB.setError("Invalid DOB");
            return false;
        }
        if (!validateDOBFormat(DOB)) {
            ipDOB.setError("Invalid DOB");
            return false;
        }
        ipDOB.setErrorEnabled(false);
        return true;
    }

    static boolean validateDOBFormat(String DOB) {
        int day = Integer.parseInt(DOB.substring(0, 2));
        int month = Integer.parseInt(DOB.substring(3, 5));
        int year = Integer.parseInt(DOB.substring(6, 10));
        return !(day > 31 | month > 12 | year > Calendar.getInstance().get(Calendar.YEAR) | day < 1 | month < 1 | year < 1);
    }

    public static boolean validatePhoneNumber(TextInputLayout ipPhoneNumber, String phone, CountryCodePicker cpp) {
        if (phone.isEmpty()) {
            //   binding.ipPhoneNumber.setError("Empty Phone Number!");
            return false;
        }

        if (!cpp.isValidFullNumber()) {
            ipPhoneNumber.setError("Invalid Phone Number!");
            return false;
        }
        ipPhoneNumber.setErrorEnabled(false);
        return true;
    }
}
