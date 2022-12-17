package com.sushant.fashionapp.Buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sushant.fashionapp.ActivityHomePage;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityOrderFinalPageBinding;

public class OrderFinalPageActivity extends AppCompatActivity {

    ActivityOrderFinalPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderFinalPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.statusLyt.radio1.setImageResource(R.drawable.ic_baseline_check_circle_24);
        binding.statusLyt.radio1.setImageTintList(ContextCompat.getColorStateList(this, R.color.black));
        binding.statusLyt.txt1.setTextColor(ContextCompat.getColor(this, R.color.black));
        binding.statusLyt.radio2.setImageResource(R.drawable.ic_baseline_check_circle_24);
        binding.statusLyt.radio2.setImageTintList(ContextCompat.getColorStateList(this, R.color.black));
        binding.statusLyt.txt2.setTextColor(ContextCompat.getColor(this, R.color.black));
        binding.statusLyt.txt3.setTextColor(ContextCompat.getColor(this, R.color.teal_700));
        binding.statusLyt.radio3.setImageTintList(ContextCompat.getColorStateList(this, R.color.teal_700));

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.btnContinueShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrderFinalPageActivity.this, ActivityHomePage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(OrderFinalPageActivity.this, ActivityHomePage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}