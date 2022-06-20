package com.sushant.fashionapp.Models;

import java.util.Objects;

public class Product {
    private String pId, pName, storeName, size;
    private int pPrice, pPic, love = 0, stock, quantity = 1;

    public Product() {
    }

    public Product(String pId, String pName, int pPic, int pPrice, String storeName, int stock) {
        this.pId = pId;
        this.pName = pName;
        this.pPic = pPic;
        this.pPrice = pPrice;
        this.storeName = storeName;
        this.stock = stock;
    }

    public Product(String pId, String pName, int pPic, int pPrice) {
        this.pId = pId;
        this.pName = pName;
        this.pPic = pPic;
        this.pPrice = pPrice;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }


    public int getpPrice() {
        return pPrice;
    }

    public void setpPrice(int pPrice) {
        this.pPrice = pPrice;
    }

    public int getpPic() {
        return pPic;
    }

    public void setpPic(int pPic) {
        this.pPic = pPic;
    }

    public int getLove() {
        return love;
    }

    public void setLove(int love) {
        this.love = love;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return pPrice == product.pPrice && pPic == product.pPic && love == product.love && stock == product.stock
                && Objects.equals(pId, product.pId) && Objects.equals(pName, product.pName)
                && Objects.equals(storeName, product.storeName) && quantity == product.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pId, pName, storeName, pPrice, pPic, love, stock);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

}
