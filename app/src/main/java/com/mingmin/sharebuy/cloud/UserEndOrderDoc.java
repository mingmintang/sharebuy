package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserEndOrderDoc {
    public static class Personal {
        private int buyCount;
        private String imageUrl;
        private String name;
        private String desc;
        private int price;
        private int coinUnit; // index of coin_units string array

        public Personal() { }

        public Personal(int buyCount, String name, String desc, int price, int coinUnit) {
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

    private @ServerTimestamp Date updateTime = new Date(0);
    private String groupId;
    private Personal personal;

    public UserEndOrderDoc() {
        setUpdateTime();
    }

    public UserEndOrderDoc(String groupId) {
        this.groupId = groupId;
        setUpdateTime();
    }

    public UserEndOrderDoc(Personal personal) {
        this.personal = personal;
        setUpdateTime();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime() {
        this.updateTime = null;
    }

    public Personal getPersonal() {
        return personal;
    }

    public void setPersonal(Personal personal) {
        this.personal = personal;
    }
}
