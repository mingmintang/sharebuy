package com.mingmin.sharebuy.notification;

public class GroupNotification extends Notification {
    private String groupId;
    private String myName;

    public GroupNotification(String fromUid, String toUid, int action, String groupId, String myName) {
        super(fromUid, toUid, action);
        this.groupId = groupId;
        this.myName = myName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }
}
