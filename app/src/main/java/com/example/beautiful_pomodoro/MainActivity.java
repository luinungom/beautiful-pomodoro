package com.example.beautiful_pomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver receiver;
    private TextView timerText;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //pomodoroService = new PomodoroService();
        serviceIntent = new Intent(this, PomodoroService.class);
        startService(serviceIntent);
        setContentView(R.layout.activity_main);

        // Assign visual elements to objects
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        ImageButton startButton = findViewById(R.id.startButton);
        ImageButton pauseButton = findViewById(R.id.pauseButton);
        ImageButton stopButton = findViewById(R.id.stopButton);
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
        pauseButton.setOnClickListener(v -> sendActionToService(PomodoroService.ACTION_PAUSE_TIMER));

        // stopButton listener
        stopButton.setOnClickListener(v -> sendActionToService(PomodoroService.ACTION_STOP_TIMER));

        // increaseButton listener
        increaseButton.setOnClickListener(v -> sendActionToService(PomodoroService.ACTION_INCREASE_TIME));

        // decreaseButton listener
        decreaseButton.setOnClickListener(v -> sendActionToService(PomodoroService.ACTION_DECREASE_TIME));
        setupGlobalTouchListener();

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
     * Setup a global listener for turning off the alarm. Touching the screen will stop the alarm.
     */
    private void setupGlobalTouchListener() {
        getWindow().getDecorView().setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                sendActionToService(PomodoroService.ACTION_STOP_ALARM);
                return true; // Consume el evento
            }
            return false;
        });
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