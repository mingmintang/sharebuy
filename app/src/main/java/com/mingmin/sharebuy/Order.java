package com.mingmin.sharebuy;

import com.google.firebase.database.Exclude;

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

    public static final String KEY_STATE = "state";
    public static final String KEY_MAX_BUY_COUNT = "maxBuyCount";
    public static final String KEY_BUY_COUNT = "buyCount";

    /**
     * Need synchronize value for buyers and taker.
     */
    private Map<String, Integer> sync;
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
    private long createTime;
    private long endTime;
    private long nCreateTime; // Negative version for firebase desc sorting
    private long nEndTime; // Negative version for firebase desc sorting
    private Map<String, Buyer> buyers; // key: buyer uid, value: buyer object

    private Order() {
    }

    public Order(String imagePath) {
        this.imagePath = imagePath;
        setCreateTime();
        buyers = new HashMap<>();
        initSync();
    }

    private void initSync() {
        sync = new HashMap<>();
        setState(STATE_CREATE);
        setMaxBuyCount(-1);
        setBuyCount(0);
    }

    public Map<String, Integer> getSync() {
        return sync;
    }

    @Exclude
    public int getState() {
        return sync.get(KEY_STATE);
    }

    @Exclude
    public void setState(int state) {
        sync.put(KEY_STATE, state);
    }

    /**
     * -1: no limit max buy count
     */
    @Exclude
    public int getMaxBuyCount() {
        return sync.get(KEY_MAX_BUY_COUNT);
    }

    @Exclude
    public void setMaxBuyCount(int maxBuyCount) {
        sync.put(KEY_MAX_BUY_COUNT, maxBuyCount);
    }

    @Exclude
    public int getBuyCount() {
        return sync.get(KEY_BUY_COUNT);
    }

    /**
     * The buyer would add values to child buyers after buyCount is updated successfully.
     */
    @Exclude
    public void setBuyCount(int buyCount) {
        sync.put(KEY_BUY_COUNT, buyCount);
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

    public long getCreateTime() {
        return createTime;
    }

    private void setCreateTime() {
        createTime = System.currentTimeMillis();
        nCreateTime = -createTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        nEndTime = -endTime;
    }

    public long getnStartTime() {
        return nCreateTime;
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
