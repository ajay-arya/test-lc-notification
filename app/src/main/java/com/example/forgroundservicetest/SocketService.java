package com.example.forgroundservicetest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class SocketService extends Service {
    private final IBinder binder = new LocalBinder();
    private Socket socket;

    public class LocalBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(">>>", "onBind");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(">>>", "onStartCommand");
        startForegroundService();
        initializeSocket();
        return START_STICKY;
    }

    private void startForegroundService() {
        Log.d(">>>", "startForegroundService");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "socket_channel")
                .setContentTitle("LedgerChat")
                .setContentText("Listening to Notifications...")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("socket_channel", "Socket Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        Notification notification = builder.build();
        startForeground(1, notification);
    }

    private void initializeSocket() {
        Log.d(">>>", "initializeSocket");
        try {
            socket = IO.socket("http://192.168.20.13:3000");
            // Set up listeners here
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void startListeningTo(String eventName) {
        Log.d(">>>", "startListeningTo");
        if (socket != null) {
            Log.d(">>>", "socket is not null");
            socket.on(eventName,  new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String message = (String) args[0];
                    Log.d(">>>", "args" + args);
                    Log.d(">>>", "message: " + message);
                    showNotification(message);
                }
            });
        } else {
            Log.d(">>>", "socket is null");
        }
    }

    private void showNotification(String message) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "message_channel")
                .setContentTitle("LedgerChat Enterprise")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("message_channel", "Message Channel", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(2, builder.build());
        }
    }


    @Override
    public void onDestroy() {
        Log.d(">>>", "onDestroy socket");
        super.onDestroy();
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

}

