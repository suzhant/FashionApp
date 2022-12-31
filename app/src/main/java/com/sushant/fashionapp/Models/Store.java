package com.sushant.fashionapp.Models;

import java.util.ArrayList;

public class Store {
    private String storeName;
    private String storeId;
    private String storeRating;
    private String storePhone;
    private String storeEmail;
    private String storeAddress;
    private String storeVAT;
    private String storeDesc;
    private String sellerId;
    private String storePic;
    private String storeSecondaryEmail;
    private Integer deliveryCharge;
    private Boolean selfPickUp;
    private ArrayList<Cart> products;


    public Store() {
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getStoreRating() {
        return storeRating;
    }

    public String getStorePhone() {
        return storePhone;
    }

    public String getStoreEmail() {
        return storeEmail;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public String getStoreVAT() {
        return storeVAT;
    }

    public String getStoreDesc() {
        return storeDesc;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getStorePic() {
        return storePic;
    }

    public String getStoreSecondaryEmail() {
        return storeSecondaryEmail;
    }

    public void setStoreSecondaryEmail(String storeSecondaryEmail) {
        this.storeSecondaryEmail = storeSecondaryEmail;
    }


    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public ArrayList<Cart> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Cart> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "Store{" +
                "storeName='" + storeName + '\'' +
                ", storeId='" + storeId + '\'' +
                ", storeRating='" + storeRating + '\'' +
                ", storePhone='" + storePhone + '\'' +
                ", storeEmail='" + storeEmail + '\'' +
                ", storeAddress='" + storeAddress + '\'' +
                ", storeVAT='" + storeVAT + '\'' +
                ", storeDesc='" + storeDesc + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", storePic='" + storePic + '\'' +
                ", storeSecondaryEmail='" + storeSecondaryEmail + '\'' +
                ", products=" + products +
                '}';
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setStoreRating(String storeRating) {
        this.storeRating = storeRating;
    }

    public void setStorePhone(String storePhone) {
        this.storePhone = storePhone;
    }

    public void setStoreEmail(String storeEmail) {
        this.storeEmail = storeEmail;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public void setStoreVAT(String storeVAT) {
        this.storeVAT = storeVAT;
    }

    public void setStoreDesc(String storeDesc) {
        this.storeDesc = storeDesc;
    }

    public void setStorePic(String storePic) {
        this.storePic = storePic;
    }

    public Integer getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(Integer deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }


    public Boolean getSelfPickUp() {
        return selfPickUp;
    }

    public void setSelfPickUp(Boolean selfPickUp) {
        this.selfPickUp = selfPickUp;
    }
}
