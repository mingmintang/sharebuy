package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class OrderDoc {
    public static final int STATE_CREATE = 0;
    public static final int STATE_TAKE = 1;
    public static final int STATE_END = 2;
    public static final int STATE_CANCEL =3;

    private int state;
    private int maxBuyCount;
    private int buyCount;
    private String imageUrl;
    private String creatorUid;
    private String takerUid;
    private String name;
    private String desc;
    private String groupId;
    private int price;
    private int coinUnit; // index of coin_units string array
    private @ServerTimestamp Date createTime;
    private @ServerTimestamp Date endTime = new Date(0);

    public OrderDoc() {
        initSync();
    }

    private void initSync() {
        setState(STATE_CREATE);
        setMaxBuyCount(0);
        setBuyCount(0);
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

    public Date getCreateTime() {
        return createTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime() {
        endTime = null;
    }
}
