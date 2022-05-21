package com.sushant.fashionapp.Models;

public class Product {
    private String pId, pName;
    private int pPrice, pPic;

    public Product() {
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
}
