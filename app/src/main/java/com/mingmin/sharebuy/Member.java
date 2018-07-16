package com.mingmin.sharebuy;

import com.mingmin.sharebuy.cloud.MemberDoc;

import java.io.Serializable;

/**
 * Group member, exclude group manager
 */
public class Member implements Serializable {
    private String uid;
    private String name;

    public Member(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public Member(String uid, MemberDoc memberDoc) {
        this.uid = uid;
        setupValues(memberDoc);
    }

    private void setupValues(MemberDoc memberDoc) {
        name = memberDoc.getName();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
