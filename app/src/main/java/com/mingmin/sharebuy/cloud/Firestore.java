package com.mingmin.sharebuy.cloud;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

public class Firestore {
    private static Firestore instance;
    private final FirebaseFirestore db;

    public static Firestore getInstance() {
        if (instance == null) {
            instance = new Firestore();
        }
        return instance;
    }

    public Firestore() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    public WriteBatch getWriteBatch() {
        return db.batch();
    }

    public <TResult> Task<TResult> runTransaction(@NonNull final Transaction.Function<TResult> updateFunction) {
        return db.runTransaction(updateFunction);
    }

    public CollectionReference getUsersCol() {
        return db.collection("users");
    }

    public DocumentReference getUserDoc(String uid) {
        return getUsersCol().document(uid);
    }

    public CollectionReference getUserGroupsCol(String uid) {
        return getUserDoc(uid).collection("groups");
    }

    public DocumentReference getUserGroupDoc(String uid, String groupId) {
        return getUserGroupsCol(uid).document(groupId);
    }

    public CollectionReference getGroupMembersCol(String groupId) {
        return getGroupDoc(groupId).collection("members");
    }

    public DocumentReference getGroupMemberDoc(String groupId, String memberUid) {
        return getGroupMembersCol(groupId).document(memberUid);
    }

    public CollectionReference getGroupJoiningMembersCol(String groupId) {
        return getGroupDoc(groupId).collection("joining");
    }

    public DocumentReference getGroupJoiningMemberDoc(String groupId, String memberUid) {
        return getGroupJoiningMembersCol(groupId).document(memberUid);
    }

    public CollectionReference getGroupsCol() {
        return db.collection("groups");
    }

    public DocumentReference getGroupDoc(String groupId) {
        return getGroupsCol().document(groupId);
    }

    public CollectionReference getRequestJoinGroupCol(String groupId) {
        return getGroupDoc(groupId).collection("requestJoin");
    }

    public CollectionReference getGroupOrdersCol(String groupId) {
        return getGroupDoc(groupId).collection("orders");
    }

    public DocumentReference getGroupOrderDoc(String groupId, String orderId) {
        return getGroupOrdersCol(groupId).document(orderId);
    }

    public CollectionReference getGroupOrderBuyersCol(String groupId, String orderId) {
        return getGroupOrderDoc(groupId, orderId).collection("buyers");
    }

    public DocumentReference getGroupOrderBuyerDoc(String groupId, String orderId, String buyerId) {
        return getGroupOrderBuyersCol(groupId, orderId).document(buyerId);
    }

    public CollectionReference getUserOrdersCol(String uid) {
        return getUserDoc(uid).collection("orders");
    }

    public DocumentReference getUserOrderDoc(String uid, String orderId) {
        return getUserOrdersCol(uid).document(orderId);
    }

    public CollectionReference getUserEndOrdersCol(String uid) {
        return getUserDoc(uid).collection("ends");
    }

    public DocumentReference getUserEndOrderDoc(String uid, String orderId) {
        return getUserEndOrdersCol(uid).document(orderId);
    }
}
