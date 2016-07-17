package ru.investflow.android.chat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;

import ru.investflow.android.chat.util.AppUtils;

//https://firebase.google.com/docs/cloud-messaging/concept-options
public class NotificationService extends FirebaseMessagingService {

    // Common ID for all chat notification
    private static final int NOTIFICATION_ID = 8;

    private static final Logger log = AppUtils.getLogger(App.class);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        log.debug("onMessageReceived: " + remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        String user = data.get("user");
        String message = data.get("message");
        if (message != null && user != null && !user.equals(AppSettings.getLogin()) && !AppUtils.isForeground()) {
            sendNotification(user, message);
        }
    }

    private void sendNotification(@NotNull String user, @NotNull String messageBody) {
        clearAllNotifications(this);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_notification_large))
                .setContentTitle("Чат Investflow")
                .setContentText(user + ": " + messageBody)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        AppUtils.addNotificationSound(builder);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void clearAllNotifications(@NotNull Context ctx) {
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}