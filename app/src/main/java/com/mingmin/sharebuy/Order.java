package com.mingmin.sharebuy;

import com.mingmin.sharebuy.cloud.OrderDoc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Order has 4 states: create -> take -> end
 *                       ∟--------∟----> cancel
 * create: At least there is imagePath. creatorUid and createTime are created when state is built.
 * take: A user took the order. A takerUid is created when state is built.
 * end: At least there are a buyer, takerUid and createTime. endTime is created when state is built.
 * cancel: Cancel the order, the order could not update again.
 *         Only creator or taker can cancel the order.
 */
public class Order {
    public static final int STATE_CREATE = 0;
    public static final int STATE_TAKE = 1;
    public static final int STATE_END = 2;
    public static final int STATE_CANCEL =3;

    private int state;
    private int maxBuyCount; // 0: no limit max buy count
    private int buyCount;
    private String imageUrl;
    private String id;
    private String creatorUid;
    private String takerUid;
    private String name;
    private String desc;
    private String groupId; // null: Personal Order, not null: Group Order
    private int price;
    private int coinUnit; // index of coin_units string array
    private Date createTime;
    private Date endTime;
    /**
     * Buyers collection doesn't exist when Personal Order, it is used only for Group Order.
     * The buyer would add values to buyers collection after buyCount is updated successfully.
     * key: buyer uid, value: buyer object
     */
    private Map<String, Buyer> buyers;

    public Order() {
        buyers = new HashMap<>();
    }

    public Order(String orderId, OrderDoc orderDoc) {
        this();
        id = orderId;
        setOrderDoc(orderDoc);
    }

    private void updateValues(OrderDoc orderDoc) {
        state = orderDoc.getState();
        maxBuyCount = orderDoc.getMaxBuyCount();
        buyCount = orderDoc.getBuyCount();
        imageUrl = orderDoc.getImageUrl();
        creatorUid = orderDoc.getCreatorUid();
        takerUid = orderDoc.getTakerUid();
        name = orderDoc.getName();
        desc = orderDoc.getDesc();
        groupId = orderDoc.getGroupId();
        price = orderDoc.getPrice();
        coinUnit = orderDoc.getCoinUnit();
        createTime = orderDoc.getCreateTime();
        endTime = orderDoc.getEndTime();
    }

    public void setOrderDoc(OrderDoc orderDoc) {
        updateValues(orderDoc);
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

    public Date getCreateTime() {
        return createTime;
    }

    public Date getEndTime() {
        return endTime;
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
