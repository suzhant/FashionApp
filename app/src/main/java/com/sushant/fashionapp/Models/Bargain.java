package com.sushant.fashionapp.Models;

public class Bargain {
    private Integer originalPrice, bargainPrice, noOfTries, sellerPrice;
    private String buyerId, storeId, productId, bargainId, sellerId, status;
    private Long timestamp;
    private Boolean isBlocked, isCountered;


    public Bargain() {
    }

    public Bargain(Integer originalPrice, Integer bargainPrice, String bargainId) {
        this.originalPrice = originalPrice;
        this.bargainPrice = bargainPrice;
        this.bargainId = bargainId;
    }

    public Integer getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Integer originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getBargainPrice() {
        return bargainPrice;
    }

    public void setBargainPrice(Integer bargainPrice) {
        this.bargainPrice = bargainPrice;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getBargainId() {
        return bargainId;
    }

    public void setBargainId(String bargainId) {
        this.bargainId = bargainId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getNoOfTries() {
        return noOfTries;
    }

    public void setNoOfTries(Integer noOfTries) {
        this.noOfTries = noOfTries;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked;
    }

    public Integer getSellerPrice() {
        return sellerPrice;
    }

    public void setSellerPrice(Integer sellerPrice) {
        this.sellerPrice = sellerPrice;
    }

    public Boolean getCountered() {
        return isCountered;
    }

    public void setCountered(Boolean countered) {
        isCountered = countered;
    }
}
