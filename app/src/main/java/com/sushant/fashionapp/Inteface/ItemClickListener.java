package com.sushant.fashionapp.Inteface;

import com.sushant.fashionapp.Models.Address;

public interface ItemClickListener {
    void onClick(String item, String type);

    void onAddressClick(Address address, boolean b);
}
