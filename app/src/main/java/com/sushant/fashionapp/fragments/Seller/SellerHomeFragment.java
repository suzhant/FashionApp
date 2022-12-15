package com.sushant.fashionapp.fragments.Seller;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.TextUtils;
import com.sushant.fashionapp.databinding.FragmentSellerHomeBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SellerHomeFragment extends Fragment {


    FragmentSellerHomeBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String sellerId, sellerName, sellerPic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSellerHomeBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("sellerId").exists()) {
                    sellerId = snapshot.child("sellerId").getValue(String.class);
                    database.getReference().child("Seller").child(sellerId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        sellerName = snapshot.child("userName").getValue(String.class);
                                        final String storeId = snapshot.child("storeId").getValue(String.class);
                                        database.getReference().child("Store").child(storeId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                String storeName = snapshot.child("storeName").getValue(String.class);
                                                binding.txtStoreName.setText(Html.fromHtml("Welcome <br><font color=\"#09AEA3\">"
                                                        + TextUtils.captializeAllFirstLetter(storeName) + "</font"));
                                                if (snapshot.child("storePic").exists()) {
                                                    String storePic = snapshot.child("storePic").getValue(String.class);
                                                    if (getActivity() != null) {
                                                        Glide.with(SellerHomeFragment.this).load(storePic).placeholder(R.drawable.avatar)
                                                                .diskCacheStrategy(DiskCacheStrategy.ALL).onlyRetrieveFromCache(true)
                                                                .into(binding.circleImageView);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        ArrayList<BarEntry> list = new ArrayList<>();
        list.add(new BarEntry(0f, 23));
        list.add(new BarEntry(1f, 10));
        list.add(new BarEntry(2f, 12));
        list.add(new BarEntry(3f, 34));
        list.add(new BarEntry(4f, 56));
        list.add(new BarEntry(5f, 34));
        list.add(new BarEntry(6f, 56));

        BarDataSet barDataSet = new BarDataSet(list, "Daily Sales");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);
        MyXAxisFormatter formatter = new MyXAxisFormatter();
        binding.barChart.getXAxis().setValueFormatter(formatter);


        BarData data = new BarData(barDataSet);
        data.setBarWidth(0.9f); // set custom bar width
        binding.barChart.setData(data);
        binding.barChart.getDescription().setText("");
        binding.barChart.setFitBars(true); // make the x-axis fit exactly all bars
        binding.barChart.animateY(2000);
        binding.barChart.invalidate();

        YAxis limitLine = binding.barChart.getAxisRight();
        limitLine.setEnabled(false);


//        Legend l = binding.barChart.getLegend();
//        l.setFormSize(10f); // set the size of the legend forms/shapes
//        l.setForm(Legend.LegendForm.CIRCLE); // set what type of form/shape should be used
//        l.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
//        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//        l.setTextSize(12f);
//        l.setTextColor(Color.BLACK);
//        l.setXEntrySpace(5f); // space between the legend entries on the x-axis
//        l.setYEntrySpace(5f); // space between the legend entries on the y-axis
//        // set custom labels and colors
//        List<LegendEntry> entries=new ArrayList<>();
//        DashPathEffect dashPathEffect=new DashPathEffect(new float[]{1f,2f,3f},12f);
//        entries.add(new LegendEntry("set1", Legend.LegendForm.CIRCLE,12f,12f, dashPathEffect,Color.RED));
//        entries.add(new LegendEntry("set2", Legend.LegendForm.CIRCLE,12f,12f, dashPathEffect,Color.RED));
//        entries.add(new LegendEntry("set3", Legend.LegendForm.CIRCLE,12f,12f, dashPathEffect,Color.RED));
//        l.setCustom(entries);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(18.5f, "Green"));
        entries.add(new PieEntry(26.7f, "Yellow"));
        entries.add(new PieEntry(24.0f, "Red"));
        entries.add(new PieEntry(30.8f, "Blue"));
        PieDataSet set = new PieDataSet(entries, "Election Results");
        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(16f);
        PieData pieData = new PieData(set);
        binding.pieChart.setData(pieData);
        binding.pieChart.animateY(2000);
        binding.pieChart.animate();
        binding.pieChart.getDescription().setText("");
        binding.pieChart.invalidate(); // refresh


        return binding.getRoot();
    }

    static class MyXAxisFormatter extends ValueFormatter {
        private final List<String> days = Arrays.asList("Mon", "Tue", "Wed", "Thr", "Fri", "Sat", "Sun");

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return days.get((int) value);
        }

    }
}