package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sushant.fashionapp.Buyer.ChatActivity;
import com.sushant.fashionapp.Models.ChatModel;
import com.sushant.fashionapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.viewHolder> {

    Context context;
    ArrayList<ChatModel> chatModels;

    public ChatAdapter(Context context, ArrayList<ChatModel> chatModels) {
        this.context = context;
        this.chatModels = chatModels;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_store, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        ChatModel chat = chatModels.get(position);
        Glide.with(context).load(chat.getStorePic()).placeholder(R.drawable.avatar).into(holder.imgStore);
        holder.txtStoreName.setText(chat.getStoreName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("storeId", chat.getStoreId());
                intent.putExtra("from", "Buyer");
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatModels.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgStore;
        TextView txtStoreName, txtLastMessage, txtDate;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgStore = itemView.findViewById(R.id.imgStore);
            txtStoreName = itemView.findViewById(R.id.txtStoreName);
            txtLastMessage = itemView.findViewById(R.id.txtMessage);
            txtDate = itemView.findViewById(R.id.txtDate);

        }
    }
}
