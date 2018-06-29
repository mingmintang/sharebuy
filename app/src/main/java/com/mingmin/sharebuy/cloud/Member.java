package com.mingmin.sharebuy.cloud;

/**
 * Group member, exclude group founder
 */
public class Member {
    private String nickname;

    private Member() {
    }

    public Member(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
