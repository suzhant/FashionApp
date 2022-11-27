package com.sushant.fashionapp.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Product implements Serializable {
    private String pId, pName, maxLimit, desc, pPic, brandName, variantPId;
    private Integer pPrice, love, quantity, productCode, variantIndex, sizeIndex, bargainPrice;
    private ArrayList<Variants> variants;
    private ArrayList<Size> sizes;
    private ArrayList<String> photos;
    private Long timeStamp;
    private String previewPic;
    private String category, subCategory, subSubCategory;
    private String season;
    private String storeId;

    public Product() {
    }

    public Product(String pId, String pName, String pPic, Integer pPrice) {
        this.pId = pId;
        this.pName = pName;
        this.pPic = pPic;
        this.pPrice = pPrice;
    }

    public Product(String pId, Integer pPrice) {
        this.pId = pId;
        this.pPrice = pPrice;
    }

    public Product(String pId, String pName, String previewPic) {
        this.pId = pId;
        this.pName = pName;
        this.previewPic = previewPic;
    }

    public Product(String pId, String pPic) {
        this.pId = pId;
        this.pPic = pPic;
    }


    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return pPrice == product.pPrice && pPic == product.pPic && love == product.love
                && Objects.equals(pId, product.pId) && Objects.equals(pName, product.pName)
                && quantity == product.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pId, pName, pPrice, pPic, love);
    }


    public String getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(String maxLimit) {
        this.maxLimit = maxLimit;
    }

    public ArrayList<Variants> getVariants() {
        return variants;
    }

    public void setVariants(ArrayList<Variants> variants) {
        this.variants = variants;
    }


    public ArrayList<Size> getSizes() {
        return sizes;
    }

    public void setSizes(ArrayList<Size> sizes) {
        this.sizes = sizes;
    }

    public Integer getpPrice() {
        return pPrice;
    }

    public void setpPrice(Integer pPrice) {
        this.pPrice = pPrice;
    }

    public Integer getLove() {
        return love;
    }

    public void setLove(Integer love) {
        this.love = love;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPreviewPic() {
        return previewPic;
    }

    public void setPreviewPic(String previewPic) {
        this.previewPic = previewPic;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<String> photos) {
        this.photos = photos;
    }

    public String getpPic() {
        return pPic;
    }

    public void setpPic(String pPic) {
        this.pPic = pPic;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getVariantPId() {
        return variantPId;
    }

    public void setVariantPId(String variantPId) {
        this.variantPId = variantPId;
    }

    public Integer getVariantIndex() {
        return variantIndex;
    }

    public void setVariantIndex(Integer variantIndex) {
        this.variantIndex = variantIndex;
    }

    public Integer getSizeIndex() {
        return sizeIndex;
    }

    public void setSizeIndex(Integer sizeIndex) {
        this.sizeIndex = sizeIndex;
    }

    public static Comparator<Product> ascending = new Comparator<Product>() {
        @Override
        public int compare(Product product, Product t1) {
            return product.getpName().compareTo(t1.getpName());
        }
    };


    public static Comparator<Product> descending = new Comparator<Product>() {
        @Override
        public int compare(Product product, Product t1) {
            return t1.getpName().compareTo(product.getpName());
        }
    };

    public static Comparator<Product> lowToHigh = new Comparator<Product>() {
        @Override
        public int compare(Product product, Product t1) {
            return product.getpPrice().compareTo(t1.getpPrice());
        }
    };

    public static Comparator<Product> highToLow = new Comparator<Product>() {
        @Override
        public int compare(Product product, Product t1) {
            return t1.getpPrice().compareTo(product.getpPrice());
        }
    };

    public static Comparator<Product> oldToNew = new Comparator<Product>() {
        @Override
        public int compare(Product product, Product t1) {
            return product.getTimeStamp().compareTo(t1.getTimeStamp());
        }
    };

    public static Comparator<Product> newToOld = new Comparator<Product>() {
        @Override
        public int compare(Product product, Product t1) {
            return t1.getTimeStamp().compareTo(product.getTimeStamp());
        }
    };


    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSubSubCategory() {
        return subSubCategory;
    }

    public void setSubSubCategory(String subSubCategory) {
        this.subSubCategory = subSubCategory;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Integer getBargainPrice() {
        return bargainPrice;
    }

    public void setBargainPrice(Integer bargainPrice) {
        this.bargainPrice = bargainPrice;
    }
}
