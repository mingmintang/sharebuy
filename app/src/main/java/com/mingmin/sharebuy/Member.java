package com.mingmin.sharebuy;

import com.mingmin.sharebuy.cloud.MemberDoc;

/**
 * Group member, exclude group founder
 */
public class Member {
    private String uid;
    private String nickname;

    public Member(String uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
    }

    public Member(String uid, MemberDoc memberDoc) {
        this.uid = uid;
        updateValues(memberDoc);
    }

    private void updateValues(MemberDoc memberDoc) {
        setNickname(memberDoc.getNickname());
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setMemberDoc(MemberDoc memberDoc) {
        updateValues(memberDoc);
    }
}
