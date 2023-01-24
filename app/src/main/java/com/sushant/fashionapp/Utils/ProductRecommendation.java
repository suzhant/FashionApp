package com.sushant.fashionapp.Utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.fashionapp.Models.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductRecommendation {

    private static final String url = "http://suzhant.pythonanywhere.com/predict";
    String imgUrl;
    Context context;
    ArrayList<String> results = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();


    public ProductRecommendation(String imgUrl, Context context) {
        this.imgUrl = imgUrl;
        this.context = context;
    }

    public void recommend() {
        RequestQueue queue = Volley.newRequestQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray colArray = jsonObject.getJSONArray("result");
                            results.clear();
                            for (int i = 0; i < colArray.length(); i++) {
                                String id = colArray.getString(i);
                                results.add(id);
                            }


                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    for (String id : results) {
                                        database.getReference().child("Products").orderByChild("pId").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                    Product product = snapshot1.getValue(Product.class);
                                                    database.getReference().child("Recommended Products").child(auth.getUid()).child(product.getpId()).setValue(product);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                    Log.d("id1", results.toString());
                                }
                            }, 100);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("error", error.getMessage());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("url", imgUrl);
                return params;
            }
        };
        queue.add(stringRequest);
    }
}
