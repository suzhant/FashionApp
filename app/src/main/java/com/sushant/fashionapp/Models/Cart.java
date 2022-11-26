package com.sushant.fashionapp.Models;

import java.io.Serializable;

public class Cart extends Product implements Serializable {
    private Integer variantIndex, sizeIndex;
    private String variantPId, size, color, storeName;

    public Cart() {
    }

    public Cart(String pId, String pName, String pPic, Integer pPrice) {
        super(pId, pName, pPic, pPrice);
    }


    @Override
    public Integer getVariantIndex() {
        return variantIndex;
    }

    @Override
    public void setVariantIndex(Integer variantIndex) {
        this.variantIndex = variantIndex;
    }

    @Override
    public Integer getSizeIndex() {
        return sizeIndex;
    }

    @Override
    public void setSizeIndex(Integer sizeIndex) {
        this.sizeIndex = sizeIndex;
    }

    @Override
    public String getVariantPId() {
        return variantPId;
    }

    @Override
    public void setVariantPId(String variantPId) {
        this.variantPId = variantPId;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
