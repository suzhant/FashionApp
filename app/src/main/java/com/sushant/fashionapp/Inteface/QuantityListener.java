package com.sushant.fashionapp.Inteface;

import com.sushant.fashionapp.Models.Cart;

public interface QuantityListener {
    void onClick(Cart cart, int quantity, String type);
}
