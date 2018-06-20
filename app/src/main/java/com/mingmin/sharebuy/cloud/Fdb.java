package com.mingmin.sharebuy.cloud;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Fdb {
    private static final FirebaseDatabase fdb = FirebaseDatabase.getInstance();

    public static DatabaseReference getUserInfoRef(String uid) {
        return fdb.getReference("users")
                .child(uid)
                .child("userInfo");
    }

    public static DatabaseReference getNicknameRef(String uid) {
        return fdb.getReference("users")
                .child(uid)
                .child("userInfo")
                .child("nickname");
    }

    public static DatabaseReference getGroupsRef() {
        return fdb.getReference("groups");
    }

    public static DatabaseReference getGroupRef(String groupId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("group");
    }

    public static DatabaseReference getGroupMembersRef(String groupId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("members");
    }

    public static DatabaseReference getGroupMemberRef(String groupId, String uid) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("members")
                .child(uid);
    }

    public static DatabaseReference getUserGroupsRef(String uid) {
        return fdb.getReference("users")
                .child(uid)
                .child("groups");
    }

    public static DatabaseReference getUserGroupRef(String uid, String groupId) {
        return fdb.getReference("users")
                .child(uid)
                .child("groups")
                .child(groupId);
    }

    public static DatabaseReference getRequestJoinGroupRef(String groupId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("notify")
                .child("requestJoinGroup");
    }

    public static DatabaseReference getGroupOrdersRef(String groupId) {
        return fdb.getReference("groups")
                .child(groupId)
                .child("orders");
    }

    public static DatabaseReference getUserOrdersRef(String uid) {
        return fdb.getReference("users")
                .child(uid)
                .child("orders");
    }
}
