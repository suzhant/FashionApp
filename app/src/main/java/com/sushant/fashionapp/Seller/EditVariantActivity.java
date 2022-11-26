package com.sushant.fashionapp.Seller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sushant.fashionapp.Adapters.EditSizeAdapter;
import com.sushant.fashionapp.Adapters.VariantPhotoAdapter;
import com.sushant.fashionapp.Models.Size;
import com.sushant.fashionapp.Models.Variants;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.Utils.ImageUtils;
import com.sushant.fashionapp.databinding.ActivityEditVariantBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class EditVariantActivity extends AppCompatActivity {

    ActivityEditVariantBinding binding;
    String pId;
    int variantIndex;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<Size> sizes = new ArrayList<>();
    ArrayList<Variants> variant = new ArrayList<>();
    ArrayList<String> photos = new ArrayList<>();
    ArrayList<String> tempPic = new ArrayList<>();
    ArrayList<String> upLoadPic = new ArrayList<>();
    EditSizeAdapter adapter;
    VariantPhotoAdapter photoAdapter;
    ActivityResultLauncher<Intent> imgLauncher;
    String color;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditVariantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        pId = getIntent().getStringExtra("pId");
        variantIndex = getIntent().getIntExtra("variantIndex", 0);
        photos = getIntent().getStringArrayListExtra("photos");
        color = getIntent().getStringExtra("color");
        sizes = (ArrayList<Size>) getIntent().getSerializableExtra("sizes");
        variant = (ArrayList<Variants>) getIntent().getSerializableExtra("origVariant");

        binding.edColorName.setText(color);
        tempPic.addAll(photos);

        binding.cardUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                imgLauncher.launch(intent);
            }
        });

        binding.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateVariant();
            }
        });
        initRecycler();
        initPhotoRecycler();

        imgLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getClipData() != null) {
                                ClipData clipData = result.getData().getClipData();

                                int count = clipData.getItemCount();
                                if (count > 5) {
                                    Toast.makeText(EditVariantActivity.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                for (int i = 0; i < count; i++) {
                                    Uri imageUrl = clipData.getItemAt(i).getUri();

                                    if (upLoadPic.size() > 4) {
                                        Toast.makeText(EditVariantActivity.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    upLoadPic.add(String.valueOf(imageUrl));
                                    photos.add(String.valueOf(imageUrl));
                                    photoAdapter.notifyItemInserted(photos.size());
                                }

                            } else if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                if (upLoadPic.size() > 4) {
                                    Toast.makeText(EditVariantActivity.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                upLoadPic.add(String.valueOf(selectedImage));
                                photos.add(String.valueOf(selectedImage));
                                photoAdapter.notifyItemInserted(photos.size());
                            }
                        }
                    }
                });

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.cardAddSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

    }

    private void openDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_size);
        MaterialButton btnSave = bottomSheetDialog.findViewById(R.id.btnSave);
        AutoCompleteTextView autoSize = bottomSheetDialog.findViewById(R.id.autoSize);
        TextInputEditText edStock = bottomSheetDialog.findViewById(R.id.edStock);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();

        String[] sizeList = getResources().getStringArray(R.array.size);
        autoSize.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, sizeList));


        assert btnSave != null;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Size product = new Size();
                String size = autoSize.getText().toString();
                String stock = edStock.getText().toString();
                product.setSize(size);
                product.setStock(Integer.valueOf(stock));
                sizes.add(product);
                adapter.notifyItemInserted(sizes.size());
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void updateVariant() {
        if (binding.edColorName.getText().toString().isEmpty() | sizes.isEmpty() | photos.isEmpty()) {
            Toast.makeText(this, "Please complete the form", Toast.LENGTH_SHORT).show();
            return;
        }
        confirmDialog();
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerSize.setLayoutManager(layoutManager);
        adapter = new EditSizeAdapter(sizes, this);
        binding.recyclerSize.setAdapter(adapter);
    }

    private void initPhotoRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerPhoto.setLayoutManager(layoutManager);
        photoAdapter = new VariantPhotoAdapter(photos, this, 1);
        binding.recyclerPhoto.setAdapter(photoAdapter);
    }


    private void confirmDialog() {
        if (CheckConnection.isOnline(this)) {
            new MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme)
                    .setMessage("Are you sure?")
                    .setTitle("Confirmation")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!upLoadPic.isEmpty()) {
                                createImageBitmap(sizes);
                            } else {
                                String color = binding.edColorName.getText().toString();
                                Variants product = new Variants();
                                product.setColor(color);
                                product.setSizes(sizes);
                                product.setPhotos(photos);
                                variant.set(variantIndex, product);
                                Intent intent = new Intent(EditVariantActivity.this, EditProductDetailsActivity.class);
                                intent.putExtra("pId", pId);
                                intent.putExtra("origVariant", variant);
                                intent.putExtra("variantIndex", variantIndex);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        } else {
            CheckConnection.showCustomDialog(this);
        }
    }

    private void createImageBitmap(ArrayList<Size> sizes) {
        ArrayList<byte[]> images = new ArrayList<>();
        for (String image : upLoadPic) {
            Bitmap bitmap = null;
            try {
                bitmap = ImageUtils.handleSamplingAndRotationBitmap(this, Uri.parse(image));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            images.add(bytes);
        }
        if (images.size() == upLoadPic.size()) {
            uploadMultipleImageToFirebase(images, sizes);
        }
    }


    private void uploadMultipleImageToFirebase(ArrayList<byte[]> imgList, ArrayList<Size> sizes) {
        ArrayList<String> finalImageList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        for (int i = 0; i < imgList.size(); i++) {
            final StorageReference reference = storage.getReference().child("Seller").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + i + "");
            UploadTask uploadTask = reference.putBytes(imgList.get(i));
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.P)
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @RequiresApi(api = Build.VERSION_CODES.P)
                            @Override
                            public void onSuccess(Uri uri) {
                                String filePath = uri.toString();
                                finalImageList.add(filePath);
                                if (finalImageList.size() == imgList.size()) {
                                    dialog.dismiss();
                                    photos.clear();
                                    photos.addAll(tempPic);
                                    photos.addAll(finalImageList);
                                    String color = binding.edColorName.getText().toString();
                                    Variants product = new Variants();
                                    product.setColor(color);
                                    product.setSizes(sizes);
                                    product.setPhotos(photos);
                                    variant.set(variantIndex, product);
                                    Intent intent = new Intent(EditVariantActivity.this, EditProductDetailsActivity.class);
                                    intent.putExtra("pId", pId);
                                    intent.putExtra("origVariant", variant);
                                    intent.putExtra("variantIndex", variantIndex);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                }
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }
    }

}