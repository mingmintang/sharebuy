package com.mingmin.sharebuy.database;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(tableName = "buyers", primaryKeys = {"orderId", "uid"})
public class Buyer {
    @NonNull
    private String orderId;
    @NonNull
    private String uid;
    private int orderCount;
    private long orderTime;
    private int buyCount;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(long orderTime) {
        this.orderTime = orderTime;
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }
}
