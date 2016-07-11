package com.example.valverde.stopwatch;

import android.os.Handler;
import android.widget.TextView;

public class Timer extends Thread {
    private long rememberedTime, pausedTime = 0, elapsedTime = 0;
    private static final Object lock = new Object();
    private static final int HOUR_FACTOR = 3600000;
    private static final int MINUTE_FACTOR = 60000;
    private static final int SECOND_FACTOR = 1000;
    private boolean running = false;
    private String timerState = "init";
    private TextView timerField;
    private Handler handler;

    public Timer(Handler handler, TextView timerField, long rememberedTime) {
        this.handler = handler;
        this.timerField = timerField;
        this.rememberedTime = rememberedTime;
        elapsedTime = rememberedTime;
    }

    @Override
    public void run() {
        final int SLEEP_TIME = 10;
        long startTime = System.currentTimeMillis() - rememberedTime;
        running = true;
        while (running) {
            pausedTime = checkIfIsPaused(pausedTime);
            try {
                final long timeElapsedInMillis = System.currentTimeMillis() - startTime;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        elapsedTime = timeElapsedInMillis - pausedTime;
                        String actualTimeInFormat = getActualTimeInFormat(elapsedTime);
                        if (running)
                            timerField.setText(actualTimeInFormat);
                    }
                });
                Thread.sleep(SLEEP_TIME);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long checkIfIsPaused(long time) {
        synchronized (lock) {
            while (timerState.equals("paused")) {
                try {
                    long pauseStart = System.currentTimeMillis();
                    lock.wait();
                    time += System.currentTimeMillis() - pauseStart;
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return time;
    }

    public static String getActualTimeInFormat(long elapsedTime) {
        String hours = Long.toString(elapsedTime / HOUR_FACTOR);
        elapsedTime %= HOUR_FACTOR;
        String minutes = getMinutes(elapsedTime);
        elapsedTime %= MINUTE_FACTOR;
        String seconds = getSeconds(elapsedTime);
        elapsedTime %= SECOND_FACTOR;
        String millis = getMillis(elapsedTime);
        return hours+":"+minutes+":"+seconds+":"+millis;
    }

    public static String getActualTimeInFormatWithoutMillis(long elapsedTime) {
        String hours = Long.toString(elapsedTime / HOUR_FACTOR);
        elapsedTime %= HOUR_FACTOR;
        String minutes = getMinutes(elapsedTime);
        elapsedTime %= MINUTE_FACTOR;
        String seconds = getSeconds(elapsedTime);
        return hours+":"+minutes+":"+seconds;
    }

    private static String getMinutes(long time) {
        int minutes = (int)time / MINUTE_FACTOR;
        if (minutes < 10) return "0"+minutes;
        else return Integer.toString(minutes);
    }

    private static String getSeconds(long time) {
        int seconds = (int)time / SECOND_FACTOR;
        if (seconds < 10) return "0"+seconds;
        else return Integer.toString(seconds);
    }

    private static String getMillis(long time) {
        int millis = (int)time / 100;
        return Integer.toString(millis);
    }

    public synchronized void pause() {
        timerState = "paused";
    }

    public void startTimer() {
        timerState = "started";
        start();
    }

    public void unPause() {
        timerState = "started";
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public String getTimerState() {
        return timerState;
    }

    public void setTimerState(String timerState) {
        this.timerState = timerState;
    }

    public long getMeasuredTime() {
        return elapsedTime;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}