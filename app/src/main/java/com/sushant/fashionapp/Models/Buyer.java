package com.sushant.fashionapp.Models;

public class Buyer extends Users {

    private String userId;

    public Buyer() {
    }

    public Buyer(String userName, String userEmail, String userPhone) {
        super(userName, userEmail, userPhone);
    }

    public Buyer(String userName, String userEmail) {
        super(userName, userEmail);
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
