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
    private String sellerId;
    private String storePic;
    private String storeSecondaryEmail;


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
        this.sellerId = builder.sellerId;
        this.storePic = builder.storePic;
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

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getStorePic() {
        return storePic;
    }

    public String getStoreSecondaryEmail() {
        return storeSecondaryEmail;
    }

    public void setStoreSecondaryEmail(String storeSecondaryEmail) {
        this.storeSecondaryEmail = storeSecondaryEmail;
    }

    public static class StoreBuilder {
        //required
        private final String storeName;
        private final String storePhone;
        private final String storeEmail;
        //optional
        private String storeId;
        private String storeRating;
        private String storeAddress;
        private String storeVAT;
        private String storeDesc;
        private String sellerId;
        private String storePic;
        private String storeSecondaryEmail;

        //required fields
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
            this.sellerId = ownerId;
            return this;
        }

        public Store.StoreBuilder storePic(String storePic) {
            this.storePic = storePic;
            return this;
        }

        public Store.StoreBuilder storeSecondaryEmail(String storeSecondaryEmail) {
            this.storeSecondaryEmail = storeSecondaryEmail;
            return this;
        }


        //Return the finally consrcuted Store object
        public Store build() {
            return new Store(this);
        }
    }

}
