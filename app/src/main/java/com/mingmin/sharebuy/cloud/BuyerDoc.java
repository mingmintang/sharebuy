package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class BuyerDoc {
    private String name;
    private int orderCount;
    private @ServerTimestamp Date orderTime;
    private int buyCount;

    public BuyerDoc() { }

    public BuyerDoc(String name, int orderCount, int buyCount) {
        this.name = name;
        this.orderCount = orderCount;
        this.buyCount = buyCount;
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
