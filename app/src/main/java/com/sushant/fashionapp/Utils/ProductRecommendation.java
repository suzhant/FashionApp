package com.sushant.fashionapp.Utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.sushant.fashionapp.Models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProductRecommendation {

    private static final String url = "http://suzhant.pythonanywhere.com/predict";
    String imgUrl, category, pId;
    Context context;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();


    public ProductRecommendation(String pId, String imgUrl, String category, Context context) {
        this.pId = pId;
        this.imgUrl = imgUrl;
        this.category = category;
        this.context = context;
    }

    public void recommend() {
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response != null) {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray colArray = jsonObject.getJSONArray("result");
                                for (int i = 0; i < colArray.length(); i++) {
                                    //  String id = colArray.getString(i);
                                    JSONObject jsonObject1 = colArray.getJSONObject(i);
                                    Log.d("jsonArray", jsonObject1.toString());
                                    String id = jsonObject1.getString("pId");
                                    String cat = jsonObject1.getString("articleType");

                                    //converting jsonObject into Product model..or deserializing json into Product model
                                    Gson gson = new Gson();
                                    Product product = gson.fromJson(jsonObject1.toString(), Product.class);

                                    if (cat.equals(category)) {
                                        product.setDateRecommended(new Date().getTime());
                                        database.getReference().child("Recommended Products").child(auth.getUid()).child(id).setValue(product);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    //   Toast.makeText(context.getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("errorMessage", error.getMessage());
                }
            }
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("url", imgUrl);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);


        new Handler().post(new Runnable() {
            @Override
            public void run() {
                queue.add(stringRequest);
            }
        });

    }
}
