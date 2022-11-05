package com.sushant.fashionapp.Models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Product {
    private String pId, pName, storeName, size, maxLimit, color, desc, pPic, brandName, variantPId;
    private Integer pPrice, stock, love, quantity, productCode, variantIndex, sizeIndex;
    private ArrayList<Product> variants;
    private ArrayList<Product> sizes;
    private ArrayList<String> photos;
    private String previewPic;
    private String category, subCategory;

    public Product() {
    }

    public Product(String pId, String pName, String pPic, Integer pPrice, String storeName, Integer stock) {
        this.pId = pId;
        this.pName = pName;
        this.pPic = pPic;
        this.pPrice = pPrice;
        this.storeName = storeName;
        this.stock = stock;
    }

    public Product(String pId, String size, Integer pPrice, Integer stock) {
        this.pId = pId;
        this.size = size;
        this.pPrice = pPrice;
        this.stock = stock;
    }

    public Product(String pId, String pName, String storeName, String previewPic) {
        this.pId = pId;
        this.pName = pName;
        this.storeName = storeName;
        this.previewPic = previewPic;
    }

    public Product(String pId, String color, String pPic) {
        this.pId = pId;
        this.color = color;
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



    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return pPrice == product.pPrice && pPic == product.pPic && love == product.love && stock == product.stock
                && Objects.equals(pId, product.pId) && Objects.equals(pName, product.pName)
                && Objects.equals(storeName, product.storeName) && quantity == product.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pId, pName, storeName, pPrice, pPic, love, stock);
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(String maxLimit) {
        this.maxLimit = maxLimit;
    }

    public ArrayList<Product> getVariants() {
        return variants;
    }

    public void setVariants(ArrayList<Product> variants) {
        this.variants = variants;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ArrayList<Product> getSizes() {
        return sizes;
    }

    public void setSizes(ArrayList<Product> sizes) {
        this.sizes = sizes;
    }

    public Integer getpPrice() {
        return pPrice;
    }

    public void setpPrice(Integer pPrice) {
        this.pPrice = pPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
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

    public int getTotalVariant() {
        return variants.size();
    }
}
