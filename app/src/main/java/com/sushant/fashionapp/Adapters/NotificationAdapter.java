package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.fashionapp.Buyer.BargainHistoryActivity;
import com.sushant.fashionapp.Buyer.ChatActivity;
import com.sushant.fashionapp.Models.NotificationModel;
import com.sushant.fashionapp.R;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder> {

    ArrayList<NotificationModel> notifications;
    Context context;

    public NotificationAdapter(Context context, ArrayList<NotificationModel> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new viewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        Glide.with(context).load(notification.getImageProfile()).placeholder(R.drawable.avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.imgProfile);

        holder.txtBody.setText(notification.getBody());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(notification.getTime()), ZoneId.systemDefault());
        holder.txtTime.setText(formatter.format(dateTime));

        if (!notification.getInteracted()) {
            holder.parent.setBackgroundColor(ContextCompat.getColor(context, R.color.skyblue_5));
        }

        String extraSentence = "";
        if (notification.getType().equals("message")) {
            extraSentence = " has messaged you:";
        }
        holder.txtTitle.setText(notification.getTitle() + extraSentence);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.getType().equals("message")) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("from", "Buyer");
                    intent.putExtra("senderId", FirebaseAuth.getInstance().getUid());
                    intent.putExtra("receiverId", notification.getReceiverId());
                    context.startActivity(intent);
                } else if (notification.getType().equals("bargain")) {
                    Intent intent = new Intent(context, BargainHistoryActivity.class);
                    context.startActivity(intent);
                }
                enableInteraction(notification);
            }
        });
    }

    private void enableInteraction(NotificationModel notification) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("interacted", true);
        FirebaseDatabase.getInstance().getReference().child("Notification").child(FirebaseAuth.getInstance().getUid())
                .child(notification.getNotificationId()).updateChildren(map);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgProfile;
        TextView txtTitle, txtBody, txtTime;
        LinearLayout parent;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.img_profile);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtBody = itemView.findViewById(R.id.txt_body);
            txtTime = itemView.findViewById(R.id.txt_time);
            parent = itemView.findViewById(R.id.parent);
        }
    }
}
