package com.sushant.fashionapp.Seller;

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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.sushant.fashionapp.Adapters.EditVariantAdapter;
import com.sushant.fashionapp.Adapters.SizeSummaryAdapter;
import com.sushant.fashionapp.Adapters.VariantPhotoAdapter;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Size;
import com.sushant.fashionapp.Models.Variants;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.Utils.ImageUtils;
import com.sushant.fashionapp.databinding.ActivityEditProductDetailsBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class EditProductDetailsActivity extends AppCompatActivity {

    ActivityEditProductDetailsBinding binding;
    EditVariantAdapter adapter;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String pid, pName, brandName, season, masterCategory, category, subcategory, pDesc;
    Integer price;
    ArrayList<String> tempImages = new ArrayList<>();
    ActivityResultLauncher<Intent> imgLauncher = null;
    SizeSummaryAdapter sizeSummaryAdapter;
    VariantPhotoAdapter photoAdapter;
    ArrayList<Variants> variants = new ArrayList<>();
    String size;
    FirebaseStorage storage;
    ArrayList<String> subCatList = new ArrayList<>();
    ArrayList<String> subSubCatList = new ArrayList<>();
    ArrayList<String> catList = new ArrayList<>();
    int variantIndex;
    double sellerPrice, commission;
    Thread t1 = null;
    MyCalc mycalc = null;
    String wholeSalePrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        pid = getIntent().getStringExtra("pId");
        variants = (ArrayList<Variants>) getIntent().getSerializableExtra("origVariant");
        variantIndex = getIntent().getIntExtra("variantIndex", 0);

        binding.btnAddVariant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });


        database.getReference().child("Products").child(pid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                pName = product.getpName();
                brandName = product.getBrandName();
                season = product.getSeason();
                masterCategory = product.getCategory();
                category = product.getSubCategory();
                subcategory = product.getSubSubCategory();
                pDesc = product.getDesc();
                price = product.getpPrice();
                binding.edProductName.setText(pName);
                binding.edBrandName.setText(brandName);
                binding.autoSeason.setText(season);
                binding.autoCategory.setText(masterCategory);
                binding.autoSubCategory.setText(category);
                binding.autoSubSubCategory.setText(subcategory);
                wholeSalePrice = String.valueOf(Math.round(price * 0.7));
                binding.edPrice.setText(wholeSalePrice);
                binding.edDescription.setText(pDesc);
                //season list
                String[] seasonList = getResources().getStringArray(R.array.seasons);
                binding.autoSeason.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, seasonList));

                addSubCategory();
                addSubSubCat();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.edPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    mycalc = new MyCalc();
                    t1 = new Thread(mycalc);
                    t1.start();
                } else {
                    binding.txtSellerPrice.setText(MessageFormat.format("Suggested Retail Price (SRP): Rs.{0}", 0));
                    binding.txtBargainLimit.setText(MessageFormat.format("Bargain Limit: Rs.{0}", 0));
                }

            }
        });

