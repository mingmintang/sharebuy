package com.mingmin.sharebuy;

import java.util.Random;

public class Group {
    private String id; // Created by FirebaseDatabase push method
    private String name;
    private String founderUid;
    private long createdTime;
    private long nCreatedTime; // Negative version for firebase desc sorting
    private int searchCode;
    private boolean isPublic = false;

    private Group() {
    }

    public Group(String id, String name, String founderUid) {
        this.id = id;
        this.name = name;
        this.founderUid = founderUid;
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
