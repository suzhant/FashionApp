package com.sushant.fashionapp;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.Adapters.VPAdapter;
import com.sushant.fashionapp.databinding.ActivityImageBinding;

import java.text.MessageFormat;
import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {

    ActivityImageBinding binding;
    ArrayList<String> pic = new ArrayList<>();
    VPAdapter vpAdapter;
    FirebaseDatabase database;
    FirebaseAuth auth;
    int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        if (getIntent() != null) {
            pic = getIntent().getStringArrayListExtra("images");
            pos = getIntent().getIntExtra("position", 0);
        }
        vpAdapter = new VPAdapter(pic, this, false);
        binding.viewPager.setAdapter(vpAdapter);

        binding.viewPager.setCurrentItem(pos, false);

        if (pic.size() > 1) {
            binding.txtNoOfPics.setVisibility(View.VISIBLE);
            binding.txtNoOfPics.setText(MessageFormat.format("{0}/" + pic.size(), pos + 1));
        }

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                //  binding.txtNoOfPics.setText(MessageFormat.format("{0}/"+list.size(),position+1));
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.txtNoOfPics.setText(MessageFormat.format("{0}/" + pic.size(), position + 1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });


        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });
    }
}