//        database.getReference().child("Products").child(pid).child("variants").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                variants.clear();
//                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                    Product product1 = snapshot1.getValue(Product.class);
//                    variants.add(product1);
//                }
//                adapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

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
                                    Toast.makeText(EditProductDetailsActivity.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                for (int i = 0; i < count; i++) {
                                    Uri imageUrl = clipData.getItemAt(i).getUri();

                                    if (tempImages.size() > 4) {
                                        Toast.makeText(EditProductDetailsActivity.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    tempImages.add(String.valueOf(imageUrl));
                                    photoAdapter.notifyItemInserted(tempImages.size());
                                }

                            } else if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                if (tempImages.size() > 4) {
                                    Toast.makeText(EditProductDetailsActivity.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                tempImages.add(String.valueOf(selectedImage));
                                photoAdapter.notifyItemInserted(tempImages.size());
                            }
                        }
                    }
                });

        binding.btnEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pName = binding.edProductName.getText().toString();
                pDesc = binding.edDescription.getText().toString();
                price = Integer.valueOf(binding.edPrice.getText().toString());
                brandName = binding.edBrandName.getText().toString();
                season = binding.autoSeason.getText().toString();
                masterCategory = binding.autoCategory.getText().toString();
                category = binding.autoSubCategory.getText().toString();
                subcategory = binding.autoSubSubCategory.getText().toString();

                if (pName.isEmpty() | masterCategory.isEmpty() | category.isEmpty() | subcategory.isEmpty() | pDesc.isEmpty() | variants.isEmpty() | brandName.isEmpty() | season.isEmpty()) {
                    Snackbar.make(binding.parent, "Please complete the form", Snackbar.LENGTH_SHORT).setAnchorView(binding.linear).show();
                    return;
                }
                addProductToDB();
            }
        });

        database.getReference().child("category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                catList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String cat = snapshot1.getKey();
                    catList.add(cat);
                }
                binding.autoCategory.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, catList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.autoCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                masterCategory = adapterView.getItemAtPosition(i).toString();
                addSubCategory();
                binding.autoSubCategory.getText().clear();
                binding.autoSubSubCategory.getText().clear();
                binding.ipSubCategory.setVisibility(View.VISIBLE);
                binding.ipSubSubCategory.setVisibility(View.GONE);
            }
        });


        binding.autoSubCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                category = adapterView.getItemAtPosition(i).toString();
                addSubSubCat();
                binding.autoSubSubCategory.getText().clear();
                binding.ipSubSubCategory.setVisibility(View.VISIBLE);
            }
        });

        binding.autoSubSubCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                subcategory = adapterView.getItemAtPosition(i).toString();
            }
        });

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditProductDetailsActivity.this, EditProductActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        initRecyclerView();
    }

    private class MyCalc implements Runnable {
        private volatile boolean running = true;

        public MyCalc() {

        }

        @Override
        public void run() {
            //Do you math here
            wholeSalePrice = binding.edPrice.getText().toString().trim();
            double wholesale = Double.parseDouble(wholeSalePrice);
            sellerPrice = wholesale / (1 - 0.3); //30% markup percentage in the wholesale price
         //   commission = wholesale * 0.1; //10% added to compensate commission
            double bargainLimit = wholesale * 1.2; //20%

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.txtSellerPrice.setText(MessageFormat.format("Suggested Retail Price (SRP): Rs.{0}", Math.round(sellerPrice)));
                    binding.txtBargainLimit.setText(MessageFormat.format("Bargain Limit: Rs.{0}", Math.round(bargainLimit)));
                    // text will be updated automatically
                }
            });
        }
    }

    private void addSubSubCat() {
        database.getReference().child("category").child(masterCategory).child(category).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subSubCatList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String subCat = snapshot1.getValue(String.class);
                    subSubCatList.add(subCat);
                }
                binding.autoSubSubCategory.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, subSubCatList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addSubCategory() {
        database.getReference().child("category").child(masterCategory).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subCatList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String subCat = snapshot1.getKey();
                    subCatList.add(subCat);
                }
                binding.autoSubCategory.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, subCatList));
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
                            ProgressDialog dialog = new ProgressDialog(EditProductDetailsActivity.this);
                            dialog.setTitle("Uploading..");
                            dialog.setMessage("Please wait while we are adding your product");
                            dialog.setCancelable(false);

                            HashMap<String, Object> obj = new HashMap<>();
                            obj.put("pName", pName);
                            obj.put("brandName", brandName);
                            obj.put("season", season);
                            obj.put("category", masterCategory);
                            obj.put("subCategory", category);
                            obj.put("pPrice", sellerPrice);
                            obj.put("desc", pDesc);
                            obj.put("variants", variants);

                            dialog.show();
                            database.getReference().child("Products").child(pid).updateChildren(obj).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
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

    private void resetAllFields() {
        binding.edProductName.getText().clear();
        binding.edPrice.getText().clear();
        binding.edDescription.getText().clear();
        binding.autoSubCategory.getText().clear();
        binding.autoCategory.getText().clear();
        binding.autoSubSubCategory.getText().clear();
        binding.edBrandName.getText().clear();
        binding.autoSeason.getText().clear();
        binding.ipSubCategory.setVisibility(View.GONE);
        binding.ipSubSubCategory.setVisibility(View.GONE);
        variants.clear();
        adapter.notifyDataSetChanged();
    }


    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerVariantSummary.setLayoutManager(layoutManager);
        adapter = new EditVariantAdapter(variants, this, pid, null);
        binding.recyclerVariantSummary.setAdapter(adapter);
    }

    private void showDialog() {
        tempImages.clear();
        ArrayList<Size> sizes = new ArrayList<>();

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
        photoAdapter = new VariantPhotoAdapter(tempImages, this, 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerImage.setLayoutManager(layoutManager);
        recyclerImage.setAdapter(photoAdapter);

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
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
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

    private void showSizeDialog(ArrayList<Size> sizes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProductDetailsActivity.this);
        builder.setMessage("Enter size details");
        builder.setCancelable(false);

        View viewInflated = LayoutInflater.from(EditProductDetailsActivity.this).inflate(R.layout.stock_editbox_dialog, findViewById(android.R.id.content), false);

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
                    Toast.makeText(EditProductDetailsActivity.this, "Your form is incomplete", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Size s : sizes) {
                    if (s.getSize().equals(size)) {
                        Toast.makeText(EditProductDetailsActivity.this, "This size is already added !!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Size product = new Size();
                product.setSize(size);
                product.setStock(Integer.valueOf(stock));
                sizes.add(product);
                sizeSummaryAdapter.notifyItemInserted(sizes.size() - 1);
                dialog.dismiss();
                size = null;
            }
        });
    }

    private void createImageBitmap(TextInputEditText colorName, ArrayList<Size> sizes) {
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


    private void uploadMultipleImageToFirebase(ArrayList<byte[]> imgList, TextInputEditText colorName, ArrayList<Size> sizes) {
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
                                    Variants product = new Variants();
                                    product.setColor(colorName.getText().toString());
                                    product.setSizes(sizes);
                                    product.setPhotos(finalImageList);
                                    variants.add(product);
                                    adapter.notifyItemInserted(variants.size());
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Thread.currentThread().interrupt();
        t1 = null;
        Intent intent = new Intent(EditProductDetailsActivity.this, EditProductActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Thread.currentThread().interrupt();
        t1 = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Thread.currentThread().interrupt();
        t1 = null;
    }
}