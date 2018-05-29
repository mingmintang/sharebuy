package com.mingmin.sharebuy.notification;

public class Notification {
    public static final int ACTION_REQUEST_JOIN_GROUP = 1;
    public static final int ACTION_ACCEPT_JOIN_GROUP = 2;
    public static final int ACTION_REFUSE_JOIN_GROUP = 3;

    private String fromUid;
    private String toUid;
    private int action;

    Notification(String fromUid, String toUid, int action) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.action = action;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
