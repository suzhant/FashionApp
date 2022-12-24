package com.sushant.fashionapp.Buyer;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.khalti.checkout.helper.Config;
import com.khalti.checkout.helper.KhaltiCheckOut;
import com.khalti.checkout.helper.OnCheckOutListener;
import com.khalti.checkout.helper.PaymentPreference;
import com.sushant.fashionapp.Adapters.ShopAdapter;
import com.sushant.fashionapp.Inteface.ItemClickListener;
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.Models.Buyer;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Delivery;
import com.sushant.fashionapp.Models.Order;
import com.sushant.fashionapp.Models.Status;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.PdfGenerator;
import com.sushant.fashionapp.databinding.ActivityPaymentBinding;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class PaymentActivity extends AppCompatActivity {

    ActivityPaymentBinding binding;
    ShopAdapter adapter;
    ArrayList<Store> stores = new ArrayList<>();
    ArrayList<String> storeIdList = new ArrayList<>();
    ArrayList<Store> selectedStoreList = new ArrayList<>();
    ArrayList<Store> finalStoreList = new ArrayList<>();
    ArrayList<Delivery> products = new ArrayList<>();
    FirebaseAuth auth;
    FirebaseDatabase database;
    boolean selectKhalti = false, selectCash = false;
    long totalPrice;
    ItemClickListener itemClickListener;
    Store store;
    Address address;
    ProgressDialog dialog;
    String email, name, invoiceNo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View view = LayoutInflater.from(this).inflate(R.layout.custom_khalti_button, binding.parent, false);
        binding.khaltiButton.setCustomView(view);

        storeIdList = getIntent().getStringArrayListExtra("storeInfo");
        totalPrice = getIntent().getLongExtra("totalPrice", 0);
        address = (Address) getIntent().getSerializableExtra("addressInfo");

        //  Backendless.initApp(this, Backendless.getApplicationIdOrDomain(), Backendless.getApiKey());


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we place your order.");
        dialog.setCancelable(false);


        binding.txtPrice.setText(MessageFormat.format("Total: {0}", totalPrice));

        itemClickListener = new ItemClickListener() {
            @Override
            public void onClick(String item, String type) {

            }

            @Override
            public <T> void onAddressClick(T Object, boolean b) {
                store = (Store) Object;
                int pos = selectedStoreList.indexOf(store);
                if (b) {
                    totalPrice = totalPrice - 70;
                    finalStoreList.get(pos).setDeliveryCharge(0);
                    finalStoreList.get(pos).setSelfPickUp(true);
                } else {
                    totalPrice = totalPrice + 70;
                    finalStoreList.get(pos).setDeliveryCharge(70);
                    finalStoreList.get(pos).setSelfPickUp(false);
                }

                binding.txtPrice.setText(MessageFormat.format("Total: {0}", totalPrice));
            }

        };


        binding.statusLyt.radio1.setImageResource(R.drawable.ic_baseline_check_circle_24);
        binding.statusLyt.radio1.setImageTintList(ContextCompat.getColorStateList(this, R.color.black));
        binding.statusLyt.txt1.setTextColor(ContextCompat.getColor(this, R.color.black));
        binding.statusLyt.radio2.setImageTintList(ContextCompat.getColorStateList(this, R.color.teal_700));
        binding.statusLyt.txt2.setTextColor(ContextCompat.getColor(this, R.color.teal_700));

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        database.getReference().child("Store").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Store store = snapshot1.getValue(Store.class);
                    stores.add(store);
                }

                for (String storeId : storeIdList) {
                    for (Store s : stores) {
                        if (storeId.equals(s.getStoreId())) {
                            s.setDeliveryCharge(70);
                            selectedStoreList.add(s);
                            break;
                        }
                    }
                }

                finalStoreList.clear();
                for (Store store : selectedStoreList) {
                    Store store1 = new Store();
                    store1.setStoreId(store.getStoreId());
                    store1.setDeliveryCharge(70);
                    store1.setSelfPickUp(false);
                    finalStoreList.add(store1);
                }
                adapter.notifyItemInserted(selectedStoreList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Cart").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Cart p = snapshot1.getValue(Cart.class);

                    Delivery product = new Delivery();
                    product.setpId(p.getpId());
                    product.setpName(p.getpName());
                    product.setDeliveryStatus(Status.PENDING.name());
                    if (p.getBargainPrice() != null) {
                        product.setBargainPrice(p.getBargainPrice());
                    }
                    product.setpPic(p.getpPic());
                    product.setpPrice(p.getpPrice());
                    product.setSize(p.getSize());
                    product.setColor(p.getColor());
                    product.setStoreId(p.getStoreId());
                    product.setQuantity(p.getQuantity());
                    products.add(product);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Buyer buyer = snapshot.getValue(Buyer.class);
                email = buyer.getUserEmail();
                name = buyer.getUserName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.cardKhalti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectKhalti = !selectKhalti;
                if (selectKhalti) {
                    //       binding.cardKhalti.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.skyBlue));
                    binding.imgKhaltiCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.skyBlue));
                    selectCash = false;
                } else {
                    //     binding.cardKhalti.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.medium_gray));
                    binding.imgKhaltiCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), com.khalti.R.color.disabled));
                }
                if (!selectCash) {
                    //     binding.cardCashOnDelivery.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.medium_gray));
                    binding.imgCashCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), com.khalti.R.color.disabled));
                }
            }
        });

        binding.cardCashOnDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCash = !selectCash;
                if (selectCash) {
                    //   binding.cardCashOnDelivery.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.skyBlue));
                    binding.imgCashCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.skyBlue));
                    selectKhalti = false;
                } else {
                    //    binding.cardCashOnDelivery.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.medium_gray));
                    binding.imgCashCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), com.khalti.R.color.disabled));
                }
                if (!selectKhalti) {
                    //     binding.cardKhalti.setStrokeColor(ContextCompat.getColor(getApplicationContext(),R.color.medium_gray));
                    binding.imgKhaltiCheck.setImageTintList(ContextCompat.getColorStateList(getApplicationContext(), com.khalti.R.color.disabled));
                }
            }
        });

        binding.khaltiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectKhalti && !selectCash) {
                    Snackbar.make(findViewById(R.id.parent), "Select Payment method", Snackbar.LENGTH_SHORT).setAnchorView(R.id.linear).show();
                    return;
                }
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    Dexter.withContext(getApplicationContext())
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    if (selectKhalti) {
                                        //  binding.khaltiButton.setOnClickListener(this);
                                        integrateKhalti();
                                    }

                                    if (selectCash) {
                                        integrateCashOnDelivery();
                                    }
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(getApplicationContext(), "Please accept permissions", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                            }).check();
                }
                if (selectKhalti) {
                    //  binding.khaltiButton.setOnClickListener(this);
                    integrateKhalti();
                }

                if (selectCash) {
                    integrateCashOnDelivery();
                }
            }
        });

        initRecycler();
    }

    private void integrateCashOnDelivery() {
        // String orderId = database.getReference().child("Order").push().getKey();
        dialog.show();
        String orderId = String.valueOf(new Date().getTime());
        Order order = new Order();
        order.setOrderId(orderId);
        order.setBuyerId(auth.getUid());
        order.setAmount(totalPrice);
        order.setProducts(products);
        order.setOrderDate(new Date().getTime());
        order.setAddressId(address.getAddressId());
        order.setStores(finalStoreList);
        order.setPaid(false);
        order.setOrderStatus(Status.PENDING.name());
        order.setPaymentMethod("Cash on Delivery");


        database.getReference().child("Orders").child(auth.getUid()).child(orderId).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Cart").child(auth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
//                        String recipient = email;
//                        String mailBody = "Your order for has been confirmed.";
//
//                        Backendless.Messaging.sendHTMLEmail("Order Confirmation", mailBody, recipient, new AsyncCallback<MessageStatus>() {
//                            @Override
//                            public void handleResponse(MessageStatus response) {
//                                Log.d("backendstatus",response.getStatus().name());
//
//                            }
//
//                            @Override
//                            public void handleFault(BackendlessFault fault) {
//                                Log.d("fault",fault.getMessage());
//                            }
//                        });
//
//                        Set<String> addresses = new HashSet<>();
//                        addresses.add(email);
//
//                        Map<String, String> templateValues = new HashMap<>();
//                        templateValues.put("customerName",name );
//                        templateValues.put("orderId", orderId);
//
//
//                        EmailEnvelope envelope = new EmailEnvelope();
//                        envelope.setTo(addresses);
//
//                        Backendless.Messaging.sendEmailFromTemplate("Order Confirmation", envelope, templateValues, new AsyncCallback<MessageStatus>() {
//                            @Override
//                            public void handleResponse(MessageStatus response) {
//                                Log.i("template", "Email has been sent");
//                            }
//
//                            @Override
//                            public void handleFault(BackendlessFault fault) {
//                                Log.e("template", fault.getMessage());
//                            }
//                        });

                        long date = new Date().getTime();
                        createInvoiceNo(date, order);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void integrateKhalti() {
        //  String orderId = database.getReference().child("Order").push().getKey();
        String orderId = String.valueOf(new Date().getTime());

        Config.Builder builder = new Config.Builder(getString(R.string.khalti_test_pub), orderId, "product", totalPrice, new OnCheckOutListener() {
            @Override
            public void onError(@NonNull String action, @NonNull Map<String, String> errorMap) {
                Log.i(action, errorMap.toString());
                Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(@NonNull Map<String, Object> data) {
                dialog.show();
                Order order = new Order();
                order.setOrderId(orderId);
                order.setBuyerId(auth.getUid());
                order.setAmount(totalPrice);
                order.setToken(data.get("token").toString());
                order.setProducts(products);
                order.setOrderDate(new Date().getTime());
                order.setAddressId(address.getAddressId());
                order.setStores(finalStoreList);
                order.setPaid(true);
                order.setOrderStatus(Status.PENDING.name());
                order.setPaymentMethod("Khalti Wallet");
                database.getReference().child("Orders").child(auth.getUid()).child(orderId).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Cart").child(auth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                long date = new Date().getTime();
                                createInvoiceNo(date, order);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(PaymentActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        }).paymentPreferences(new ArrayList<PaymentPreference>() {{
            add(PaymentPreference.KHALTI);
        }});
        Config config = builder.build();
        binding.khaltiButton.setCheckOutConfig(config);
        KhaltiCheckOut khaltiCheckOut = new KhaltiCheckOut(this, config);
        khaltiCheckOut.show();
    }

    private void initRecycler() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerShops.setLayoutManager(linearLayoutManager);
        adapter = new ShopAdapter(selectedStoreList, this, itemClickListener);
        binding.recyclerShops.setAdapter(adapter);
    }

    private void implementJavaMail(String receiverMail, String orderId, File file) {

        try {
            String stringSenderEmail = "sushantshrestha62@gmail.com";

            String stringHost = "smtp.gmail.com";

            Properties properties = System.getProperties();

            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(stringSenderEmail, getString(R.string.mail_secret));
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress("noreply@support.fashionApp.com.np", "Fashion App"));
            mimeMessage.setHeader("Disposition-Notification-To", "noreply@support.fashionApp.com.np");
            mimeMessage.setReplyTo(InternetAddress.parse("noreply@support.fashionApp.com.np", false));
            mimeMessage.setSentDate(new Date());

            //single recipient
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverMail));

            //multiple recipients
//            javax.mail.Address[] recipient = new javax.mail.Address[]{
//                    new InternetAddress("xresthasushant61@gmail.com"),
//                    new InternetAddress("sushantshrestha62@gmail.com")
//            };
//            mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.toString(recipient));
            mimeMessage.setSubject("Order Confirmation");


            // Create the message part
            MimeBodyPart messageBodyPart1 = new MimeBodyPart();
            //body
            messageBodyPart1.setText(MessageFormat.format("Dear {0},\n\nYour order for #{1} has been confirmed.\n\n\n\n\n\nSincerely,\nFashion development Team", name, orderId));

            //if you want to inline image in email
            //inlineImage(messageBodyPart);

            //getting pic from internet and attaching it in mail
//            MimeBodyPart imageBodyPart = new MimeBodyPart();
//            String filepath="";
//            URLDataSource bds = new URLDataSource(new URL(filepath));
//            imageBodyPart.setDataHandler(new DataHandler(bds));
//            imageBodyPart.setFileName("photo.jpg");

            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(file);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName("invoice.pdf");
            //       attachmentBodyPart.attachFile(file, "application/pdf", null);


            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart1);
            //    multipart.addBodyPart(imageBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            // Send the complete message parts
            mimeMessage.setContent(multipart);


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void inlineImage(BodyPart messageBodyPart, String filepath) throws MessagingException, MalformedURLException {
        String htmlText = "<H1>Hello</H1><img src=\"cid:image\">";
        messageBodyPart.setContent(htmlText, "text/html");

        MimeBodyPart imageBodyPart = new MimeBodyPart();
        URLDataSource bds = new URLDataSource(new URL(filepath));
        imageBodyPart.setDataHandler(new DataHandler(bds));
        imageBodyPart.setHeader("Content-ID", "<image>");
        imageBodyPart.setFileName("photo.jpg");
    }


    public void createInvoiceNo(long date, Order order) {
        database.getReference().child("Invoice").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DateTimeFormatter formatter;
                LocalDateTime dateTime;
                long count;
                if (snapshot.exists()) {
                    count = snapshot.getChildrenCount() + 1;
                } else {
                    count = 1;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault());
                    String formattedStr = String.format("%05d", count);
                    invoiceNo = dateTime.format(formatter) + "-" + formattedStr;
                    try {
                        PdfGenerator pdfGenerator = new PdfGenerator(PaymentActivity.this, order, address, email, invoiceNo, date);
                        File file = pdfGenerator.createInvoicePdf();
                        implementJavaMail(email, order.getOrderId(), file);
                        pdfGenerator.uploadInvoice(file, dialog);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}