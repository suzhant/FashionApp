package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.R;

import java.util.ArrayList;
import java.util.Objects;

public class EditSizeAdapter extends RecyclerView.Adapter<EditSizeAdapter.viewHolder> {

    ArrayList<Product> sizes;
    Context context;

    public EditSizeAdapter(ArrayList<Product> sizes, Context context) {
        this.sizes = sizes;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_size_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Product product = sizes.get(position);
        String size = product.getSize();
        String stock = product.getStock().toString();
        holder.txtStock.setText(String.format("Stock: %s", stock));
        holder.txtSize.setText(String.format("Size: %s", size));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(size, stock, holder);
            }
        });

    }

    private void openDialog(String size, String stock, viewHolder holder) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_size);
        MaterialButton btnSave = bottomSheetDialog.findViewById(R.id.btnSave);
        AutoCompleteTextView autoSize = bottomSheetDialog.findViewById(R.id.autoSize);
        TextInputEditText edStock = bottomSheetDialog.findViewById(R.id.edStock);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        assert autoSize != null;
        autoSize.setText(size);
        edStock.setText(stock);
        bottomSheetDialog.show();

        String[] sizeList = context.getResources().getStringArray(R.array.size);
        autoSize.setAdapter(new ArrayAdapter<String>(context, R.layout.drop_down_items, sizeList));

        assert btnSave != null;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sizes.remove(holder.getAbsoluteAdapterPosition());
                Product product = new Product();
                String size = autoSize.getText().toString();
                String stock = edStock.getText().toString();
                product.setSize(size);
                product.setStock(Integer.valueOf(stock));
                sizes.add(holder.getAbsoluteAdapterPosition(), product);
                notifyItemChanged(holder.getAbsoluteAdapterPosition());
                bottomSheetDialog.dismiss();
            }
        });


    }

    @Override
    public int getItemCount() {
        return sizes.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        private final TextView txtSize;
        private final TextView txtStock;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            txtSize = itemView.findViewById(R.id.txtSize);
            txtStock = itemView.findViewById(R.id.txtStock);
        }
    }
}
