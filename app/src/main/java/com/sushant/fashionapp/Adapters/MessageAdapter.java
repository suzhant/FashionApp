package com.sushant.fashionapp.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sushant.fashionapp.Models.Message;
import com.sushant.fashionapp.R;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    String pic, senderId;


    public MessageAdapter(Context context, ArrayList<Message> messages, String pic, String senderId) {
        this.context = context;
        this.messages = messages;
        this.pic = pic;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sender_bubble, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receiver_bubble, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder senderViewHolder = ((SenderViewHolder) holder);
            if ("text".equals(message.getType())) {
                senderViewHolder.txtSenderMsg.setText(message.getMessage());
            } else if ("image".equals(message.getType())) {
                senderViewHolder.frameSenderText.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(senderViewHolder.imgSender);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getTimestamp()), ZoneId.systemDefault());
            senderViewHolder.txtSenderTime.setText(formatter.format(dateTime));

        } else {
            //ReceiverViewHolder starts from here
            ReceiverViewHolder receiverViewHolder = ((ReceiverViewHolder) holder);
            if ("text".equals(message.getType())) {
                receiverViewHolder.txtReceiverMsg.setText(message.getMessage());
            } else if ("image".equals(message.getType())) {
                receiverViewHolder.frameReceiverText.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(com.denzcoskun.imageslider.R.drawable.loading).into(receiverViewHolder.imgReceiver);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getTimestamp()), ZoneId.systemDefault());
            receiverViewHolder.txtReceiverTime.setText(formatter.format(dateTime));
            Glide.with(context).load(pic).placeholder(R.drawable.avatar).into(receiverViewHolder.imgUser);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (Objects.equals(senderId, messages.get(position).getuId())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public static class SenderViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameSenderText;
        TextView txtSenderMsg, txtSenderTime;
        ImageView imgSender;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            frameSenderText = itemView.findViewById(R.id.frameLayout);
            txtSenderMsg = itemView.findViewById(R.id.txtSenderMsg);
            txtSenderTime = itemView.findViewById(R.id.txtSenderTime);
            imgSender = itemView.findViewById(R.id.imgSender);
        }
    }


    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameReceiverText;
        TextView txtReceiverMsg, txtReceiverTime;
        ImageView imgReceiver;
        CircleImageView imgUser;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            frameReceiverText = itemView.findViewById(R.id.frameLayout);
            txtReceiverMsg = itemView.findViewById(R.id.txtReceiverMsg);
            txtReceiverTime = itemView.findViewById(R.id.txtReceiverTime);
            imgReceiver = itemView.findViewById(R.id.imgReceiver);
            imgUser = itemView.findViewById(R.id.imgUser);
        }
    }

}
