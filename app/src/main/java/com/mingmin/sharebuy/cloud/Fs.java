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

    public CollectionReference getGroupsCol() {
        return db.collection("groups");
    }

    public DocumentReference getGroupDoc(String groupId) {
        return getGroupsCol().document(groupId);
    }

    public CollectionReference getGroupMembersCol(String groupId) {
        return getGroupDoc(groupId).collection("members");
    }

    public DocumentReference getGroupMemberDoc(String groupId, String uid) {
        return getGroupMembersCol(groupId).document(uid);
    }

    public CollectionReference getRequestJoinGroupCol(String groupId) {
        return getGroupDoc(groupId).collection("requestJoin");
    }
}
