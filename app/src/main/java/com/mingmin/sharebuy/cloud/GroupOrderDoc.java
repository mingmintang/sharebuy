package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class GroupOrderDoc {
    private int state;
    private int maxBuyCount;
    private int buyCount;
    private String imageUrl;
    private String managerUid;
    private String managerName;
    private String name;
    private String desc;
    private String groupId;
    private int price;
    private int coinUnit; // index of coin_units string array
    private @ServerTimestamp Date createTime = new Date(0);
    private @ServerTimestamp Date updateTime = new Date(0);

    public GroupOrderDoc() {
        setCreateTime();
        setUpdateTime();
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

    public String getManagerUid() {
        return managerUid;
    }

    public void setManagerUid(String managerUid) {
        this.managerUid = managerUid;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
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

    private void setCreateTime() {
        createTime = null;
    }

    public Date getCreateTime() {
        return createTime;
    }

    private void setUpdateTime() {
        updateTime = null;
    }

    public Date getUpdateTime() {
        return updateTime;
    }
}
