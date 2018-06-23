package com.mingmin.sharebuy.cloud;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mingmin.sharebuy.Group;
import com.mingmin.sharebuy.User;
import com.mingmin.sharebuy.notification.GroupNotification;
import com.mingmin.sharebuy.notification.Notification;

import java.util.HashMap;
import java.util.Map;

public class CloudActions {
    public static Task<Void> createNewGroup(String groupName, User user) {
        String groupId = Fdb.getGroupsRef()
                .push()
                .getKey();
        Group group = new Group(groupId, groupName, user.getUid(), user.getNickname());
        Task<Void> addGroup = Fdb.getGroupRef(group.getId()).setValue(group);
        Map<String, Object> member = new HashMap<>();
        member.put("nickname", group.getFounderNickname());
        member.put("isJoined", true);
        Task<Void> addMember = Fdb.getGroupMemberRef(group.getId(), group.getFounderUid()).setValue(member);
        Task<Void> addGroupInUser = Fdb.getUserGroupRef(user.getUid(), group.getId()).setValue(true);

        return Tasks.whenAll(addGroup, addMember, addGroupInUser);
    }

    public static Task<Void> exitGroup(String groupId, String uid) {
        Task<Void> deleteMember = Fdb.getGroupMemberRef(groupId, uid).removeValue();
        Task<Void> deleteGroupInUser = Fdb.getUserGroupRef(groupId, uid).removeValue();

        return Tasks.whenAll(deleteMember, deleteGroupInUser);
    }

    public static Task<Void> requestJoinGroup(Group group, String uid) {
        GroupNotification notification = new GroupNotification(
                uid,
                group.getFounderUid(),
                Notification.ACTION_REQUEST_JOIN_GROUP,
                group.getId());

        return Fdb.getRequestJoinGroupRef(group.getId())
                .push()
                .setValue(notification);
    }
}
