package com.example.valverde.stopwatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StopwatchActivity extends AppCompatActivity {
    private Timer timer;
    private Handler handler = new Handler();
    @BindView(R.id.timeField) TextView timerField;
    @BindView(R.id.startButton) ImageButton startButton;
    @BindView(R.id.resetButton) ImageButton resetButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopwatch_layout);
        ButterKnife.bind(this);
        stopServiceIfRunning();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timerState = timer.getTimerState();

                if (timerState.equals("init"))
                    startTimer(0);
                else if (timerState.equals("started"))
                    pauseTimer();
                else if (timerState.equals("paused"))
                    unPauseTimer();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null) {
                    timer.setRunning(false);
                    timer.unPause();
                    timer = null;
                    timer = new Timer(handler, timerField, 0);
                    resetPreferences();
                }
                timerField.setText(getString(R.string.start_timer_value));
            }
        });
        getPreferences();
    }

    private void getPreferences() {
        SharedPreferences preferences = this.getSharedPreferences(
                      "com.example.stopwatch", Context.MODE_PRIVATE);
        String timerKey = "com.example.stopwatch.rememberedTime";
        String stateKey = "com.example.stopwatch.rememberedState";
        String closingKey = "com.example.stopwatch.closingMoment";
        String lastTimeInStirng = preferences.getString(timerKey, "");
        long rememberedTime;
        if (!lastTimeInStirng.equalsIgnoreCase("") && !lastTimeInStirng.equalsIgnoreCase("0")) {
            rememberedTime = Long.parseLong(lastTimeInStirng);
            String timerState = preferences.getString(stateKey, "");
            long closingTime = Long.parseLong(preferences.getString(closingKey, ""));
            if (timerState.equals("started")) {
                rememberedTime += System.currentTimeMillis() - closingTime;
                setPauseButtonResource();
            }
            else if (timerState.equals("paused")) {
                String actualTimerText = Timer.getActualTimeInFormat(rememberedTime);
                timerField.setText(actualTimerText);
            }
            timer = new Timer(handler, timerField, rememberedTime);
            timer.startTimer();
            timer.setTimerState(timerState);
        }
        else {
            timer = new Timer(handler, timerField, 0);
        }
    }

    private void resetPreferences() {
        SharedPreferences preferences = this.getSharedPreferences(
                "com.example.stopwatch", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private void startTimer(long rememberedTime) {
        timer = new Timer(handler, timerField, rememberedTime);
        timer.startTimer();
        setPauseButtonResource();
    }

    private void pauseTimer() {
        timer.pause();
        setStartButtonResource();
    }

    private void unPauseTimer() {
        timer.unPause();
        setPauseButtonResource();
    }

    private synchronized void stopServiceIfRunning() {
        StopwatchService service = StopwatchService.getInstance();
        if (service != null)
            service.stopService();
    }

    private void setPauseButtonResource() {
        startButton.setImageResource(R.drawable.pause_button);
    }

    private void setStartButtonResource() {
        startButton.setImageResource(R.drawable.play_button);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences preferences = this.getSharedPreferences(
                "com.example.stopwatch", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        long measuredTime = timer.getMeasuredTime();

        String timerState = timer.getTimerState();
        String measuredTimeInString = Long.toString(measuredTime);
        editor.putString("com.example.stopwatch.rememberedTime", measuredTimeInString);
        editor.putString("com.example.stopwatch.rememberedState", timerState);
        editor.putString("com.example.stopwatch.closingMoment",
                Long.toString(System.currentTimeMillis()));
        editor.apply();

        timer.setRunning(false);
        timer.unPause();
        timer = null;
        if (measuredTime > 0) {
            Intent serviceIntent = new Intent(this, StopwatchService.class);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getPreferences();
        stopServiceIfRunning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServiceIfRunning();
    }
}