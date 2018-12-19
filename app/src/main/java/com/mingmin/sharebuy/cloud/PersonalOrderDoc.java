package com.mingmin.sharebuy.cloud;

public class PersonalOrderDoc {
    private int buyCount;
    private String imageUrl;
    private String name;
    private String desc;
    private int price;
    private int coinUnit; // index of coin_units string array

    public PersonalOrderDoc() { }

    public PersonalOrderDoc(int buyCount, String name, String desc, int price, int coinUnit) {
        this.buyCount = buyCount;
        this.name = name;
        this.desc = desc;
        this.price = price;
        this.coinUnit = coinUnit;
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
}
