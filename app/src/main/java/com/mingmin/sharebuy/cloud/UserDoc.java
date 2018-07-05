package com.mingmin.sharebuy.cloud;

import java.util.HashMap;
import java.util.Map;

public class UserDoc {
    String nickname;
    Map<String, Boolean> tokens;

    public UserDoc() {
        tokens = new HashMap<>();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Map<String, Boolean> getTokens() {
        return tokens;
    }

    public void setTokens(Map<String, Boolean> tokens) {
        this.tokens = tokens;
    }

    public void addToken(String token) {
        tokens.put(token, true);
    }
}
