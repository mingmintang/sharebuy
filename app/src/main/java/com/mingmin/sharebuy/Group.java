package com.mingmin.sharebuy;

import com.mingmin.sharebuy.cloud.GroupDoc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Group implements Serializable {
    private GroupDoc groupDoc;
    private String id;
    private String name;
    private String founderUid;
    private String founderNickname;
    private Date createdTime;
    private int searchCode;
    private ArrayList<Member> members;

    public Group(String id, GroupDoc groupDoc) {
        this.id = id;
        setGroupDoc(groupDoc);
        members = new ArrayList<>();
    }

    private void updateValues(GroupDoc groupDoc) {
        setName(groupDoc.getName());
        setFounderUid(groupDoc.getFounderUid());
        setFounderNickname(groupDoc.getFounderNickname());
        setCreatedTime(groupDoc.getCreateTime());
        setSearchCode(groupDoc.getSearchCode());
    }

    public GroupDoc getGroupDoc() {
        return groupDoc;
    }

    public void setGroupDoc(GroupDoc groupDoc) {
        this.groupDoc = groupDoc;
        updateValues(groupDoc);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFounderUid() {
        return founderUid;
    }

    public void setFounderUid(String founderUid) {
        this.founderUid = founderUid;
    }

    public String getFounderNickname() {
        return founderNickname;
    }

    public void setFounderNickname(String founderNickname) {
        this.founderNickname = founderNickname;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public int getSearchCode() {
        return searchCode;
    }

    public void setSearchCode(int searchCode) {
        this.searchCode = searchCode;
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }

    public String searchNicknameByUid(String uid) {
        if (uid.equals(getFounderUid())) {
            return getFounderNickname();
        }
        if (members.size() > 0) {
            for (Member member : members) {
                if (uid.equals(member.getUid())) {
                    return member.getNickname();
                }
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return getName() + " (" + getFounderNickname() + ")";
    }
}
