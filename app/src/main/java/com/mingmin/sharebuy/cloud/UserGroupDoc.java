package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserGroupDoc {
    private String name;
    private String managerUid;
    private String managerName;
    private @ServerTimestamp Date joinTime;
    private String myName; // myName is null if user is manager.

    public UserGroupDoc() { }

    // For group manager, do not save myName
    public UserGroupDoc(String name, String managerUid, String managerName) {
        this.name = name;
        this.managerUid = managerUid;
        this.managerName = managerName;
    }

    // For group member
    public UserGroupDoc(String name, String managerUid, String managerName, String myName) {
        this.name = name;
        this.managerUid = managerUid;
        this.managerName = managerName;
        this.myName = myName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManagerUid() {
        return managerUid;
    }

    public void setManagerUid(String managerUid) {
        this.managerUid = managerUid;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Date getJoinTime() {
        return joinTime;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }
}
