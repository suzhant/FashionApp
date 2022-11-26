package com.sushant.fashionapp.Inteface;

import com.sushant.fashionapp.Models.Product;
import com.sushant.fashionapp.Models.Variants;

public interface VariantClickListener {
    void onClick(Variants product, int pos);

    void onProductClick(Product product, int pos);
}
