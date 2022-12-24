package com.sushant.fashionapp.Models;

public class Invoice {
    private String invoiceNo, invoiceUrl, orderId;
    private Long invoiceDate;

    public Invoice() {
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Long invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
}
