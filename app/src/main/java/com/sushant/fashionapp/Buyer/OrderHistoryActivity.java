package com.sushant.fashionapp.Buyer;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.santalu.maskara.widget.MaskEditText;
import com.sushant.fashionapp.Adapters.OrderAdapter;
import com.sushant.fashionapp.Models.Order;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityOrderHistoryBinding;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OrderHistoryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    ActivityOrderHistoryBinding binding;
    OrderAdapter adapter;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Order> orders = new ArrayList<>();
    ArrayList<Order> tempList = new ArrayList<>();
    MaskEditText edStart, edEnd;
    double start, end;
    String status = "PENDING";
    ValueEventListener valueEventListener;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


//        binding.viewPager.setAdapter(new OrderHistoryFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
//        String[] titles = {"Pending", "Completed", "Cancelled"};
//        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
//                (tab, position) -> tab.setText(titles[position])
//        ).attach();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        end = new Date().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        start = calendar.getTimeInMillis();


        startQuery();


        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.filter) {
                    showBottomSheet();
                }
                return false;
            }
        });

        initRecycler();
    }

    private void startQuery() {
        DatabaseReference ref = database.getReference().child("Orders").child(auth.getUid());
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orders.clear();
                tempList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order.getOrderStatus().equals(status)) {
                        orders.add(order);
                        tempList.add(order);
                    }
                }
                if (!orders.isEmpty()) {
                    Collections.sort(orders, Order.newToOld);
                    adapter.notifyItemInserted(orders.size());
                } else {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addListenerForSingleValueEvent(valueEventListener);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_order_filter);
        AutoCompleteTextView autoStatus = bottomSheetDialog.findViewById(R.id.autoStatus);
        edStart = bottomSheetDialog.findViewById(R.id.edStart);
        edEnd = bottomSheetDialog.findViewById(R.id.edEnd);
        ImageView imgClose = bottomSheetDialog.findViewById(R.id.imgClose);
        MaterialButton btnApply = bottomSheetDialog.findViewById(R.id.btnApply);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        assert btnApply != null;
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status = autoStatus.getText().toString();
                filterByDate();
                bottomSheetDialog.dismiss();
            }
        });

        assert imgClose != null;
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        //status list
        String[] statusList = {"PENDING", "COMPLETED", "CANCELLED"};
        autoStatus.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, statusList));


        //setting up datePicker dialog
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        );
        dpd.setMaxDate(Calendar.getInstance());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli((long) start), ZoneId.systemDefault());
        LocalDateTime endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli((long) end), ZoneId.systemDefault());
        //   LocalDateTime weekAgo = dateTime.minusWeeks(1);
        edStart.setText(startTime.format(formatter));
        edEnd.setText(endTime.format(formatter));

        edStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getSupportFragmentManager(), "start");
            }
        });

        edEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getSupportFragmentManager(), "end");
            }
        });

        bottomSheetDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filterByDate() {
        orders.clear();
        Predicate<Order> orderPredicate = s -> s.getOrderDate() > start && s.getOrderDate() < end && s.getOrderStatus().equals(status);
        List<Order> order = tempList.stream()
                .filter(orderPredicate)
                .sorted(Order.newToOld)
                .collect(Collectors.toList());
        orders.addAll(order);

        adapter.notifyDataSetChanged();
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerOrder.setLayoutManager(layoutManager);
        adapter = new OrderAdapter(orders, this);
        binding.recyclerOrder.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.YEAR, year);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(calendar.getTimeInMillis()), ZoneId.systemDefault());
        assert view.getTag() != null;
        if (view.getTag().equals("start")) {
            edStart.setText(formatter.format(dateTime));
            start = calendar.getTimeInMillis();
        } else {
            edEnd.setText(formatter.format(dateTime));
            end = calendar.getTimeInMillis();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}