package com.mingmin.sharebuy.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "endOrders")
public class EndOrder {
    public static final int STATE_CREATE = 0;
    public static final int STATE_TAKE = 1;
    public static final int STATE_END = 2;
    public static final int STATE_CANCEL =3;

    @PrimaryKey
    @NonNull
    private String id;
    private int state;
    private int maxBuyCount;
    private int buyCount;
    private String imagePath;
    private String imageUrl;
    private String creatorUid;
    private String takerUid;
    private String name;
    private String desc;
    private String groupId;
    private int price;
    private int coinUnit; // index of coin_units string array
    private long createTime;
    private long endTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getMaxBuyCount() {
        return maxBuyCount;
    }

    public void setMaxBuyCount(int maxBuyCount) {
        this.maxBuyCount = maxBuyCount;
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
