package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Buyer.ViewMoreActivity;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.SortModel;
import com.sushant.fashionapp.R;

import java.util.ArrayList;
import java.util.HashSet;

public class SortFilterAdapter extends RecyclerView.Adapter<SortFilterAdapter.viewHolder> {

    ArrayList<SortModel> list;
    Context context;
    HashSet<String> colors;
    ItemClickListener itemClickListener;
    BottomSheetDialog bottomSheetDialog;
    ViewMoreActivity viewMoreActivity;
    ArrayList<String> subItems;

    public SortFilterAdapter(ArrayList<SortModel> list, Context context, ItemClickListener itemClickListener) {
        this.list = list;
        this.context = context;
        this.itemClickListener = itemClickListener;
        this.viewMoreActivity = (ViewMoreActivity) context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sort_item_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        SortModel item = list.get(position);
        holder.txtTitle.setText(item.getName());
        holder.txtDesc.setText(item.getDesc());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(item, holder);
            }
        });


        FirebaseDatabase.getInstance().getReference().child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                colors = new HashSet<>();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    for (int i = 0; i < product.getVariants().size(); i++) {
                        colors.add(product.getVariants().get(i).getColor());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showDialog(SortModel item, viewHolder holder) {
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.item_sort_dialog);
        ListView listView = bottomSheetDialog.findViewById(R.id.dialogListView);
        TextView title = bottomSheetDialog.findViewById(R.id.txtTitle);
        initListView(listView, item, holder);
        title.setText(item.getName());
        bottomSheetDialog.show();
    }

    private void initListView(ListView listView, SortModel item, viewHolder holder) {
        subItems = new ArrayList<>();
        switch (item.getName()) {
            case "Sort by":
                subItems.add("Ascending");
                subItems.add("Descending");
                subItems.add("Price: low to high");
                subItems.add("Price: high to low");
                break;
            case "Category":
                subItems.add("Male");
                subItems.add("Female");
                subItems.add("Kids");
                subItems.add("Toddler");
                break;
            case "Colour":
                subItems.addAll(colors);
                break;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, subItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                String text = ((TextView) view).getText().toString();
                itemClickListener.onClick(text, item.getName());
                holder.txtDesc.setText(text);
                bottomSheetDialog.dismiss();
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final TextView txtTitle;
        private final TextView txtDesc;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDesc = itemView.findViewById(R.id.txtdesc);
        }
    }
}
