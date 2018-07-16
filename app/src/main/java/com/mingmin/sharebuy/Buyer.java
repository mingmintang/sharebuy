package com.mingmin.sharebuy;

import com.mingmin.sharebuy.cloud.BuyerDoc;

import java.util.Date;

public class Buyer {
    private String uid;
    private String name;
    private int orderCount;
    private Date orderTime;
    private int buyCount;

    public Buyer(String uid, String name, int buyCount) {
        this.uid = uid;
        this.name = name;
        this.buyCount = buyCount;
    }

    public Buyer(String uid, BuyerDoc buyerDoc) {
        this.uid = uid;
        setupValues(buyerDoc);
    }

    private void setupValues(BuyerDoc buyerDoc) {
        name = buyerDoc.getName();
        orderCount = buyerDoc.getOrderCount();
        buyCount = buyerDoc.getBuyCount();
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }
}
