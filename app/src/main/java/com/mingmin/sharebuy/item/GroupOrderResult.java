package com.mingmin.sharebuy.item;

import com.mingmin.sharebuy.cloud.GroupOrderDoc;

import java.io.Serializable;

public class GroupOrderResult implements Serializable {
    public int orderState;
    public String imagePath;
    public String uid;
    public GroupOrderDoc groupOrderDoc;
    public Group group;

    public GroupOrderResult(int orderState, String imagePath, String uid, GroupOrderDoc groupOrderDoc, Group group) {
        this.orderState = orderState;
        this.imagePath = imagePath;
        this.uid = uid;
        this.groupOrderDoc = groupOrderDoc;
        this.group = group;
    }
}
