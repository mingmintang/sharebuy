package com.mingmin.sharebuy;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mingmin.sharebuy.notification.GroupNotification;

public class SharebuyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        int action = Integer.parseInt(remoteMessage.getData().get("action"));
        String groupId = remoteMessage.getData().get("groupId");

        Log.d("wwwww", "onMessageReceived: " + groupId + "/" + action);
    }
}
