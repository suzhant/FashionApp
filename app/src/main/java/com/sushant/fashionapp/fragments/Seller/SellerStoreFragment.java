package com.sushant.fashionapp.fragments.Seller;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sushant.fashionapp.databinding.FragmentSellerStoreBinding;
import com.sushant.fashionapp.seller.ActivityAddProduct;
import com.sushant.fashionapp.seller.DisplayProductActivity;
import com.sushant.fashionapp.seller.EditProductActivity;

public class SellerStoreFragment extends Fragment {

    FragmentSellerStoreBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSellerStoreBinding.inflate(inflater, container, false);

        binding.btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ActivityAddProduct.class));
            }
        });

        binding.btnDisplayProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), DisplayProductActivity.class));
            }
        });

        binding.btnEditProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditProductActivity.class));
            }
        });

        return binding.getRoot();
    }
}