package com.sushant.fashionapp.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sushant.fashionapp.BuildConfig;
import com.sushant.fashionapp.R;
import com.sushant.fashionapp.Seller.SellerHomePage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ImageUtils {
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = Math.min(heightRatio, widthRatio);

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static void createImageBitmap(HashMap<String, Uri> imageUrl, String sellerId, Context context) {
        ArrayList<byte[]> images = new ArrayList<>();
        ArrayList<String> imageName = new ArrayList<>();
        for (Map.Entry<String, Uri> set : imageUrl.entrySet()) {
            Bitmap bitmap = null;
            try {
                bitmap = ImageUtils.handleSamplingAndRotationBitmap(context, set.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            images.add(bytes);
            imageName.add(set.getKey());
        }

        int totalLength = totalLength(images);
        uploadMultipleImageToFirebase(images, totalLength, imageName, sellerId, context);
    }

    private static void uploadMultipleImageToFirebase(ArrayList<byte[]> uri, int length, ArrayList<String> imageName, String sellerId, Context context) {
        Calendar calendar = Calendar.getInstance();
        ProgressDialog dialog = new ProgressDialog(context);
//        if (length > 256) {
//            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        }
//        dialog.setProgress(0);
        dialog.setMessage("Please wait..");
        dialog.setCancelable(false);
        dialog.show();
        for (int i = 0; i < uri.size(); i++) {
            final StorageReference reference = storage.getReference().child("Seller").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
            UploadTask uploadTask = reference.putBytes(uri.get(i));
            int finalI = i;
            int size = uri.size();
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
                                HashMap<String, Object> image = new HashMap<>();
                                image.put(imageName.get(finalI), filePath);
                                database.getReference().child("Seller").child(sellerId).updateChildren(image);
                                if (finalI == size - 1) {
                                    dialog.dismiss();
                                    context.startActivity(new Intent(context, SellerHomePage.class));
                                }
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                    //only works if image size is greater than 256kb!
//                    if (length > 256) {
//                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
//                        int currentProgress = (int) progress;
//                        dialog.setProgress(currentProgress);
//                    }
//                }
//            });
        }

    }

    private static int totalLength(ArrayList<byte[]> images) {
        int sum = 0;
        for (byte[] bytes : images) {
            sum += bytes.length / 1024;
        }
        return sum;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        assert drawable != null;
        //setting color of image to white
        drawable.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_ATOP);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @NonNull
    public static byte[] getByteFromDrawable(Context context, int resourceId) {
        Drawable drawable = ContextCompat.getDrawable(context, resourceId);
        assert drawable != null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static String getURLForResource(int resourceId) {
        //use BuildConfig.APPLICATION_ID instead of R.class.getPackage().getName() if both are not same
        return Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + resourceId).toString();
    }

}
