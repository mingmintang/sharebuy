package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

public class GroupDoc implements Serializable {
    private String name;
    private String founderUid;
    private String founderNickname;
    private @ServerTimestamp Date createTime;
    private int searchCode;

    public GroupDoc() { }

    public GroupDoc(String name, String founderUid, String founderNickname) {
        this.name = name;
        this.founderUid = founderUid;
        this.founderNickname = founderNickname;
        searchCode = new Random(System.currentTimeMillis()).nextInt(999998) + 1;
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

    public String getFounderNickname() {
        return founderNickname;
    }

    public void setFounderNickname(String founderNickname) {
        this.founderNickname = founderNickname;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public int getSearchCode() {
        return searchCode;
    }
}
