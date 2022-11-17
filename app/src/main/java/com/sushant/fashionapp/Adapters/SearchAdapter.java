package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Buyer.SearchResultActivity;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.viewHolder> {

    Context context;
    ArrayList<String> searchList;
    ArrayList<String> recent;
    ItemClickListener itemClickListener;

    public SearchAdapter(Context context, ArrayList<String> searchList, ArrayList<String> recent, ItemClickListener itemClickListener) {
        this.context = context;
        this.searchList = searchList;
        this.recent = recent;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        String query = searchList.get(position);
        holder.text.setText(query);
        if (!searchList.equals(recent)) {
            holder.imgStart.setImageResource(R.drawable.ic_search);
        } else {
            holder.imgStart.setImageResource(R.drawable.ic_order_history);
        }

        holder.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteHistory(query);
            }
        });

        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SearchResultActivity.class);
                intent.putExtra("pName", query);
                context.startActivity(intent);
                //   itemClickListener.onClick(query,null);
            }
        });


    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView imgStart, imgClose;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.txtDropDown);
            imgStart = itemView.findViewById(R.id.imgStart);
            imgClose = itemView.findViewById(R.id.imgClose);
        }
    }

    private void deleteHistory(String query) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference ref = database.getReference().child("Search History").child(auth.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String text = snapshot1.child("query").getValue(String.class);
                    if (text.equals(query)) {
                        ref.child(snapshot1.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
