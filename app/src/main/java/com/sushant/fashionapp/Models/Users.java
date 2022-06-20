package com.sushant.fashionapp.Models;

import com.sushant.fashionapp.Utils.TextUtils;

public class Users {
    private String userName, userEmail, userGender, userDOB, userPhone, userId, userPic;
    public Gender gender;

    public Users() {
    }

    public Users(String userName, String userEmail, String userGender, String userDOB, String userPhone) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userGender = userGender;
        this.userDOB = userDOB;
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

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserDOB() {
        return userDOB;
    }

    public void setUserDOB(String userDOB) {
        this.userDOB = userDOB;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }
}
