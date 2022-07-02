package com.sushant.fashionapp.Models;

import java.util.ArrayList;
import java.util.Objects;

public class Product {
    private String pId, pName, storeName, size, maxLimit, color;
    private Integer pPrice, pPic, stock, love, quantity;
    private ArrayList<Product> variants;
    private ArrayList<Product> sizes;

    public Product() {
    }

    public Product(String pId, String pName, Integer pPic, Integer pPrice, String storeName, Integer stock) {
        this.pId = pId;
        this.pName = pName;
        this.pPic = pPic;
        this.pPrice = pPrice;
        this.storeName = storeName;
        this.stock = stock;
    }

    public Product(String pId, String size, Integer pPrice, Integer stock) {
        this.pId = pId;
        this.size = size;
        this.pPrice = pPrice;
        this.stock = stock;
    }

    public Product(String pId, String pName, String storeName, Integer pPic) {
        this.pId = pId;
        this.pName = pName;
        this.storeName = storeName;
        this.pPic = pPic;
    }

    public Product(String pId, String color, Integer pPic) {
        this.pId = pId;
        this.color = color;
        this.pPic = pPic;
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(String maxLimit) {
        this.maxLimit = maxLimit;
    }

    public ArrayList<Product> getVariants() {
        return variants;
    }

    public void setVariants(ArrayList<Product> variants) {
        this.variants = variants;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ArrayList<Product> getSizes() {
        return sizes;
    }

    public void setSizes(ArrayList<Product> sizes) {
        this.sizes = sizes;
    }

    public Integer getpPrice() {
        return pPrice;
    }

    public void setpPrice(Integer pPrice) {
        this.pPrice = pPrice;
    }

    public Integer getpPic() {
        return pPic;
    }

    public void setpPic(Integer pPic) {
        this.pPic = pPic;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getLove() {
        return love;
    }

    public void setLove(Integer love) {
        this.love = love;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
