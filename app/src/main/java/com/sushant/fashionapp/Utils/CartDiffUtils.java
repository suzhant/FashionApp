package com.sushant.fashionapp.Utils;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Product;

import java.util.ArrayList;

public class CartDiffUtils extends DiffUtil.Callback {

    private final ArrayList<Cart> mOldProducts;
    private final ArrayList<Cart> mNewProducts;

    public CartDiffUtils(ArrayList<Cart> mOldProducts, ArrayList<Cart> mNewProducts) {
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
        final Cart oldProduct = mOldProducts.get(oldItemPosition);
        final Cart newProduct = mNewProducts.get(newItemPosition);

        Bundle bundle = new Bundle();
        if (!oldProduct.getpName().equals(newProduct.getpName())) {
            bundle.putString("newName", newProduct.getpName());
        } else if (oldProduct.getpPrice() != newProduct.getpPrice()) {
            bundle.putInt("newPrice", newProduct.getpPrice());
        } else if (!oldProduct.getStoreName().equals(newProduct.getStoreName())) {
            bundle.putString("newStoreName", newProduct.getStoreName());
        } else if (oldProduct.getQuantity() != newProduct.getQuantity()) {
            bundle.putInt("newQuantity", newProduct.getQuantity());
        }


        if (bundle.size() == 0) {
            return null;
        }

        return bundle;
    }
}
