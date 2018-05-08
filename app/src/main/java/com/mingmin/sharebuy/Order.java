package com.mingmin.sharebuy;

import java.util.ArrayList;

public class Order {
    private long id;
    private String uid;
    private String name;
    private String imagePath;
    private String imageUrl;
    private int price;
    private int count;
    private long startTime;
    private long endTime;
    private int buyerCount;
    private long nStartTime; // Negative version for firebase desc sorting
    private long nEndTime; // Negative version for firebase desc sorting

    private Order() {
    }

    public Order(String uid) {
        id = System.currentTimeMillis();
        this.uid = uid;
    }

    public long getId() {
        return id;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
        nStartTime = -startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        nEndTime = -endTime;
    }

    public int getBuyerCount() {
        return buyerCount;
    }

    public long getnStartTime() {
        return nStartTime;
    }

    public long getnEndTime() {
        return nEndTime;
    }
}
