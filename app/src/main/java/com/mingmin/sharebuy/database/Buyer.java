package com.mingmin.sharebuy.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.mingmin.sharebuy.cloud.BuyerDoc;

import java.util.Date;

@Entity(tableName = "buyers", primaryKeys = {"orderId", "uid"})
@TypeConverters({DateTypeConverter.class})
public class Buyer {
    @NonNull
    private String orderId;
    @NonNull
    private String uid;
    private String name;
    private int orderCount;
    private Date orderTime;
    private int buyCount;

    public void setupBuyerDocValues(String orderId, String uid, BuyerDoc buyerDoc) {
        this.orderId = orderId;
        this.uid = uid;
        this.name = buyerDoc.getName();
        this.orderCount = buyerDoc.getOrderCount();
        this.orderTime = buyerDoc.getOrderTime();
        this.buyCount = buyerDoc.getBuyCount();
    }

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

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }
}
