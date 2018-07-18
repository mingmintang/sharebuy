package com.mingmin.sharebuy.cloud;

import android.support.annotation.Nullable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserOrderDoc {
    private @ServerTimestamp Date updateTime = new Date(0);
    private String myName;
    private String groupId;

    public UserOrderDoc() { }

    public UserOrderDoc(String myName, @Nullable String groupId) {
        this.myName = myName;
        this.groupId = groupId;
        setUpdateTime();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    private void setUpdateTime() {
        updateTime = null;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
