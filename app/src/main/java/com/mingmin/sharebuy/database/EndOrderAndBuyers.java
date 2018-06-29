package com.mingmin.sharebuy.database;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class EndOrderAndBuyers {
    @Embedded
    private EndOrder endOrder;
    @Relation(parentColumn = "id", entityColumn = "orderId")
    private List<Buyer> buyers = new ArrayList<>();

    public EndOrder getEndOrder() {
        return endOrder;
    }

    public void setEndOrder(EndOrder endOrder) {
        this.endOrder = endOrder;
    }

    public List<Buyer> getBuyers() {
        return buyers;
    }

    public void setBuyers(List<Buyer> buyers) {
        this.buyers = buyers;
    }
}
