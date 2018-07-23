package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

public class GroupDoc implements Serializable {
    private String name;
    private String managerUid;
    private String managerName;
    private @ServerTimestamp Date createTime;
    private int searchCode;

    public GroupDoc() { }

    public GroupDoc(String name, String managerUid, String managerName) {
        this.name = name;
        this.managerUid = managerUid;
        this.managerName = managerName;
        searchCode = new Random(System.currentTimeMillis()).nextInt(999998) + 1;
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

    public Date getCreateTime() {
        return createTime;
    }

    public int getSearchCode() {
        return searchCode;
    }
}
