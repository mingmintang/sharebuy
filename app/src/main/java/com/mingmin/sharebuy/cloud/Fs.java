package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.WriteBatch;

public class Fs {
    private static Fs instance;
    private final FirebaseFirestore db;

    public static Fs getInstance() {
        if (instance == null) {
            instance = new Fs();
        }
        return instance;
    }

    public Fs() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    public WriteBatch getWriteBatch() {
        return db.batch();
    }

    public CollectionReference getUsersRef() {
        return db.collection("users");
    }

    public DocumentReference getUserRef(String uid) {
        return getUsersRef().document(uid);
    }

    public CollectionReference getUserGroupsRef(String uid) {
        return getUserRef(uid).collection("groups");
    }

    public DocumentReference getUserGroupRef(String uid, String groupId) {
        return getUserGroupsRef(uid).document(groupId);
    }

    public CollectionReference getGroupsRef() {
        return db.collection("groups");
    }

    public DocumentReference getGroupRef(String groupId) {
        return getGroupsRef().document(groupId);
    }

    public CollectionReference getGroupMembersRef(String groupId) {
        return getGroupRef(groupId).collection("members");
    }

    public DocumentReference getGroupMemberRef(String groupId, String uid) {
        return getGroupMembersRef(groupId).document(uid);
    }

    public CollectionReference getRequestJoinGroupRef(String groupId) {
        return getGroupRef(groupId).collection("requestJoin");
    }
}
