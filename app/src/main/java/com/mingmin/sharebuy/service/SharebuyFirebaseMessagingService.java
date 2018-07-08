package com.mingmin.sharebuy.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mingmin.sharebuy.Group;
import com.mingmin.sharebuy.MainActivity;
import com.mingmin.sharebuy.R;
import com.mingmin.sharebuy.cloud.Clouds;

import static com.mingmin.sharebuy.notification.Notification.ACTION_REQUEST_JOIN_GROUP;

public class SharebuyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_NAME_REQUEST_JOIN_GROUP = "請求加入群組";
    private final String TAG = getClass().getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        int action = Integer.parseInt(remoteMessage.getData().get("action"));
        String groupId = remoteMessage.getData().get("groupId");

        switch (action) {
            case ACTION_REQUEST_JOIN_GROUP:
                requestJoinGroupNotification(groupId);
                break;
        }
    }

    private void requestJoinGroupNotification(String groupId) {
        Clouds.getInstance().getGroupByGroupId(groupId)
                .addOnSuccessListener(new OnSuccessListener<Group>() {
                    @Override
                    public void onSuccess(Group group) {
                        Intent intent = new Intent(SharebuyFirebaseMessagingService.this, MainActivity.class);
                        intent.putExtra("goToGroupManage", true);
                        intent.putExtra("group", group);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(SharebuyFirebaseMessagingService.this,
                                ACTION_REQUEST_JOIN_GROUP,
                                intent,
                                PendingIntent.FLAG_ONE_SHOT);

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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }
}
