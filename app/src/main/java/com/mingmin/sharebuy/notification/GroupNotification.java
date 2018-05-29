package com.mingmin.sharebuy.notification;

public class GroupNotification extends Notification {
    private String groupId;

    public GroupNotification(String fromUid, String toUid, int action, String groupId) {
        super(fromUid, toUid, action);
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
