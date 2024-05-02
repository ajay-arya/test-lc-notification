package com.example.forgroundservicetest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;


import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;

public class SocketIOForegroundService extends Service {

    private static final String TAG = "SocketIOService";
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "TEST";


    private Socket socket;
    private NotificationManager notificationManager;
    private NotificationCompat.InboxStyle inboxStyle;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startForeground();
        return START_STICKY;
    }

    private void startForeground() {
        Log.d(">>>", "startForeground");
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("LedgerChat Testing")
                .setContentText("Listening for socket messages")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private NotificationManager createNotificationChannel() {
        NotificationManager manager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        return manager;
    }

    private void updateNotification(String message) {
        if (inboxStyle == null) {
            inboxStyle = new NotificationCompat.InboxStyle();
        }

        inboxStyle.addLine(message);

        if (notificationManager == null) {
            notificationManager = createNotificationChannel();
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("LedgerChat")
                .setSmallIcon(R.drawable.ic_notification)
//                .setStyle(inboxStyle)
//                .build();

                .setContentText(message)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(">>>", "OnCreate");
        super.onCreate();

        try {
            socket = IO.socket("http://10.0.2.2:3000");
//            socket = IO.socket("http://192.168.20.13:3000");
            socket.connect();
            socket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String message = (String) args[0];
                    Log.d(">>>", "args" + args);
                    Log.d(">>>", "message: " + message);
                    updateNotification(message);
                }
            });

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onDestroy() {
        Log.d(">>>", "OnDestroy");
        super.onDestroy();
        if (socket != null && socket.connected()) {
            socket.disconnect();
            socket.off("message");
        }
    }
}
