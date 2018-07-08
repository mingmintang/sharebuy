package com.mingmin.sharebuy.cloud;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Fdb {
    private final FirebaseDatabase fdb;
    private static Fdb instance;


    public static Fdb getInstance() {
        if (instance == null) {
            instance = new Fdb();
        }
        return instance;
    }

    private Fdb() {
        fdb = FirebaseDatabase.getInstance();
    }

    public DatabaseReference getConnectStatusRef() {
        return fdb.getReference(".info/connected");
    }

    public DatabaseReference getUserInfoRef(String uid) {
        return fdb.getReference("users")
                .child(uid)
                .child("userInfo");
    }

    public DatabaseReference getUserNicknameRef(String uid) {
        return fdb.getReference("users")
                .child(uid)
                .child("userInfo")
                .child("nickname");
    }

    public DatabaseReference getUserTokenRef(String uid, String token) {
        return fdb.getReference("users")
                .child(uid)
                .child("tokens")
                .child(token);
    }

    public DatabaseReference getUserEndOrdersRef(String uid) {
        return fdb.getReference("users")
                .child(uid)
                .child("endOrders");
    }

    public DatabaseReference getGroupsRef() {
        return fdb.getReference("groups");
    }

    public DatabaseReference getGroupRef(String groupId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("group");
    }

    public DatabaseReference getGroupMembersRef(String groupId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("members");
    }

    public DatabaseReference getGroupMemberRef(String groupId, String uid) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("members")
                .child(uid);
    }

    public DatabaseReference getUserGroupsRef(String uid) {
        return fdb.getReference("users")
                .child(uid)
                .child("groups");
    }

    public DatabaseReference getUserGroupRef(String uid, String groupId) {
        return fdb.getReference("users")
                .child(uid)
                .child("groups")
                .child(groupId);
    }

    public DatabaseReference getRequestJoinGroupRef(String groupId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("notify")
                .child("requestJoinGroup");
    }

    public DatabaseReference getGroupOrdersRef(String groupId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("orders");
    }

    public DatabaseReference getGroupOrderRef(String groupId, String orderId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("orders")
                .child(orderId);
    }

    public DatabaseReference getGroupOrderBuyCountRef(String groupId, String orderId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("orders")
                .child(orderId)
                .child("sync/buyCount");
    }

    public DatabaseReference getGroupOrderSyncRef(String groupId, String orderId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("orders")
                .child(orderId)
                .child("sync");
    }

    public DatabaseReference getGroupOrderBuyersRef(String groupId, String orderId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("orders")
                .child(orderId)
                .child("buyers");
    }

    public DatabaseReference getUserOrdersRef(String uid) {
        return fdb.getReference("users")
                .child(uid)
                .child("orders");
    }
}
