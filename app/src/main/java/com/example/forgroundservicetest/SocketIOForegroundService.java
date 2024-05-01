package com.example.forgroundservicetest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;


import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.io.IOException;
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

        // Create notification to run service in foreground
        Intent notificationIntent = new Intent(this, MainActivity.class);
        int pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE; // Use FLAG_IMMUTABLE for Android S and above
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlags);


        Notification notification = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("Socket.IO Service")
                .setContentText("Listening for socket messages...")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(">>>", "OnCreate");
        super.onCreate();

        // Connect to Socket.IO server
        try {
//            socket = IO.socket("http://10.0.2.2:3000");
            socket = IO.socket("http://192.168.20.13:3000");
            socket.connect();
            Log.d(">>>", "OnCreate--3");
            socket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String message = (String) args[0];
                    Log.d(">>>", "args" + args);
                    Log.d(">>>", "message: " + message);
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
