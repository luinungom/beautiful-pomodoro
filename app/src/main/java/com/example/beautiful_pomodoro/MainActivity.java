package com.example.beautiful_pomodoro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private byte seconds;
    private byte minutes;
    private TextView timerText;
    private TimerTask timerTask;
    private Timer timer;
    private boolean running;
    private boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign visual elements to objects
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        ImageButton startButton = findViewById(R.id.startButton);
        ImageButton pauseButton = findViewById(R.id.pauseButton);
        Button stopButton = findViewById(R.id.stopButton);
        timerText = findViewById(R.id.timerText);


        // Load background gradient
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        seconds = 0;
        minutes = 1;

        // startButton listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
            }
        });

        // pauseButton listener
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTimer();
            }
        });

        // stopButton listener
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
            }
        });
    }

    /**
     * Starts the timer logic.
     */
    private void startTimer() {
        // Generate the timer task in a different thread
        if ((!running || paused) && minutes + seconds != 0) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    seconds--;
                    if (seconds <= -1) {
                        seconds = 59;
                        minutes--;
                    }
                    // Ends timer if time ends.
                    if (minutes == 0 && seconds == 0) {
                        timer.cancel();
                        running = false;
                    }
                    // We need to update the timer in the same thread that handles the UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateVisualTime(minutes, seconds);
                        }
                    });
                }
            };
            timer = new Timer();
            timer.scheduleAtFixedRate(timerTask, 1, 1000);
            running = true;
            paused = false;
        }
    }

    /**
     * Pauses the timer.
     */
    private void pauseTimer() {
        if (running) {
            timer.cancel();
            paused = true;
        }
    }

    /**
     * Stops the timer.
     */
    private void stopTimer() {
        if (minutes + seconds != 0) {
            seconds = 0;
            minutes = 0;
            timerTask.cancel();
            running = false;
            paused = false;
            // We need to update the timer in the same thread that handles the UI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateVisualTime(minutes, seconds);
                }
            });
        }
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
        if (timerTask != null) {
            timer.purge();
            timerTask.cancel();

        }
    }

}