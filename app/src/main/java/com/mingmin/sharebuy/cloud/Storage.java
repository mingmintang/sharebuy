package com.mingmin.sharebuy.cloud;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class Storage {
    private static Storage instance;
    private FirebaseStorage storage;

    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    private Storage() {
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Create unique file name and storage reference by JAVA UUID
     * @return unique StorageReference
     */
    public StorageReference createOrderImagePathRef() {
        String name = UUID.randomUUID().toString().replace("-", "");
        return storage.getReference().child("order").child(name);
    }
}
