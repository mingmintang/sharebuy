package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

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

    public CollectionReference getUsersRef() {
        return db.collection("users");
    }

    public DocumentReference getUserInfoRef(String uid) {
        return getUsersRef()
                .document(uid + "/info/nickname");
    }

    public DocumentReference getUserRef(String uid) {
        return getUsersRef().document(uid);
    }
}
