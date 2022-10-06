package com.sushant.fashionapp.Buyer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sushant.fashionapp.Models.Users;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.ImageUtils;
import com.sushant.fashionapp.databinding.ActivityBuyerProfileBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class BuyerProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    ActivityBuyerProfileBinding binding;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog dialog;
    String buyerPic, buyerId, buyerName;
    ActivityResultLauncher<Intent> imageLauncher;
    DatabaseReference userRef;
    ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuyerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);

        userRef = database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()));
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users buyer = snapshot.getValue(Users.class);
                assert buyer != null;
                buyerId = buyer.getUserId();
                buyerName = buyer.getUserName();

                binding.txtName.setText(buyerName);

                if (snapshot.child("userPic").exists()) {
                    buyerPic = snapshot.child("userPic").getValue(String.class);
                    Glide.with(getApplicationContext()).load(buyerPic)
                            .placeholder(R.drawable.avatar)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.imgChangePhoto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userRef.addValueEventListener(userListener);


        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.imgChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetPhotoDialog();
            }
        });

        binding.txtChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetPhotoDialog();
            }
        });

        imageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                createImageBitmap(selectedImage);
                            }
                        }
                    }
                });

        binding.linearChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetNameDialog();
            }
        });

    }

    private void showBottomSheetNameDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_name);
        MaterialButton btnSave = bottomSheetDialog.findViewById(R.id.btnSave);
        TextInputEditText edFirstName = bottomSheetDialog.findViewById(R.id.edFirstName);
        TextInputEditText edLastName = bottomSheetDialog.findViewById(R.id.edLastName);
        LinearLayout parent = bottomSheetDialog.findViewById(R.id.nameParent);
        //putting bottomsheetdialog above keyboard
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        assert btnSave != null;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setTitle("Changing Name");
                dialog.setMessage("Please wait...");
                dialog.show();
                String firstName = edFirstName.getText().toString();
                String lastName = edLastName.getText().toString();
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    Snackbar.make(parent, "Please fill both fields", Snackbar.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                HashMap<String, Object> name = new HashMap<>();
                name.put("userName", firstName + " " + lastName);
                database.getReference().child("Users").child(buyerId).updateChildren(name).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        bottomSheetDialog.dismiss();
                        dialog.dismiss();
                        Snackbar.make(binding.getRoot(), "Name Changed", Snackbar.LENGTH_SHORT).show();
                    }
                });

            }
        });
        bottomSheetDialog.show();
    }

    private void showBottomSheetPhotoDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_photo);
        MaterialButton btnTakePhoto = bottomSheetDialog.findViewById(R.id.btnTakePhoto);
        MaterialButton btnUploadFromGallery = bottomSheetDialog.findViewById(R.id.btnUploadFromGallery);
        assert btnTakePhoto != null;
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(BuyerProfileActivity.this)
                        .cameraOnly()
                        .crop()
                        .start(REQUEST_IMAGE_CAPTURE);
                bottomSheetDialog.dismiss();
            }
        });
        assert btnUploadFromGallery != null;

        btnUploadFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                imageLauncher.launch(intent);
            }
        });

        bottomSheetDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (data.getData() != null) {
                        Uri selectedImage = data.getData();
                        createImageBitmap(selectedImage);
                    }
                }
            }
        }
    }

    private void createImageBitmap(Uri imageUrl) {
        Bitmap bitmap = null;
        try {
            bitmap = ImageUtils.handleSamplingAndRotationBitmap(BuyerProfileActivity.this, imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        int length = bytes.length / 1024;
        uploadImageToFirebase(bytes, length);
    }

    private void uploadImageToFirebase(byte[] uri, int length) {
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Profile Images").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        if (length > 256) {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        dialog.setProgress(0);
        dialog.setMessage("Please wait");
        dialog.show();
        UploadTask uploadTask = reference.putBytes(uri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {
                    dialog.dismiss();
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();
                            HashMap<String, Object> image = new HashMap<>();
                            image.put("userPic", filePath);
                            database.getReference().child("Users").child(buyerId).updateChildren(image);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //only works if image size is greater than 256kb!
                if (length > 256) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    int currentProgress = (int) progress;
                    dialog.setProgress(currentProgress);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null) {
            userRef.removeEventListener(userListener);
        }
    }
}