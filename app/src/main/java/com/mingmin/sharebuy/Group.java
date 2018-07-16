package com.mingmin.sharebuy;

import com.mingmin.sharebuy.cloud.GroupDoc;
import com.mingmin.sharebuy.cloud.MemberDoc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Group implements Serializable {
    private String id;
    private String name;
    private String managerUid;
    private String managerName;
    private Date createTime;
    private Date joinTime;
    private int searchCode;
    private ArrayList<Member> members;
    private String myName;

    public Group(String id, String name, String managerName) {
        this.id = id;
        this.name = name;
        this.managerName = managerName;
    }

    public Group(String id, String name, String managerUid, String managerName) {
        this.id = id;
        this.name = name;
        this.managerUid = managerUid;
        this.managerName = managerName;
    }

    public Group(String id, String name, String managerUid, String managerName, String myName) {
        this.id = id;
        this.name = name;
        this.managerUid = managerUid;
        this.managerName = managerName;
        this.myName = myName;
    }

    public Group(String id, GroupDoc groupDoc) {
        this.id = id;
        members = new ArrayList<>();
        setupValues(groupDoc);
    }

    private void setupValues(GroupDoc groupDoc) {
        name = groupDoc.getName();
        managerUid = groupDoc.getManagerUid();
        managerName = groupDoc.getManagerName();
        createTime = groupDoc.getCreateTime();
        searchCode = groupDoc.getSearchCode();
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

    public String getManagerUid() {
        return managerUid;
    }

    public void setManagerUid(String managerUid) {
        this.managerUid = managerUid;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getJoinTime() {
        return joinTime;
    }

    public int getSearchCode() {
        return searchCode;
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public String searchUserNameByUid(String uid) {
        if (uid.equals(getManagerUid())) {
            return getManagerName();
        }
        if (members.size() > 0) {
            for (Member member : members) {
                if (uid.equals(member.getUid())) {
                    return member.getName();
                }
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return getName() + " (" + getManagerName() + ")";
    }
}
