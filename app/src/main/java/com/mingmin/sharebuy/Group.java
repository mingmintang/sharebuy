package com.mingmin.sharebuy;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;

import java.util.Random;

public class Group {
    private String id; // Created by FirebaseDatabase push method
    private String name;
    private String founderUid;
    private String founderName;
    private long createdTime;
    private long nCreatedTime; // Negative version for firebase desc sorting
    private int searchCode;
    private boolean isPublic = false;

    private Group() {
    }

    public Group(String id, String name, String founderUid, String founderName) {
        this.id = id;
        this.name = name;
        this.founderUid = founderUid;
        this.founderName = founderName;
        createdTime = System.currentTimeMillis();
        nCreatedTime = -createdTime;
        searchCode = new Random(createdTime).nextInt(999998) + 1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFounderUid() {
        return founderUid;
    }

    public void setFounderUid(String founderUid) {
        this.founderUid = founderUid;
    }

    public String getFounderName() {
        return founderName;
    }

    public void setFounderName(String founderName) {
        this.founderName = founderName;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getnCreatedTime() {
        return nCreatedTime;
    }

    public int getSearchCode() {
        return searchCode;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    @Override
    public String toString() {
        return getName() + " (" + getSearchCode() + ")";
    }
}
