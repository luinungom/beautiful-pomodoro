package com.example.beautiful_pomodoro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver receiver;
    private TextView timerText;
    PomodoroService pomodoroService;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pomodoroService = new PomodoroService();
        serviceIntent = new Intent(this, PomodoroService.class);
        startService(serviceIntent);
        setContentView(R.layout.activity_main);

        // Assign visual elements to objects
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        ImageButton startButton = findViewById(R.id.startButton);
        ImageButton pauseButton = findViewById(R.id.pauseButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button increaseButton = findViewById(R.id.increaseButton);
        Button decreaseButton = findViewById(R.id.decreaseButton);
        timerText = findViewById(R.id.timerText);

        // Load background gradient
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        // startButton listener
        startButton.setOnClickListener(v -> sendActionToService(PomodoroService.ACTION_START_TIMER));

        // pauseButton listener
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pomodoroService.pauseTimer();
            }
        });

        // stopButton listener
        stopButton.setOnClickListener(v -> sendActionToService(PomodoroService.ACTION_STOP_TIMER));

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pomodoroService.increaseTime();
                //insertedTime.setText(String.valueOf(insertedMinutes));
            }
        });

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pomodoroService.decreaseTime();
                //insertedTime.setText(String.valueOf(insertedMinutes));
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Updates the clock
                int minutes = intent.getIntExtra("minutes", 0);
                int seconds = intent.getIntExtra("seconds", 0);
                updateVisualTime(minutes, seconds);
            }
        };

        // Registers the BroadcastReceiver
        IntentFilter filter = new IntentFilter("UPDATE_TIME");
        registerReceiver(receiver, filter);
    }

    private void sendActionToService(String action) {
        Intent serviceIntent = new Intent(this, PomodoroService.class);
        serviceIntent.setAction(action);
        startService(serviceIntent);
    }

    private void startPomodoroTimer() {
        Intent intent = new Intent(this, PomodoroService.class);
        intent.setAction(PomodoroService.ACTION_START_TIMER);
        startService(intent);
    }

    private void stopPomodoroTimer() {
        Intent intent = new Intent(this, PomodoroService.class);
        intent.setAction(PomodoroService.ACTION_STOP_TIMER);
        startService(intent);
    }

    /**
     * Manages the visual representation of the elapsed time.
     *
     * @param minutes Numeric value for minutes.
     * @param seconds Numeric value for seconds.
     */
    private void updateVisualTime(int minutes, int seconds) {
        // Represents the time in the UI.
        String time = (minutes <= 9 ? "0" : "") + minutes + ":" + (seconds <= 9 ? "0" : "") + seconds;
        timerText.setText(time);
    }

    /**
     * Kills the timerTask when the application is stopped for avoiding memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}