package com.example.valverde.stopwatch;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class StopwatchService extends Service {
    private static volatile StopwatchService instance = null;
    private boolean stopService = false;
    private SimpleServiceTimer timer;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences preferences = this.getSharedPreferences(
                "com.example.stopwatch", Context.MODE_PRIVATE);
        String timerKey = "com.example.stopwatch.rememberedTime";
        String stateKey = "com.example.stopwatch.rememberedState";
        String closingKey = "com.example.stopwatch.closingMoment";

        final long rememberedTime = Long.parseLong(preferences.getString(timerKey, ""));
        final long closingTime = Long.parseLong(preferences.getString(closingKey, ""));
        String timerState = preferences.getString(stateKey, "");

        if (timerState.equals("started")) {
            long startTime = rememberedTime + (System.currentTimeMillis() - closingTime);
            timer = new SimpleServiceTimer(startTime);
            timer.start();
        }
        else if (timerState.equals("paused")) {
            String timeInFormat = Timer.getActualTimeInFormatWithoutMillis(rememberedTime);
            showNotification(timeInFormat);
        }
        return START_STICKY;
    }


    private void showNotification(String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.stopwatch_icon);
        builder.setContentTitle("Stopwatch");
        builder.setContentText(text);

        Intent intent = new Intent(this, StopwatchActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(StopwatchActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null)
            timer.setRunning(false);
        if (!stopService) {
            Intent broadcastIntent = new Intent("restart_service");
            sendBroadcast(broadcastIntent);
        }
    }

    public void stopService() {
        stopService = true;
        onDestroy();
    }

    public static StopwatchService getInstance() {
        return instance;
    }


    class SimpleServiceTimer extends Thread {
        private boolean running = true;
        private long startTime;
        private final long SLEEP_TIME = 500;

        SimpleServiceTimer(long startTime) {
            this.startTime = startTime;
        }

        @Override
        public void run() {
            long timerStart = System.currentTimeMillis();
            while (running) {
                try {
                    long timePassed = System.currentTimeMillis() - timerStart;
                    long time = startTime + timePassed;
                    String timeInFormat = Timer.getActualTimeInFormatWithoutMillis(time);
                    showNotification(timeInFormat);
                    Thread.sleep(SLEEP_TIME);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setRunning(boolean running) {
            this.running = running;
        }
    }
}
