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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
    HashSet<String> masterCategory;
    HashSet<String> brand;
    HashSet<String> season;
    HashSet<String> category;
    ItemClickListener itemClickListener;
    BottomSheetDialog bottomSheetDialog;
    ArrayList<String> subItems;
    TextView txtClear;
    String type, storeId;

    public SortFilterAdapter(ArrayList<SortModel> list, Context context, ItemClickListener itemClickListener) {
        this.list = list;
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    public SortFilterAdapter(ArrayList<SortModel> list, Context context, ItemClickListener itemClickListener, String storeId, String type) {
        this.list = list;
        this.context = context;
        this.itemClickListener = itemClickListener;
        this.type = type;
        this.storeId = storeId;
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

        Query query = FirebaseDatabase.getInstance().getReference().child("Products").orderByChild("storeId").equalTo(storeId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                colors = new HashSet<>();
                masterCategory = new HashSet<>();
                brand = new HashSet<>();
                season = new HashSet<>();
                category = new HashSet<>();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Product product = snapshot1.getValue(Product.class);
                    for (int i = 0; i < product.getVariants().size(); i++) {
                        colors.add(product.getVariants().get(i).getColor());
                        masterCategory.add(product.getMasterCategory());
                        category.add(product.getCategory());
                    }
                    brand.add(product.getBrandName());
                    season.add(product.getSeason());
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
        txtClear = bottomSheetDialog.findViewById(R.id.txtClear);
        initListView(listView, item, holder);
        title.setText(item.getName());
        bottomSheetDialog.show();

        if (!holder.txtDesc.getText().equals("All")) {
            txtClear.setVisibility(View.VISIBLE);
        }

        assert txtClear != null;
        txtClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onClick("All", item.getName());
                holder.txtDesc.setText("All");
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void initListView(ListView listView, SortModel item, viewHolder holder) {
        subItems = new ArrayList<>();
        switch (item.getName()) {
            case "Sort by":
//                subItems.add("Ascending");
//                subItems.add("Descending");
                subItems.add("Time: new to old");
                subItems.add("Time: old to new");
                subItems.add("Price: low to high");
                subItems.add("Price: high to low");
                break;
            case "Category":
                if (type != null && type.equals("shop")) {
                    subItems.addAll(category);
                } else {
                    subItems.addAll(masterCategory);
                }
                break;
            case "Colour":
                subItems.addAll(colors);
                break;
            case "Brand":
                subItems.addAll(brand);
                break;
            case "Season":
                subItems.addAll(season);
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
