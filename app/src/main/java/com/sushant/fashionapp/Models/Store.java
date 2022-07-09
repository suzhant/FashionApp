package com.sushant.fashionapp.Models;

public class Store {
    private String storeName;
    private String storeId;
    private String storeRating;
    private String storePhone;
    private String storeEmail;
    private String storeAddress;
    private String storeVAT;
    private String storeDesc;
    private String ownerId;


    public Store() {
    }

    public Store(StoreBuilder builder) {
        this.storeName = builder.storeName;
        this.storeId = builder.storeId;
        this.storeRating = builder.storeRating;
        this.storePhone = builder.storePhone;
        this.storeEmail = builder.storeEmail;
        this.storeAddress = builder.storeAddress;
        this.storeVAT = builder.storeVAT;
        this.storeDesc = builder.storeDesc;
        this.ownerId = builder.ownerId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getStoreRating() {
        return storeRating;
    }

    public String getStorePhone() {
        return storePhone;
    }

    public String getStoreEmail() {
        return storeEmail;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public String getStoreVAT() {
        return storeVAT;
    }

    public String getStoreDesc() {
        return storeDesc;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public static class StoreBuilder {
        private final String storeName;
        private String storeId;
        private String storeRating;
        private final String storePhone;
        private final String storeEmail;
        private String storeAddress;
        private String storeVAT;
        private String storeDesc;
        private String ownerId;

        public StoreBuilder(String storeName, String storePhone, String storeEmail) {
            this.storeName = storeName;
            this.storePhone = storePhone;
            this.storeEmail = storeEmail;
        }

        public Store.StoreBuilder storeRating(String storeRating) {
            this.storeRating = storeRating;
            return this;
        }

        public Store.StoreBuilder storeId(String storeId) {
            this.storeId = storeId;
            return this;
        }

        public Store.StoreBuilder storeAddress(String storeAddress) {
            this.storeAddress = storeAddress;
            return this;
        }

        public Store.StoreBuilder storeVAT(String storeVAT) {
            this.storeVAT = storeVAT;
            return this;
        }

        public Store.StoreBuilder storeDesc(String storeDesc) {
            this.storeDesc = storeDesc;
            return this;
        }

        public Store.StoreBuilder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }


        //Return the finally consrcuted User object
        public Store build() {
            return new Store(this);
        }
    }

}
