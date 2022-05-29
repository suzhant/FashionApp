package com.sushant.fashionapp.Utils;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.sushant.fashionapp.Models.Product;

import java.util.ArrayList;

public class CartDiffUtils extends DiffUtil.Callback {

    private final ArrayList<Product> mOldProducts;
    private final ArrayList<Product> mNewProducts;

    public CartDiffUtils(ArrayList<Product> mOldProducts, ArrayList<Product> mNewProducts) {
        this.mOldProducts = mOldProducts;
        this.mNewProducts = mNewProducts;
    }

    @Override
    public int getOldListSize() {
        return mOldProducts.size();
    }

    @Override
    public int getNewListSize() {
        return mNewProducts.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldProducts.get(oldItemPosition).getpId().equals(mNewProducts.get(newItemPosition).getpId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Product oldUser = mOldProducts.get(oldItemPosition);
        final Product newUsers = mNewProducts.get(newItemPosition);
        return oldUser.equals(newUsers);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        final Product oldProduct = mOldProducts.get(oldItemPosition);
        final Product newProduct = mNewProducts.get(newItemPosition);

        Bundle bundle = new Bundle();
        if (!oldProduct.getpName().equals(newProduct.getpName())) {
            bundle.putString("newName", newProduct.getpName());
        } else if (oldProduct.getpPrice() != newProduct.getpPrice()) {
            bundle.putInt("newPrice", newProduct.getpPrice());
        } else if (!oldProduct.getStoreName().equals(newProduct.getStoreName())) {
            bundle.putString("newStoreName", newProduct.getStoreName());
        } else if (oldProduct.getStock() != newProduct.getStock()) {
            bundle.putInt("newStock", newProduct.getStock());
        }


        if (bundle.size() == 0) {
            return null;
        }

        return bundle;
    }
}
