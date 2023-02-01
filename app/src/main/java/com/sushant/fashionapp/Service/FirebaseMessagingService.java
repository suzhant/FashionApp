package com.sushant.fashionapp.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.sushant.fashionapp.Buyer.ChatActivity;
import com.sushant.fashionapp.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    NotificationManager mNotificationManager;
    String senderId, profilePic, message, msgType, title, Type, receiverId, from;
    NotificationCompat.Builder builder;
    String videoSoundPath;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        message = data.get("message");
        msgType = data.get("msgType");
        title = data.get("title");
//        icon = data.get("notificationIcon");
        Type = data.get("Type");


        videoSoundPath = "android.resource://" + getPackageName() + "/" + R.raw.incoming_sound;

        String path = "android.resource://" + getPackageName() + "/" + R.raw.iphone;
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
        r.play();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setLooping(false);
        }


        if ("Chat".equals(Type)) {
            senderId = data.get("senderId");
            profilePic = data.get("ProfilePic");
            receiverId = data.get("receiverId");
            from = data.get("destination");
            builder = new NotificationCompat.Builder(this, "CHANNEL_ID1");
            Intent resultIntent = new Intent(this, ChatActivity.class);
            resultIntent.putExtra("receiverId", senderId);
            resultIntent.putExtra("senderId", receiverId);
            resultIntent.putExtra("from", from);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.setContentTitle(title);
            builder.setContentIntent(pendingIntent);
            builder.setSound(Uri.parse(path), AudioManager.STREAM_NOTIFICATION);
            builder.setAutoCancel(true);
            builder.setPriority(Notification.PRIORITY_MAX);
            builder.setOnlyAlertOnce(true);
            builder.setSmallIcon(R.drawable.ic_baseline_circle_notifications_24);
            builder.setLights(0xff0000ff, 200, 200);
            builder.setDefaults(Notification.FLAG_SHOW_LIGHTS);
        }


        //        Uri img=remoteMessage.getNotification().getImageUrl();
        Bitmap bitmap2 = getBitmapFromUrl(String.valueOf(profilePic));
        switch (msgType) {
            case "photo":
                Bitmap bitmap1 = getBitmapFromUrlWithoutCircle(String.valueOf(message));
                builder.setContentText("Sent you a pic");
                builder.setLargeIcon(bitmap2).setStyle(
                        new NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap1)
                                .bigLargeIcon(null)
                );
                break;
            case "text":
                builder.setContentText(message).setStyle(new NotificationCompat.BigTextStyle().bigText(message));
                builder.setLargeIcon(bitmap2);
                break;
            case "audio":
                builder.setContentText("sent you an audio");
                builder.setLargeIcon(bitmap2);
                break;
            case "video":
                builder.setContentText(message);
                builder.setLargeIcon(bitmap2);
                break;
        }

        mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .setUsage(AudioAttributes.USAGE_ALARM)
//                    .build();
            String channelId = "Your_channel_id";
            NotificationChannel channel;
            channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);//disable notification sound
//            channel.setSound(Uri.parse(path),audioAttributes);
            mNotificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);


// notificationId is a unique int for each notification that you must define
            mNotificationManager.notify(100, builder.build());
        }


    }


    public Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return getCircleBitmap(bitmap);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

    public Bitmap getBitmapFromUrlWithoutCircle(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }


    public Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height) {
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height >> 1;
        } else {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width) / 2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width >> 1;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        bitmap.recycle();

        return output;
    }


    private Spannable getActionText(@StringRes int stringRes, @ColorRes int colorRes) {
        Spannable spannable = new SpannableString(this.getText(stringRes));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // This will only work for cases where the Notification.Builder has a fullscreen intent set
            // Notification.Builder that does not have a full screen intent will take the color of the
            // app and the following leads to a no-op.
            spannable.setSpan(
                    new ForegroundColorSpan(this.getColor(colorRes)), 0, spannable.length(), 0);
        }
        return spannable;
    }
}


