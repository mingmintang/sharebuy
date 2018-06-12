package com.mingmin.sharebuy;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Order has 4 states: create -> take -> end
 *                       ∟--------∟----> cancel
 * create: At least there is imagePath. creatorUid and startTime are created when state is built.
 * take: A user took the order. A takerUid is created when state is built.
 * end: At least there are a buyer, takerUid and startTime. endTime is created when state is built.
 * cancel: Cancel the order, the order could not update again.
 *         Only creator or taker can cancel the order.
 */
public class Order {
    public static final int STATE_CRATE = 0;
    public static final int STATE_TAKE = 1;
    public static final int STATE_END = 2;
    public static final int STATE_CANCEL =3;

    private int state;
    private String imagePath;
    private String imageUrl;
    private String id;
    private String creatorUid;
    private String takerUid;
    private String name;
    private String desc;
    private String groupId;
    private int price;
    private int coinUnit; // index of coin_units string array
    private int maxBuyCount;
    private long startTime;
    private long endTime;
    private long nStartTime; // Negative version for firebase desc sorting
    private long nEndTime; // Negative version for firebase desc sorting
    private Map<String, Buyer> buyers; // key: buyer uid, value: buyer object

    private Order() {
    }

    public Order(String imagePath) {
        this.imagePath = imagePath;
        setStartTime();
        buyers = new HashMap<>();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public String getTakerUid() {
        return takerUid;
    }

    public void setTakerUid(String takerUid) {
        this.takerUid = takerUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getCoinUnit() {
        return coinUnit;
    }

    public void setCoinUnit(int coinUnit) {
        this.coinUnit = coinUnit;
    }

    public int getBuyCount() {
        int count = 0;
        if (buyers != null && !buyers.isEmpty()) {
            for (Buyer buyer : buyers.values()) {
                count += buyer.getBuyCount();
            }
        }
        return count;
    }

    public int getMaxBuyCount() {
        return maxBuyCount;
    }

    public void setMaxBuyCount(int maxBuyCount) {
        this.maxBuyCount = maxBuyCount;
    }

    public long getStartTime() {
        return startTime;
    }

    private void setStartTime() {
        startTime = System.currentTimeMillis();
        nStartTime = -startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        nEndTime = -endTime;
    }

    public long getnStartTime() {
        return nStartTime;
    }

    public long getnEndTime() {
        return nEndTime;
    }

    public Map<String, Buyer> getBuyers() {
        return buyers;
    }

    public void addBuyer(Buyer buyer) {
        buyers.put(buyer.getUid(), buyer);
    }

    public void setBuyers(Map<String, Buyer> buyers) {
        this.buyers = buyers;
    }
}
