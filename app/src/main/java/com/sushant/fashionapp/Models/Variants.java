package com.sushant.fashionapp.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Variants implements Serializable {
    private String variantId, color;
    private ArrayList<String> photos;
    private ArrayList<Size> sizes;

    public Variants() {
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<String> photos) {
        this.photos = photos;
    }

    public ArrayList<Size> getSizes() {
        return sizes;
    }

    public void setSizes(ArrayList<Size> sizes) {
        this.sizes = sizes;
    }
}
