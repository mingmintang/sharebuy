package com.mingmin.sharebuy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.mingmin.sharebuy.notification.Notification.ACTION_REQUEST_JOIN_GROUP;

public class SharebuyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_NAME_REQUEST_JOIN_GROUP = "請求加入群組";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        int action = Integer.parseInt(remoteMessage.getData().get("action"));
        String groupId = remoteMessage.getData().get("groupId");

        Log.d("wwwww", "onMessageReceived: " + groupId + "/" + action);

        switch (action) {
            case ACTION_REQUEST_JOIN_GROUP:
                requestJoinGroupNotification(action, groupId);
                break;
        }
    }

    private void requestJoinGroupNotification(int action, String groupId) {
        FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Group group = dataSnapshot.getValue(Group.class);

                        Intent intent = new Intent(SharebuyFirebaseMessagingService.this, GroupManageActivity.class);
                        intent.putExtra("group", group);
                        intent.putExtra("selectedItemId", R.id.group_manage_nav_joining);
                        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(SharebuyFirebaseMessagingService.this);
                        taskStackBuilder.addParentStack(GroupManageActivity.class);
                        taskStackBuilder.addNextIntent(intent);
                        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(
                                ACTION_REQUEST_JOIN_GROUP,
                                PendingIntent.FLAG_ONE_SHOT);
//                        PendingIntent pendingIntent = PendingIntent.getActivity(SharebuyFirebaseMessagingService.this,
//                                ACTION_REQUEST_JOIN_GROUP,
//                                intent,
//                                PendingIntent.FLAG_ONE_SHOT);

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(SharebuyFirebaseMessagingService.this,
                                String.valueOf(ACTION_REQUEST_JOIN_GROUP))
                                .setSmallIcon(R.drawable.ic_sharebuy)
                                .setContentTitle(getResources().getString(R.string.app_name))
                                .setContentText("有新成員等待加入 " + group.getName() + " 群組")
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(String.valueOf(ACTION_REQUEST_JOIN_GROUP),
                                    CHANNEL_NAME_REQUEST_JOIN_GROUP,
                                    NotificationManager.IMPORTANCE_DEFAULT);
                            notificationManager.createNotificationChannel(channel);
                        }

                        notificationManager.notify(ACTION_REQUEST_JOIN_GROUP, builder.build());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


}
