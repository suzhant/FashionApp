package com.sushant.fashionapp.Models;

public class Seller extends Users {
    private String panNo;
    private String citizenNo;
    private String imgCitizenFront, imgCitizenBack, imgPanFront, imgPanBack;
    private String sellerId;
    private String storeId;

    public Seller() {
    }

    public Seller(String userName, String userEmail, String userPhone) {
        super(userName, userEmail, userPhone);
    }

    public String getPanNo() {
        return panNo;
    }

    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }

    public String getCitizenNo() {
        return citizenNo;
    }

    public void setCitizenNo(String citizenNo) {
        this.citizenNo = citizenNo;
    }

    public String getImgCitizenFront() {
        return imgCitizenFront;
    }

    public void setImgCitizenFront(String imgCitizenFront) {
        this.imgCitizenFront = imgCitizenFront;
    }

    public String getImgCitizenBack() {
        return imgCitizenBack;
    }

    public void setImgCitizenBack(String imgCitizenBack) {
        this.imgCitizenBack = imgCitizenBack;
    }

    public String getImgPanFront() {
        return imgPanFront;
    }

    public void setImgPanFront(String imgPanFront) {
        this.imgPanFront = imgPanFront;
    }

    public String getImgPanBack() {
        return imgPanBack;
    }

    public void setImgPanBack(String imgPanBack) {
        this.imgPanBack = imgPanBack;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
}
