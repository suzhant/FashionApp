package com.sushant.fashionapp;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sushant.fashionapp.databinding.ActivityImageBinding;

public class ImageActivity extends AppCompatActivity {

    ActivityImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });
    }
}