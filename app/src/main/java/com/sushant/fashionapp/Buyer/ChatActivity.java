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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.MessageAdapter;
import com.sushant.fashionapp.Models.Buyer;
import com.sushant.fashionapp.Models.ChatModel;
import com.sushant.fashionapp.Models.Message;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String senderId, receiverId, senderRoom, receiverRoom, receiverPic, receiverName;
    ArrayList<Message> messages = new ArrayList<>();
    String from;
    ValueEventListener messageListener;
    DatabaseReference messageRef;
    int pos, numItems;
    LinearLayoutManager layoutManager;
    String userPic, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.gray_100));

        from = getIntent().getStringExtra("from");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        senderId = getIntent().getStringExtra("senderId");
        receiverId = getIntent().getStringExtra("receiverId");
        receiverPic = getIntent().getStringExtra("pic");
        receiverName = getIntent().getStringExtra("receiverName");
        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        binding.txtStoreName.setText(receiverName);
        Glide.with(getApplicationContext()).load(receiverPic).placeholder(R.drawable.ic_clarity_store_line).into(binding.imgStore);

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.recyclerMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy != 0) {
                    binding.fabChat.setVisibility(View.VISIBLE);
                }
                pos = layoutManager.findLastCompletelyVisibleItemPosition();
                numItems = Objects.requireNonNull(binding.recyclerMessage.getAdapter()).getItemCount();
                if (pos >= numItems - 5) {
                    binding.fabChat.setVisibility(View.GONE);
                }
            }
        });

        binding.fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutManager.scrollToPosition(messages.size() - 1);
            }
        });


        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerMessage.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);
        final MessageAdapter adapter = new MessageAdapter(this, messages, receiverPic, senderId);
        binding.recyclerMessage.setAdapter(adapter);

        messageRef = database.getReference().child("Messages").child(senderRoom);
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Message message = snapshot1.getValue(Message.class);
                    messages.add(message);
                }
                int count = messages.size();
                if (count > 0) {
                    adapter.notifyDataSetChanged();
                } else {
                    pos = layoutManager.findLastCompletelyVisibleItemPosition();
                    numItems = Objects.requireNonNull(binding.recyclerMessage.getAdapter()).getItemCount();
                    if (pos >= numItems - 2) {
                        binding.recyclerMessage.smoothScrollToPosition(messages.size());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        messageRef.addValueEventListener(messageListener);


        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Buyer buyer = snapshot.getValue(Buyer.class);
                userName = buyer.getUserName();
                if (buyer.getUserPic() != null) {
                    userPic = buyer.getUserPic();
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
                createBuyer();
                createSeller();
            }
        });


    }

    private void createSeller() {
        database.getReference().child("Chats").child("Buyer").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    database.getReference().child("Store").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Store store = snapshot.getValue(Store.class);
                            ChatModel chatModel = new ChatModel();
                            chatModel.setId(receiverId);
                            chatModel.setTimestamp(new Date().getTime());
                            chatModel.setName(store.getStoreName());
                            if (receiverPic != null) {
                                chatModel.setPic(receiverPic);
                            }
                            database.getReference().child("Chats").child("Buyer").child(senderId).child(receiverId).setValue(chatModel);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void createBuyer() {
        database.getReference().child("Chats").child("Store").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    ChatModel buyer = new ChatModel();
                    buyer.setId(auth.getUid());
                    buyer.setTimestamp(new Date().getTime());
                    buyer.setName(userName);
                    if (userPic != null) {
                        buyer.setPic(userPic);
                    }
                    database.getReference().child("Chats").child("Store").child(receiverId).child(auth.getUid()).setValue(buyer);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
            model.setuId(senderId);
            binding.editMessage.getText().clear();

            database.getReference().child("Messages").child(senderRoom).child(key).setValue(model)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("Messages").child(receiverRoom).child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    @Override
    protected void onDestroy() {
        if (messageRef != null) {
            messageRef.removeEventListener(messageListener);
        }
        super.onDestroy();
    }
}