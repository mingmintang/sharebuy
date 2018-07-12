package com.mingmin.sharebuy;

import com.mingmin.sharebuy.cloud.BuyerDoc;

import java.util.Date;

public class Buyer {
    private String uid;
    private int orderCount;
    private Date orderTime;
    private int buyCount;

    public Buyer(String uid, BuyerDoc buyerDoc) {
        this.uid = uid;
        setBuyerDoc(buyerDoc);
    }

    private void updateValues(BuyerDoc buyerDoc) {
        orderCount = buyerDoc.getOrderCount();
        orderTime = buyerDoc.getOrderTime();
        buyCount = buyerDoc.getBuyCount();
    }

    public void setBuyerDoc(BuyerDoc buyerDoc) {
        updateValues(buyerDoc);
    }

    public String getUid() {
        return uid;
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
