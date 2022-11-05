package com.sushant.fashionapp.seller;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sushant.fashionapp.Adapters.SizeSummaryAdapter;
import com.sushant.fashionapp.Adapters.VariantPhotoAdapter;
import com.sushant.fashionapp.Adapters.VariantSummaryAdapter;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.Utils.ImageUtils;
import com.sushant.fashionapp.databinding.ActivityAddProductBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class ActivityAddProduct extends AppCompatActivity {

    ActivityAddProductBinding binding;
    VariantPhotoAdapter adapter;
    ArrayList<String> tempImages = new ArrayList<>();
    ActivityResultLauncher<Intent> imgLauncher;
    ArrayList<Product> variants = new ArrayList<>();

    VariantSummaryAdapter variantSummaryAdapter;
    SizeSummaryAdapter sizeSummaryAdapter;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String pName, cat, subCat, pDes, storeId, price, sellerId, storeName;
    FirebaseStorage storage;
    String size;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        binding.btnAddVariant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        variantSummaryAdapter = new VariantSummaryAdapter(variants, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        binding.recyclerVariantSummary.setLayoutManager(layoutManager);
        binding.recyclerVariantSummary.setAdapter(variantSummaryAdapter);

        //category List
        String[] catList = getResources().getStringArray(R.array.category);
        binding.autoCategory.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, catList));

        //subcategory List
        String[] subCatList = getResources().getStringArray(R.array.subCategory);
        binding.autoSubCategory.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, subCatList));

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
                                    Toast.makeText(ActivityAddProduct.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                for (int i = 0; i < count; i++) {
                                    Uri imageUrl = clipData.getItemAt(i).getUri();

                                    if (tempImages.size() > 4) {
                                        Toast.makeText(ActivityAddProduct.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    tempImages.add(String.valueOf(imageUrl));
                                    adapter.notifyItemInserted(tempImages.size());
                                }

                            } else if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                if (tempImages.size() > 4) {
                                    Toast.makeText(ActivityAddProduct.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                tempImages.add(String.valueOf(selectedImage));
                                adapter.notifyItemInserted(tempImages.size());
                            }
                        }
                    }
                });

        binding.btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pName = binding.edProductName.getText().toString();
                cat = binding.autoCategory.getText().toString();
                subCat = binding.autoSubCategory.getText().toString();
                pDes = binding.edDescription.getText().toString();
                price = binding.edPrice.getText().toString();
                if (pName.isEmpty() | cat.isEmpty() | subCat.isEmpty() | pDes.isEmpty() | variants.isEmpty()) {
                    Snackbar.make(binding.parent, "Please complete the form", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                addProductToDB();
            }
        });

        database.getReference().child("Store").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Store store = snapshot1.getValue(Store.class);
                    assert store != null;
                    if (store.getOwnerId().equals(auth.getUid())) {
                        storeId = store.getStoreId();
                        sellerId = store.getOwnerId();
                        storeName = store.getStoreName();
                        break;
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void addProductToDB() {
        confirmDialog();
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
                            String key = database.getReference().child("Products").push().getKey();
                            ProgressDialog dialog = new ProgressDialog(ActivityAddProduct.this);
                            dialog.setTitle("Uploading..");
                            dialog.setMessage("Please wait while we are adding your product");
                            dialog.setCancelable(false);

                            Product product1 = new Product(key, pName, storeName, variants.get(0).getPhotos().get(0));
                            product1.setLove(0);
                            product1.setpPrice(Integer.valueOf(price));
                            product1.setCategory(cat);
                            product1.setSubCategory(subCat);
                            product1.setDesc(pDes);
                            product1.setBrandName("Gucci");
                            product1.setProductCode(123);
                            product1.setVariants(variants);
                            HashMap<String, Object> store = new HashMap<>();
                            store.put("storeId", storeId);

                            dialog.show();
                            database.getReference().child("Products").child(product1.getpId()).setValue(product1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("Products").child(product1.getpId()).updateChildren(store);
                                    resetAllFields();
                                    dialog.dismiss();
                                }
                            });
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

    private void showDialog() {
        tempImages.clear();
        ArrayList<Product> sizes = new ArrayList<>();

        Dialog variantDialog = new Dialog(this);
        variantDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        variantDialog.setContentView(R.layout.add_variant_layout);
        TextInputEditText colorName = variantDialog.findViewById(R.id.edColorName);
        RecyclerView recyclerImage = variantDialog.findViewById(R.id.recyclerPhoto);
        RecyclerView recyclerSize = variantDialog.findViewById(R.id.recyclerSize);
        MaterialCardView cardUpload = variantDialog.findViewById(R.id.cardUpload);
        Button addVariant = variantDialog.findViewById(R.id.btnAddVariant);
        Button addSize = variantDialog.findViewById(R.id.btnAddSize);
        LinearLayout variantDialogLayout = variantDialog.findViewById(R.id.variantLayout);


        variantDialog.show();
        variantDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        variantDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        variantDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        variantDialog.getWindow().setGravity(Gravity.BOTTOM);


        // showing images in horizontal
        adapter = new VariantPhotoAdapter(tempImages, this, 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerImage.setLayoutManager(layoutManager);
        recyclerImage.setAdapter(adapter);

        //showing size summary
        sizeSummaryAdapter = new SizeSummaryAdapter(sizes, this, 1);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerSize.setLayoutManager(layoutManager1);
        recyclerSize.setAdapter(sizeSummaryAdapter);


        addSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorName.clearFocus();
                showSizeDialog(sizes);
            }
        });


        cardUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                imgLauncher.launch(intent);
            }
        });

        addVariant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (colorName.getText().toString().isEmpty() | sizes.isEmpty() | tempImages.isEmpty()) {
                    Snackbar.make(variantDialogLayout, "Please fill all the fields", Snackbar.LENGTH_SHORT).setAnchorView(addVariant).show();
                    return;
                }
                createImageBitmap(colorName, sizes);
                variantDialog.dismiss();
            }
        });


    }

    private void showSizeDialog(ArrayList<Product> sizes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAddProduct.this);
        builder.setMessage("Enter size details");
        builder.setCancelable(false);

        View viewInflated = LayoutInflater.from(ActivityAddProduct.this).inflate(R.layout.stock_editbox_dialog, findViewById(android.R.id.content), false);

        EditText input = viewInflated.findViewById(R.id.input);
        AutoCompleteTextView dropMenu = viewInflated.findViewById(R.id.autoSize);
        builder.setView(viewInflated);

        String[] sizelist = getResources().getStringArray(R.array.size);
        dropMenu.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, sizelist));


        dropMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                size = adapterView.getItemAtPosition(pos).toString();
            }
        });

        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stock;
                stock = input.getText().toString();
                if (size == null || stock.isEmpty()) {
                    Toast.makeText(ActivityAddProduct.this, "Your form is incomplete", Toast.LENGTH_SHORT).show();
                    return;
                }
                Product product = new Product();
                product.setSize(size);
                product.setStock(Integer.valueOf(stock));
                sizes.add(product);
                sizeSummaryAdapter.notifyItemInserted(sizes.size() - 1);
                dialog.dismiss();
                size = null;
            }
        });

    }

    private void createImageBitmap(TextInputEditText colorName, ArrayList<Product> sizes) {
        ArrayList<byte[]> images = new ArrayList<>();
        for (String image : tempImages) {
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
        if (images.size() == tempImages.size()) {
            uploadMultipleImageToFirebase(images, colorName, sizes);
        }
    }


    private void uploadMultipleImageToFirebase(ArrayList<byte[]> imgList, TextInputEditText colorName, ArrayList<Product> sizes) {
        ArrayList<String> finalImageList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Images");
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
                                    Product product = new Product();
                                    product.setColor(colorName.getText().toString());
                                    product.setSizes(sizes);
                                    product.setPhotos(finalImageList);
                                    variants.add(product);
                                    variantSummaryAdapter.notifyItemInserted(variants.size());
                                    dialog.dismiss();
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

    private void resetAllFields() {
        binding.edProductName.getText().clear();
        binding.edPrice.getText().clear();
        binding.edDescription.getText().clear();
        binding.autoSubCategory.getText().clear();
        binding.autoCategory.getText().clear();
        variants.clear();
        variantSummaryAdapter.notifyDataSetChanged();
    }

}