package com.sushant.fashionapp.Models;

import com.sushant.fashionapp.Utils.TextUtils;

public class Users {
    private String userName;
    private String userEmail;
    private String userSecondaryEmail;
    private String userDOB;
    private String userPhone;
    private String userPic;
    private String userAddress;

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

    public void setUserDOB(String userDOB) {
        this.userDOB = userDOB;
    }


    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserSecondaryEmail() {
        return userSecondaryEmail;
    }

    public void setUserSecondaryEmail(String userSecondaryEmail) {
        this.userSecondaryEmail = userSecondaryEmail;
    }
}
