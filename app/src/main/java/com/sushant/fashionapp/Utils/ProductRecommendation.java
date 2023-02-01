package com.sushant.fashionapp.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray colArray = jsonObject.getJSONArray("result");
                            for (int i = 0; i < colArray.length(); i++) {
                                String id = colArray.getString(i);
                                database.getReference().child("Products").orderByChild("pId").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                            Product product = snapshot1.getValue(Product.class);
                                            assert product != null;
                                            product.setDateRecommended(new Date().getTime());
                                            if (category.equals(product.getArticleType())) {
                                                product.setFrequency(1);
                                                database.getReference().child("Recommended Products").child(auth.getUid()).child(product.getpId()).setValue(product);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            updateFrequency(pId);

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
            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("url", imgUrl);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }

    private void updateFrequency(String pId) {
        database.getReference().child("Recommended Products").child(auth.getUid()).child(pId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Integer frequency = snapshot.child("frequency").getValue(Integer.class);
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("frequency", frequency + 1);
                            database.getReference().child("Recommended Products").child(auth.getUid()).child(pId).updateChildren(map);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
