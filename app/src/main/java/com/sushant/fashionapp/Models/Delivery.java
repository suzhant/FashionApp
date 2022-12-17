package com.sushant.fashionapp.Models;

public class Delivery extends Cart {
    private String deliveryStatus;
    private Long deliverDate;

    public Delivery() {
    }


    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Long getDeliverDate() {
        return deliverDate;
    }

    public void setDeliverDate(Long deliverDate) {
        this.deliverDate = deliverDate;
    }
}
