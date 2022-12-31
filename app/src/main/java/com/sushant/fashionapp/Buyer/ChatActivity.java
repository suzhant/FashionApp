package com.sushant.fashionapp.Buyer;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.ChatModel;
import com.sushant.fashionapp.Models.Message;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String senderId, receiverId, senderRoom, receiverRoom;
    ArrayList<Message> messages = new ArrayList<>();
    String from, storePic = null, storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.gray_100));

        from = getIntent().getStringExtra("from");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        senderId = auth.getUid();
        receiverId = getIntent().getStringExtra("storeId");
        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              onBackPressed();
            }
        });


        database.getReference().child("Store").child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Store store = snapshot.getValue(Store.class);
                assert store != null;
                if (store.getStorePic() != null) {
                    storePic = store.getStorePic();
                }
                storeName = store.getStoreName();
                binding.txtStoreName.setText(storeName);
                Glide.with(getApplicationContext()).load(storePic).placeholder(R.drawable.ic_clarity_store_line).into(binding.imgStore);

                database.getReference().child("Chats").child(from).child(auth.getUid()).child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            ChatModel chatModel = new ChatModel();
                            chatModel.setStoreId(receiverId);
                            chatModel.setTimestamp(new Date().getTime());
                            chatModel.setStoreName(storeName);
                            if (storePic != null) {
                                chatModel.setStorePic(storePic);
                            }
                            database.getReference().child("Chats").child(from).child(auth.getUid()).child(receiverId).setValue(chatModel);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        database.getReference().child("Messages").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Message message = snapshot1.getValue(Message.class);
                    messages.add(message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    binding.imgSend.setVisibility(View.VISIBLE);
                } else {
                    binding.imgSend.setVisibility(View.GONE);
                }

            }
        });

        binding.imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


    }

    private void sendMessage() {
        final String message = binding.editMessage.getText().toString();
        String key = database.getReference().push().getKey();
        assert key != null;
        if (!message.isEmpty()) {
            binding.recyclerMessage.smoothScrollToPosition(messages.size());
            final Message model = new Message();
            model.setMessage(message);
            Date date = new Date();
            model.setTimestamp(date.getTime());
            model.setType("text");
            model.setMessageId(key);
            model.setuId(auth.getUid());
            binding.editMessage.getText().clear();

            database.getReference().child("Messages").child(senderRoom).child(key).setValue(model)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("Messages").child(receiverRoom).child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    String path = "android.resource://" + getPackageName() + "/" + R.raw.google_notification;
                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
                                    r.play();
                                }
                            });
                        }
                    });


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}