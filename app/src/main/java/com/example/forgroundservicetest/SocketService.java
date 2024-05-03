package com.example.forgroundservicetest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
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
    private Timer timer;

    public class LocalBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeSocket();
        startSocketReconnectionTask();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void initializeSocket() {
        try {
            socket = IO.socket("http://192.168.20.13:3000");
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void startSocketReconnectionTask() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!socket.connected()) {
                    Log.d("SocketService", "Socket not connected, attempting reconnection...");
                    socket.connect();
                }
            }
        }, 0, 5000); // Check every 5 seconds
    }

    public void startListeningTo(String eventName) {
        // Implement your logic to start listening to a new event name
        if (socket != null) {
            Log.d(">>>", "socket is not null");
            socket.on(eventName,  new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String message = (String) args[0];
                    Log.d(">>>", "args" + args);
                    Log.d(">>>", "message: " + message);
                }
            });

        } else {
            Log.d(">>>", "socket is null");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
        if (timer != null) {
            timer.cancel();
        }
    }
}

