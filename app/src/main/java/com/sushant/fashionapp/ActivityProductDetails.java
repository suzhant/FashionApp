package com.sushant.fashionapp;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.models.SlideModel;
import com.sushant.fashionapp.databinding.ActivityProductDetailsBinding;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ActivityProductDetails extends AppCompatActivity {

    ActivityProductDetailsBinding binding;
    int price;
    boolean isTextViewClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        price = getIntent().getIntExtra("pPrice", 0);
        binding.txtPrice.setText(MessageFormat.format("Rs. {0}", price));


        List<SlideModel> list = new ArrayList<>();
        list.add(new SlideModel(R.drawable.red_dress3, null));
        list.add(new SlideModel(R.drawable.red_skirt, null));
        list.add(new SlideModel(R.drawable.red_dress2, null));
        binding.imgSlider.setImageList(list);


        binding.txtDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTextViewClicked) {
                    binding.txtDescription.setMaxLines(Integer.MAX_VALUE);
                    isTextViewClicked = true;
                } else {
                    binding.txtDescription.setMaxLines(2);
                    isTextViewClicked = false;
                }
            }
        });
    }
}