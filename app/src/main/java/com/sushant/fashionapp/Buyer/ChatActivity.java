package com.sushant.fashionapp.Buyer;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Adapters.MessageAdapter;
import com.sushant.fashionapp.Models.Buyer;
import com.sushant.fashionapp.Models.ChatModel;
import com.sushant.fashionapp.Models.Message;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityChatBinding;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity implements DefaultLifecycleObserver {

    ActivityChatBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String senderId, receiverId, senderRoom, receiverRoom, receiverPic, receiverName, senderName, senderPic;
    ArrayList<Message> messages = new ArrayList<>();
    String from;
    ValueEventListener messageListener, eventListener;
    DatabaseReference messageRef, statusRef, infoConnected;
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

        String to;
        if (from.equals("Buyer")) {
            to = "Users";
        } else {
            to = "Store";
        }
        statusRef = database.getReference().child(to).child(senderId).child("Connection");
        manageConnection();

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

        String destination;
        if (from.equals("Buyer")) {
            destination = "Store";
        } else {
            destination = "Users";
        }
        DatabaseReference ref = database.getReference().child(destination).child(receiverId).child("Connection");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastActive = snapshot.child("lastOnline").getValue(Long.class);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastActive), ZoneId.systemDefault());
                binding.txtLastMessage.setText(MessageFormat.format("Last Active: {0}", formatter.format(dateTime)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        String buyer;
        if (from.equals("Buyer")) {
            buyer = "Users";
            database.getReference().child(buyer).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Buyer buyer1 = snapshot.getValue(Buyer.class);
                    senderName = buyer1.getUserName();
                    senderPic = buyer1.getUserPic();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            buyer = "Store";
            database.getReference().child(buyer).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Store store = snapshot.getValue(Store.class);
                    senderName = store.getStoreName();
                    senderPic = store.getStorePic();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        binding.imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBuyer();
                createSeller();
                sendMessage();
            }
        });


    }

    private void createSeller() {
        database.getReference().child("Chats").child(from).child(senderId).child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    ChatModel chatModel = new ChatModel();
                    chatModel.setId(receiverId);
                    database.getReference().child("Chats").child(from).child(senderId).child(receiverId).setValue(chatModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void createBuyer() {
        String to;
        if (from.equals("Buyer")) {
            to = "Store";
        } else {
            to = "Buyer";
        }
        database.getReference().child("Chats").child(to).child(receiverId).child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    ChatModel buyer = new ChatModel();
                    buyer.setId(senderId);
                    database.getReference().child("Chats").child(to).child(receiverId).child(senderId).setValue(buyer);
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

    private void manageConnection() {
        final DatabaseReference status = statusRef.child("Status");
        final DatabaseReference lastOnlineRef = statusRef.child("lastOnline");
        infoConnected = database.getReference(".info/connected");

        eventListener = infoConnected.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                assert connected != null;
                if (connected) {
                    status.setValue("online");
                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                } else {
                    status.onDisconnect().setValue("offline");
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        // app moved to foreground
        if (auth.getCurrentUser() != null) {
            updateStatus("online");

        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        // app moved to background
        if (auth.getCurrentUser() != null) {
            updateStatus("offline");
        }
    }

    void updateStatus(String status) {
        HashMap<String, Object> obj = new HashMap<>();
        obj.put("Status", status);
        statusRef.updateChildren(obj);
    }
}