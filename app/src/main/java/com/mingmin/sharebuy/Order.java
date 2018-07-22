package com.mingmin.sharebuy;

import com.mingmin.sharebuy.cloud.GroupOrderDoc;
import com.mingmin.sharebuy.cloud.UserOrderDoc;

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

    private int state; // There is only end state in Personal Order.
    private int maxBuyCount; // 0: no limit max buy count
    private int buyCount;
    private String imageUrl;
    private String id;
    private String managerUid; // null: Personal Order, not null: Group Order
    private String managerName; // null: Personal Order, not null: Group Order
    private String name;
    private String desc;
    private String groupId; // null: Personal Order, not null: Group Order
    private int price;
    private int coinUnit; // index of coin_units string array
    private Date createTime; // If Personal Order, createTime = updateTime
    private Date updateTime;
    /**
     * Buyers collection doesn't exist when Personal Order, it is used only for Group Order.
     * The buyer would add values to buyers collection after buyCount is updated successfully.
     * key: buyer uid, value: buyer object
     */
    private HashMap<String, Buyer> buyers;
    private String myName;

    public Order(String orderId, int state, int maxBuyCount, int buyCount, String imageUrl,
                 String managerUid, String managerName, String name, String desc, String groupId,
                 int price, int coinUnit, Date createTime, Date updateTime) {
        id = orderId;
        buyers = new HashMap<>();
        this.state = state;
        this.maxBuyCount = maxBuyCount;
        this.buyCount = buyCount;
        this.imageUrl = imageUrl;
        this.managerUid = managerUid;
        this.managerName = managerName;
        this.name = name;
        this.desc = desc;
        this.groupId = groupId;
        this.price = price;
        this.coinUnit = coinUnit;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Order(String orderId, GroupOrderDoc groupOrderDoc) {
        id = orderId;
        buyers = new HashMap<>();
        setupValues(groupOrderDoc);
    }

    public Order(String id, UserOrderDoc userOrderDoc) {
        this.id = id;
        updateTime = userOrderDoc.getUpdateTime();
        myName = userOrderDoc.getMyName();
        groupId = userOrderDoc.getGroupId();
    }

    private void setupValues(GroupOrderDoc groupOrderDoc) {
        state = groupOrderDoc.getState();
        maxBuyCount = groupOrderDoc.getMaxBuyCount();
        buyCount = groupOrderDoc.getBuyCount();
        imageUrl = groupOrderDoc.getImageUrl();
        managerUid = groupOrderDoc.getManagerUid();
        managerName = groupOrderDoc.getManagerName();
        name = groupOrderDoc.getName();
        desc = groupOrderDoc.getDesc();
        groupId = groupOrderDoc.getGroupId();
        price = groupOrderDoc.getPrice();
        coinUnit = groupOrderDoc.getCoinUnit();
        createTime = groupOrderDoc.getCreateTime();
        updateTime = groupOrderDoc.getUpdateTime();
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public Map<String, Buyer> getBuyers() {
        return buyers;
    }

    public void addBuyer(Buyer buyer) {
        buyers.put(buyer.getUid(), buyer);
    }

    public void setBuyers(HashMap<String, Buyer> buyers) {
        this.buyers = buyers;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }
}
