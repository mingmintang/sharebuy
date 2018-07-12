package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class BuyerDoc {
    private int orderCount;
    private @ServerTimestamp Date orderTime;
    private int buyCount;

    public BuyerDoc() { }

    public BuyerDoc(int orderCount, int buyCount) {
        this.orderCount = orderCount;
        this.buyCount = buyCount;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
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
