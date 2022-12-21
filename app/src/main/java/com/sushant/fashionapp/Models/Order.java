package com.sushant.fashionapp.Models;

import java.util.ArrayList;

public class Order extends Payment {
    private ArrayList<Delivery> products;
    private String addressId;
    private ArrayList<Store> stores;
    private String orderStatus;
    private Long orderDate;


    public Order() {
    }

    public ArrayList<Delivery> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Delivery> products) {
        this.products = products;
    }

   public String getAddressId() {
      return addressId;
   }

   public void setAddressId(String addressId) {
      this.addressId = addressId;
   }

   public ArrayList<Store> getStores() {
      return stores;
   }

   public void setStores(ArrayList<Store> stores) {
       this.stores = stores;
   }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Long getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Long orderDate) {
        this.orderDate = orderDate;
    }
}