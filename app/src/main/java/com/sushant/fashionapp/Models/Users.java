package com.sushant.fashionapp.Models;

import com.sushant.fashionapp.Utils.TextUtils;

import java.util.ArrayList;

public class Users {
    private String userName;
    private String userEmail;
    private String userDOB;
    private String userPhone;
    private String buyerId;
    private String sellerId;
    private String userPic;
    private String panNo;
    private String citizenNo;
    private String imgCitizenFront, imgCitizenBack, imgPanFront, imgPanBack;
    private ArrayList<Users> images;

    public Users() {
    }

    public Users(String userName, String userEmail, String userPhone) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = TextUtils.captializeAllFirstLetter(userName);
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserDOB() {
        return userDOB;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    public String getPanNo() {
        return panNo;
    }

    public String getCitizenNo() {
        return citizenNo;
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

    public ArrayList<Users> getImages() {
        return images;
    }

    public void setImages(ArrayList<Users> images) {
        this.images = images;
    }

    public void setUserDOB(String userDOB) {
        this.userDOB = userDOB;
    }

    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }

    public void setCitizenNo(String citizenNo) {
        this.citizenNo = citizenNo;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}
