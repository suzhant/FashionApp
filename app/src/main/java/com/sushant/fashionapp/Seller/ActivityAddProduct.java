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
import android.view.WindowManager;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
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
import com.sushant.fashionapp.Adapters.EditSizeAdapter;
import com.sushant.fashionapp.Adapters.EditVariantAdapter;
import com.sushant.fashionapp.Adapters.SizeSummaryAdapter;
import com.sushant.fashionapp.Adapters.VariantPhotoAdapter;
import com.sushant.fashionapp.Inteface.VariantClickListener;
import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Seller;
import com.sushant.fashionapp.Models.Size;
import com.sushant.fashionapp.Models.Variants;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Utils.CheckConnection;
import com.sushant.fashionapp.Utils.ImageUtils;
import com.sushant.fashionapp.databinding.ActivityAddProductBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ActivityAddProduct extends AppCompatActivity {

    ActivityAddProductBinding binding;
    VariantPhotoAdapter adapter;
    ArrayList<String> tempImages = new ArrayList<>();
    ActivityResultLauncher<Intent> imgLauncher;
    ActivityResultLauncher<Intent> photoLauncher;
    ArrayList<Variants> variants = new ArrayList<>();
    ArrayList<String> catList = new ArrayList<>();
    ArrayList<String> subCatList = new ArrayList<>();
    ArrayList<String> subSubCatList = new ArrayList<>();

    EditVariantAdapter variantSummaryAdapter;
    SizeSummaryAdapter sizeSummaryAdapter;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String pName, cat, subCat, subSubCat, pDes, storeId, wholeSalePrice, storeName;
    FirebaseStorage storage;
    String size, brandName, season;
    VariantClickListener productClickListener;

    ArrayList<Size> sizes;
    ArrayList<String> photos;
    EditSizeAdapter editSizeAdapter;
    String color;
    VariantPhotoAdapter photoAdapter;
    ArrayList<String> upLoadPic;
    int position;
    double sellerPrice, commission;
    Thread t1 = null;
    MyCalc mycalc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        productClickListener = new VariantClickListener() {
            @Override
            public void onClick(Variants product, int pos) {
                openDialog(product);
                position = pos;
            }

            @Override
            public void onProductClick(Product product, int pos) {

            }
        };

        binding.btnAddVariant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        variantSummaryAdapter = new EditVariantAdapter(variants, this, null, productClickListener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerVariantSummary.setLayoutManager(layoutManager);
        binding.recyclerVariantSummary.setAdapter(variantSummaryAdapter);

        //category List
        // String[] catList = getResources().getStringArray(R.array.category);

        //season list
        String[] seasonList = getResources().getStringArray(R.array.seasons);
        binding.autoSeason.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.drop_down_items, seasonList));

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

        photoLauncher = registerForActivityResult(
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

                                    if (upLoadPic.size() > 4) {
                                        Toast.makeText(ActivityAddProduct.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    upLoadPic.add(String.valueOf(imageUrl));
                                    photos.add(String.valueOf(imageUrl));
                                    photoAdapter.notifyItemInserted(photos.size());
                                }

                            } else if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                if (upLoadPic.size() > 4) {
                                    Toast.makeText(ActivityAddProduct.this, "You can add upto 5 images only", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                upLoadPic.add(String.valueOf(selectedImage));
                                photos.add(String.valueOf(selectedImage));
                                photoAdapter.notifyItemInserted(photos.size());
                            }
                        }
                    }
                });


        binding.autoCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cat = adapterView.getItemAtPosition(i).toString();
                addSubCategory(cat);
                binding.autoSubCategory.getText().clear();
                binding.autoSubSubCategory.getText().clear();
                binding.ipSubCategory.setVisibility(View.VISIBLE);
                binding.ipSubSubCategory.setVisibility(View.GONE);
            }
        });

        binding.autoSubCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                subCat = adapterView.getItemAtPosition(i).toString();
                addSubSubCat();
                binding.autoSubSubCategory.getText().clear();
                binding.ipSubSubCategory.setVisibility(View.VISIBLE);
            }
        });

        binding.autoSubSubCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                subSubCat = adapterView.getItemAtPosition(i).toString();
            }
        });

        binding.btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pName = binding.edProductName.getText().toString();
                pDes = binding.edDescription.getText().toString();
                wholeSalePrice = binding.edPrice.getText().toString();
                brandName = binding.edBrandName.getText().toString();
                season = binding.autoSeason.getText().toString();
                cat = binding.autoCategory.getText().toString();
                subCat = binding.autoSubCategory.getText().toString();
                subSubCat = binding.autoSubSubCategory.getText().toString();

                if (pName.isEmpty() | cat.isEmpty() | subCat.isEmpty() | subSubCat.isEmpty() | pDes.isEmpty() | variants.isEmpty() | brandName.isEmpty() | season.isEmpty()) {
                    Snackbar.make(binding.parent, "Please complete the form", Snackbar.LENGTH_SHORT).setAnchorView(binding.linear).show();
                    return;
                }
                addProductToDB();
            }
        });

        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("sellerId").exists()) {
                    Seller user = snapshot.getValue(Seller.class);
                    storeId = user.getStoreId();
                    database.getReference().child("Store").child(storeId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            storeName = snapshot.child("storeName").getValue(String.class);
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
//        database.getReference().child("Store").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                    Store store = snapshot1.getValue(Store.class);
//                    assert store != null;
//                    if (store.getSellerId().equals(auth.getUid())) {
//                        storeId = store.getStoreId();
//                        storeName = store.getStoreName();
//                        break;
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
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

    }

    private void openDialog(Variants product) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_edit_variant_dialog);
        Button btnSave = bottomSheetDialog.findViewById(R.id.btnDone);
        TextInputEditText edColor = bottomSheetDialog.findViewById(R.id.edColorName);
        RecyclerView recyclerSize = bottomSheetDialog.findViewById(R.id.recycler_size);
        RecyclerView recyclerPhoto = bottomSheetDialog.findViewById(R.id.recycler_photo);
        MaterialCardView cardAddSize = bottomSheetDialog.findViewById(R.id.cardAddSize);
        MaterialCardView cardUpload = bottomSheetDialog.findViewById(R.id.cardUploadPhoto);

        edColor.setText(product.getColor());
        Objects.requireNonNull(bottomSheetDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetDialog.show();

        //size recycler
        sizes = new ArrayList<>();
        sizes.addAll(product.getSizes());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        assert recyclerSize != null;
        recyclerSize.setLayoutManager(layoutManager);
        editSizeAdapter = new EditSizeAdapter(sizes, this);
        recyclerSize.setAdapter(editSizeAdapter);

        //photo recycler
        photos = new ArrayList<>();
        upLoadPic = new ArrayList<>();
        photos.addAll(product.getPhotos());
        assert recyclerPhoto != null;
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerPhoto.setLayoutManager(layoutManager1);
        photoAdapter = new VariantPhotoAdapter(photos, this, 1);
        recyclerPhoto.setAdapter(photoAdapter);

        cardAddSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSizeDialog();
            }
        });

        cardUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ACTION_PICK WILL POP UP DIALOG AND SHOW GALLERY AND OTHERS
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                photoLauncher.launch(intent);
            }
        });


        assert btnSave != null;
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color = edColor.getText().toString();
                updateVariant();
                bottomSheetDialog.dismiss();
            }
        });

    }

    private class MyCalc implements Runnable {
        public MyCalc() {

        }

        @Override
        public void run() {
            //Do you math here
            wholeSalePrice = binding.edPrice.getText().toString();
            double wholesale = Double.parseDouble(wholeSalePrice);
            sellerPrice = wholesale / (1 - 0.3); //30% markup percentage in the wholesale price
      //      commission = wholesale * 0.1; //10% added to compensate commission
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

        ;
    }

    private void updateVariant() {
        if (color.isEmpty() | sizes.isEmpty() | photos.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please complete the form", Toast.LENGTH_SHORT).show();
            return;
        }
        confirmVariantDialog();
    }

    private void confirmVariantDialog() {
        if (CheckConnection.isOnline(this)) {
            new MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme)
                    .setMessage("Are you sure?")
                    .setTitle("Confirmation")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!upLoadPic.isEmpty()) {
                                createPhotoBitmap(sizes);
                            } else {
                                Variants product = new Variants();
                                product.setColor(color);
                                product.setSizes(sizes);
                                product.setPhotos(photos);
                                variants.remove(position);
                                variants.add(position, product);
                                variantSummaryAdapter.notifyItemChanged(position, 1);
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

    private void openSizeDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottomsheet_size);
        MaterialButton btnSave = bottomSheetDialog.findViewById(R.id.btnSave);
        AutoCompleteTextView autoSize = bottomSheetDialog.findViewById(R.id.autoSize);
        TextInputEditText edStock = bottomSheetDialog.findViewById(R.id.edStock);
        LinearLayout parent = bottomSheetDialog.findViewById(R.id.nameParent);

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
                for (Size s : sizes) {
                    if (s.getSize().equals(size)) {
                        Snackbar.make(parent, "This size is already added !!", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                }

                product.setSize(size);
                product.setStock(Integer.valueOf(stock));
                sizes.add(product);
                editSizeAdapter.notifyItemInserted(sizes.size());
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void addSubSubCat() {
        database.getReference().child("category").child(cat).child(subCat).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void addSubCategory(String category) {
        database.getReference().child("category").child(category).addListenerForSingleValueEvent(new ValueEventListener() {
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
                            String key = database.getReference().child("Products").push().getKey();
                            ProgressDialog dialog = new ProgressDialog(ActivityAddProduct.this);
                            dialog.setTitle("Uploading..");
                            dialog.setMessage("Please wait while we are adding your product");
                            dialog.setCancelable(false);

                            Product product1 = new Product(key, pName, variants.get(0).getPhotos().get(0));
                            product1.setLove(0);
                            product1.setpPrice((int) sellerPrice);
                            product1.setCategory(cat);
                            product1.setSubCategory(subCat);
                            product1.setSubSubCategory(subSubCat);
                            product1.setDesc(pDes);
                            product1.setTimeStamp(new Date().getTime());
                            product1.setBrandName(brandName);
                            product1.setVariants(variants);
                            product1.setSeason(season);
                            product1.setStoreId(storeId);

                            dialog.show();
                            database.getReference().child("Products").child(product1.getpId()).setValue(product1).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                //ACTION_PICK WILL POP UP DIALOG AND SHOW GALLERY AND OTHERS
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                imgLauncher.launch(intent);
            }
        });

        addVariant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color = colorName.getText().toString();
                if (color.isEmpty() | sizes.isEmpty() | tempImages.isEmpty()) {
                    Snackbar.make(variantDialogLayout, "Please fill all the fields", Snackbar.LENGTH_SHORT).setAnchorView(addVariant).show();
                    return;
                }
                for (Variants s : variants) {
                    if (s.getColor().equals(color)) {
                        Snackbar.make(variantDialogLayout, "This color has been already added!!", Snackbar.LENGTH_SHORT).setAnchorView(addVariant).show();
                        return;
                    }
                }
                createImageBitmap(sizes);
                variantDialog.dismiss();
            }
        });


    }

    private void showSizeDialog(ArrayList<Size> sizes) {
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
                for (Size s : sizes) {
                    if (s.getSize().equals(size)) {
                        Toast.makeText(ActivityAddProduct.this, "This size is already added !!", Toast.LENGTH_SHORT).show();
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

    private void createImageBitmap(ArrayList<Size> sizes) {
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
                                    Variants product = new Variants();
                                    product.setColor(color);
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
        binding.autoSubSubCategory.getText().clear();
        binding.edBrandName.getText().clear();
        binding.autoSeason.getText().clear();
        binding.ipSubCategory.setVisibility(View.GONE);
        binding.ipSubSubCategory.setVisibility(View.GONE);
        variants.clear();
        variantSummaryAdapter.notifyDataSetChanged();
    }

    private void createPhotoBitmap(ArrayList<Size> sizes) {
        ArrayList<byte[]> images = new ArrayList<>();
        int count = photos.size();
        for (String image : photos) {
            count--;
            if (!image.contains("https://firebasestorage.googleapis.com")) {
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
        }
        if (count == 0) {
            uploadMultiplePhotoToFirebase(images, sizes);
        }
    }


    private void uploadMultiplePhotoToFirebase(ArrayList<byte[]> imgList, ArrayList<Size> sizes) {
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
                                    photos.removeAll(upLoadPic);
                                    photos.addAll(finalImageList);
                                    Variants product = new Variants();
                                    product.setColor(color);
                                    product.setSizes(sizes);
                                    product.setPhotos(photos);
                                    variants.remove(position);
                                    variants.add(position, product);
                                    variantSummaryAdapter.notifyDataSetChanged();
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
    protected void onStop() {
        super.onStop();
        Thread.currentThread().interrupt();
        t1 = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Thread.currentThread().interrupt();
        t1 = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Thread.currentThread().interrupt();
        t1 = null;
    }
}