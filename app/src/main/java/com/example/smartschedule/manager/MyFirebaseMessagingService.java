package com.example.smartschedule.manager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM messages here
        if (remoteMessage.getData().size() > 0) {
            // Handle data payload
            String data = remoteMessage.getData().get("key"); // Example key-value data
        }

        if (remoteMessage.getNotification() != null) {
            // Handle notification payload
            String notificationBody = remoteMessage.getNotification().getBody();
            // Show notification or handle accordingly
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Handle the updated token (e.g., send it to your server)
    }
}
