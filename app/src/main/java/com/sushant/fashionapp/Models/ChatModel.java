package com.sushant.fashionapp.Models;

public class ChatModel extends Store {
    private String LastMessage;
    private Long timestamp;

    public ChatModel() {
    }

    public String getLastMessage() {
        return LastMessage;
    }

    public void setLastMessage(String lastMessage) {
        LastMessage = lastMessage;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
