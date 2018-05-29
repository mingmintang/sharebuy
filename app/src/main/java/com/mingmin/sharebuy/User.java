package com.mingmin.sharebuy;

public class User {
    private String uid;
    private String nickname;

    public User(String uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
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
}
