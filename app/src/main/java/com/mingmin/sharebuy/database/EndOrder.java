package com.mingmin.sharebuy.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.mingmin.sharebuy.Order;
import com.mingmin.sharebuy.cloud.GroupOrderDoc;
import com.mingmin.sharebuy.cloud.UserEndOrderDoc;

import java.util.Date;

@Entity(tableName = "endOrders")
@TypeConverters({DateTypeConverter.class})
public class EndOrder {
    @PrimaryKey
    @NonNull
    private String id;
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
    private int coinUnit;
    private Date createTime;
    private Date updateTime;

    public void setupPersonalOrderValues(String orderId, Date updateTime, UserEndOrderDoc.Personal personalOrder) {
        this.id = orderId;
        this.state = Order.STATE_END;
        this.maxBuyCount = personalOrder.getBuyCount();
        this.buyCount = personalOrder.getBuyCount();
        this.imageUrl = personalOrder.getImageUrl();
        this.name = personalOrder.getName();
        this.desc = personalOrder.getDesc();
        this.price = personalOrder.getPrice();
        this.coinUnit = personalOrder.getCoinUnit();
        this.createTime = updateTime;
        this.updateTime = updateTime;
    }

    public void setupGroupOrderValues(String orderId, String groupId, Date updateTime, GroupOrderDoc groupOrderDoc) {
        this.id = orderId;
        this.state = groupOrderDoc.getState();
        this.maxBuyCount = groupOrderDoc.getMaxBuyCount();
        this.buyCount = groupOrderDoc.getBuyCount();
        this.imageUrl = groupOrderDoc.getImageUrl();
        this.managerUid = groupOrderDoc.getManagerUid();
        this.managerName = groupOrderDoc.getManagerName();
        this.name = groupOrderDoc.getName();
        this.desc = groupOrderDoc.getDesc();
        this.groupId = groupId;
        this.price = groupOrderDoc.getPrice();
        this.coinUnit = groupOrderDoc.getCoinUnit();
        this.createTime = groupOrderDoc.getCreateTime();
        this.updateTime = updateTime;
    }

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
