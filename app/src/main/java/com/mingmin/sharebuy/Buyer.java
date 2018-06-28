package com.mingmin.sharebuy;

public class Buyer {
    private String uid;
    private int orderCount;
    private long orderTime;
    private int buyCount;

    private Buyer() {
    }

    public Buyer(String uid, int orderCount, int buyCount) {
        this.uid = uid;
        this.orderCount = orderCount;
        this.buyCount = buyCount;
        orderTime = System.currentTimeMillis();
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

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }
}
