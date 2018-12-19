package com.mingmin.sharebuy.cloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserEndOrderDoc {
    private @ServerTimestamp Date createTime = new Date(0);
    private @ServerTimestamp Date updateTime = new Date(0);
    private String groupId;

    public UserEndOrderDoc() {
        setCreateTime();
        setUpdateTime();
    }

    public UserEndOrderDoc(String groupId, Date createTime) {
        this.groupId = groupId;
        this.createTime = createTime;
        setUpdateTime();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime() {
        this.createTime = null;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime() {
        this.updateTime = null;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
