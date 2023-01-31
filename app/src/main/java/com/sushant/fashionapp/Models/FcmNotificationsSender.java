package com.sushant.fashionapp.Models;

import android.app.Activity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender {

    String userFcmToken;
    String title;
    String body;
    Activity mActivity;
    String avatar, receiverId, senderId, msgType, Type, click_action, from;


    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey = "AAAAYLNtExw:APA91bFLujGW8bPIQiAHAFqAnFF0F9n6jbf4NSMRXIdVRUoQ8gwV80NJqDXcatT7jWKINV5euyab-FIpoLVPFrd9Md3oUJzstDJ0d1kKSqyG_nfJ0iMcFoYX_P9OSdIAkP2TzgP8-xgf";

    public FcmNotificationsSender(String userFcmToken, String title, String body, String avatar, String receiverId, String senderId, String msgType, String from, String Type, String click_action, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.avatar = avatar;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.msgType = msgType;
        this.from = from;
        this.Type = Type;
        this.click_action = click_action;
        this.mActivity = mActivity;
    }


    public void sendNotifications() throws IOException {


        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject dataObject = new JSONObject();
            dataObject.put("title", title);
            dataObject.put("senderId", senderId);
            dataObject.put("receiverId", receiverId);
            dataObject.put("message", body);
            dataObject.put("ProfilePic", avatar);
            dataObject.put("destination", from);
            dataObject.put("msgType", msgType);
            dataObject.put("Type", Type);
            dataObject.put("click_action", click_action);
            mainObj.put("data", dataObject);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    // code run is got response

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // code run is got error

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {


                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;


                }
            };
            requestQueue.add(request);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
