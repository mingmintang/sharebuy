package com.mingmin.sharebuy.cloud;

public class MemberDoc {
    private String nickname;
    private boolean joined;

    public MemberDoc() {
    }

    public MemberDoc(String nickname, boolean joined) {
        this.nickname = nickname;
        this.joined = joined;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }
}
