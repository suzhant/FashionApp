package com.sushant.fashionapp.Models;

import java.util.Comparator;

public class NotificationModel {
    private String title, body, imageProfile, notificationId, type, receiverId;
    private Long time;
    private Boolean isInteracted;

    public NotificationModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageProfile() {
        return imageProfile;
    }

    public void setImageProfile(String imageProfile) {
        this.imageProfile = imageProfile;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Boolean getInteracted() {
        return isInteracted;
    }

    public void setInteracted(Boolean interacted) {
        isInteracted = interacted;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public static Comparator<NotificationModel> latestTime = new Comparator<NotificationModel>() {
        @Override
        public int compare(NotificationModel notificationModel, NotificationModel t1) {
            return t1.getTime().compareTo(notificationModel.getTime());
        }
    };
}
