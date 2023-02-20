package com.sushant.fashionapp.Buyer;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.sushant.fashionapp.Models.FcmNotificationsSender;
import com.sushant.fashionapp.Models.Message;
import com.sushant.fashionapp.Models.NotificationModel;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.databinding.ActivityChatBinding;

import java.io.IOException;
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
    ValueEventListener messageListener, eventListener, tokenListener;
    DatabaseReference messageRef, statusRef, infoConnected, tokenRef;
    int pos, numItems;
    LinearLayoutManager layoutManager;
    String userToken;
    boolean notify = false;
    String destinationId, destination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.gray_100));

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        from = getIntent().getStringExtra("from");
        senderId = getIntent().getStringExtra("senderId");
        receiverId = getIntent().getStringExtra("receiverId");
        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        binding.imgStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (from.equals("Buyer")) {
                    Intent intent = new Intent(getApplicationContext(), StorePageActivity.class);
                    intent.putExtra("storeId", receiverId);
                    intent.putExtra("storePic", receiverPic);
                    intent.putExtra("storeName", receiverName);
                    startActivity(intent);
                }
            }
        });

        binding.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.item_store) {
                    Intent intent = new Intent(getApplicationContext(), StorePageActivity.class);
                    intent.putExtra("storeId", receiverId);
                    intent.putExtra("storePic", receiverPic);
                    intent.putExtra("storeName", receiverName);
                    startActivity(intent);
                }
                return false;
            }
        });


        String to;
        if (from.equals("Buyer")) {
            to = "Users";
        } else {
            to = "Store";
            binding.toolbar.getMenu().removeItem(R.id.item_store);
        }
        statusRef = database.getReference().child(to).child(senderId).child("Connection");
        manageConnection();

        if (from.equals("Buyer")) {
            database.getReference().child("Store").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Store store = snapshot.getValue(Store.class);
                    receiverName = store.getStoreName();
                    receiverPic = store.getStorePic();
                    binding.txtStoreName.setText(receiverName);
                    Glide.with(getApplicationContext()).load(receiverPic).placeholder(R.drawable.ic_clarity_store_line).into(binding.imgStore);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            database.getReference().child("Users").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Buyer buyer1 = snapshot.getValue(Buyer.class);
                    receiverName = buyer1.getUserName();
                    receiverPic = buyer1.getUserPic();
                    binding.txtStoreName.setText(receiverName);
                    Glide.with(getApplicationContext()).load(receiverPic).placeholder(R.drawable.ic_clarity_store_line).into(binding.imgStore);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


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
        final MessageAdapter adapter = new MessageAdapter(this, messages, senderId);
        binding.recyclerMessage.setAdapter(adapter);

        messageRef = database.getReference().child("Messages").child(senderRoom);
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Message message = snapshot1.getValue(Message.class);
                    message.setProfilePic(receiverPic);
                    messages.add(message);
                }
                int count = messages.size();
                if (count > 0) {
                    adapter.notifyItemInserted(count);
                    binding.recyclerMessage.smoothScrollToPosition(messages.size());
                }
//                else {
//                    pos = layoutManager.findLastCompletelyVisibleItemPosition();
//                    numItems = Objects.requireNonNull(binding.recyclerMessage.getAdapter()).getItemCount();
//                    if (pos >= numItems - 1) {
//                        binding.recyclerMessage.smoothScrollToPosition(messages.size());
//                    }
//                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        messageRef.addValueEventListener(messageListener);


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
                    binding.linearInterface.setVisibility(View.GONE);
                } else {
                    binding.imgSend.setVisibility(View.GONE);
                    binding.linearInterface.setVisibility(View.VISIBLE);
                }

            }
        });



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
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a");
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastActive), ZoneId.systemDefault());
                binding.txtLastMessage.setText(MessageFormat.format("Last Active: {0}", formatter.format(dateTime)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child(destination).child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (destination.equals("Users")) {
                    destinationId = snapshot.child("userId").getValue(String.class);
                } else {
                    destinationId = snapshot.child("buyerId").getValue(String.class);
                }

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
            notify = true;
            binding.recyclerMessage.smoothScrollToPosition(messages.size());
            final Message model = new Message();
            model.setMessage(message);
            Date date = new Date();
            model.setTimestamp(date.getTime());
            model.setType("text");
            model.setMessageId(key);
            model.setSenderId(senderId);
            model.setReceiverId(receiverId);
            binding.editMessage.getText().clear();

            if (notify) {
                sendNotification(senderName, message, senderPic, "text");
            }
            notify = false;


            database.getReference().child("Messages").child(senderRoom).child(key).setValue(model)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("Messages").child(receiverRoom).child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    NotificationModel notificationModel = new NotificationModel();
                                    notificationModel.setTitle(senderName);
                                    notificationModel.setBody(message);
                                    notificationModel.setTime(date.getTime());
                                    notificationModel.setInteracted(false);
                                    notificationModel.setType("message");
                                    notificationModel.setReceiverId(senderId);
                                    notificationModel.setNotificationId(key);
                                    notificationModel.setImageProfile(senderPic);
                                    database.getReference().child("Notification").child(receiverId).child(key).setValue(notificationModel);
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


    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void sendNotification(String title, String message, String image, String msgType) {
        tokenRef = database.getReference().child("Users").child(destinationId).child("Token");
        tokenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userToken = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        tokenRef.addValueEventListener(tokenListener);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String destination = from.equals("Buyer") ? "Store" : "Buyer";
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, title, message, image, receiverId, senderId, msgType, destination,
                        "Chat", ".ChatActivity", ChatActivity.this);
                try {
                    fcmNotificationsSender.sendNotifications();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }


}