package com.sushant.fashionapp.Utils;

import android.util.Patterns;

import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFieldValidation {

    public static boolean nameValidation(TextInputLayout input, String name) {
        if (name.isEmpty()) {
            input.requestFocus();
            input.setErrorEnabled(true);
            input.setError("Empty UserName!");
            return false;
        } else {
            input.setErrorEnabled(false);
        }
        return true;
    }

    public static boolean validateEmail(TextInputLayout input, String email) {
        if (email.isEmpty()) {
            input.requestFocus();
            input.setError("Empty Email!");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input.requestFocus();
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
            input.setError("Empty Password!");
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
}
