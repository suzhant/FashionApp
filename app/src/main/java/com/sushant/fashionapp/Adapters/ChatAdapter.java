package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Buyer.ChatActivity;
import com.sushant.fashionapp.Models.Buyer;
import com.sushant.fashionapp.Models.ChatModel;
import com.sushant.fashionapp.Models.Message;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.viewHolder> {

    Context context;
    ArrayList<ChatModel> chatModels;
    String senderId, from, receiverName, receiverPic;

    public ChatAdapter(Context context, ArrayList<ChatModel> chatModels, String senderId, String from) {
        this.context = context;
        this.chatModels = chatModels;
        this.senderId = senderId;
        this.from = from;
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

        Glide.with(context).load(chat.getPic()).placeholder(R.drawable.avatar).into(holder.imgStore);
        holder.txtStoreName.setText(chat.getName());

        String to;
        if (from.equals("Buyer")) {
            to = "Store";
            FirebaseDatabase.getInstance().getReference().child(to).child(chat.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Store store = snapshot.getValue(Store.class);
                    receiverPic = store.getStorePic();
                    receiverName = store.getStoreName();
                    Glide.with(context).load(store.getStorePic()).placeholder(R.drawable.avatar).into(holder.imgStore);
                    holder.txtStoreName.setText(store.getStoreName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            to = "Users";
            FirebaseDatabase.getInstance().getReference().child(to).child(chat.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Buyer buyer = snapshot.getValue(Buyer.class);
                    receiverName = buyer.getUserName();
                    receiverPic = buyer.getUserPic();
                    Glide.with(context).load(buyer.getUserPic()).placeholder(R.drawable.avatar).into(holder.imgStore);
                    holder.txtStoreName.setText(buyer.getUserName());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        String senderRoom = senderId + chat.getId();
        Query query = FirebaseDatabase.getInstance().getReference().child("Messages").child(senderRoom).orderByKey().limitToLast(1);
        query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Message message = snapshot1.getValue(Message.class);
                    holder.txtLastMessage.setText(message.getMessage());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                    LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getTimestamp()), ZoneId.systemDefault());
                    holder.txtDate.setText(formatter.format(dateTime));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("senderId", senderId);
                intent.putExtra("receiverId", chat.getId());
                intent.putExtra("from", from);
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
