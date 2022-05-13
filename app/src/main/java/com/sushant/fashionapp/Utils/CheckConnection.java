package com.sushant.fashionapp.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sushant.fashionapp.R;

import java.io.IOException;

public class CheckConnection {
    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo == null || !networkInfo.isConnectedOrConnecting());
    }

    public boolean isInternet() {

        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isOnline(Context context) {
        CheckConnection checkConnection = new CheckConnection();
        return !checkConnection.isConnected(context) || checkConnection.isInternet();
    }

    public static void showCustomDialog(Context context) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.RoundShapeTheme);
        builder.setMessage("Please connect to the internet to proceed forward")
                .setTitle("No Connection")
                .setCancelable(true)
                .setIcon(R.drawable.ic_wifi_off_24)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }
}
