package com.sushant.fashionapp.Utils;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.sushant.fashionapp.ActivitySignIn;
import com.sushant.fashionapp.R;

public class Dialogs {

    public static void logoutDialog(Context context, Activity activity) {
        if (CheckConnection.isOnline(context)) {
            new MaterialAlertDialogBuilder(context, R.style.RoundShapeTheme)
                    .setMessage("Do you want to logout?")
                    .setTitle("Logout")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            context.startActivity(new Intent(context.getApplicationContext(), ActivitySignIn.class));
                            FirebaseAuth.getInstance().signOut();
                            ActivityCompat.finishAfterTransition(activity);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        } else {
            CheckConnection.showCustomDialog(context);
        }
    }
}
