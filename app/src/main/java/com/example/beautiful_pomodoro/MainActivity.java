package com.example.beautiful_pomodoro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private byte seconds = 0;
    private byte minutes = 1;
    private byte insertedMinutes = 1;
    private TextView timerText;
    private TimerTask timerTask;
    private Timer timer;
    private boolean running;
    private boolean paused = false;
    private TextView insertedTime;
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Assign visual elements to objects
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        ImageButton startButton = findViewById(R.id.startButton);
        ImageButton pauseButton = findViewById(R.id.pauseButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button increaseButton = findViewById(R.id.increaseButton);
        Button decreaseButton = findViewById(R.id.decreaseButton);
        timerText = findViewById(R.id.timerText);
        //insertedTime = findViewById(R.id.insertedTime);

        // Load background gradient
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

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

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseTime();
                //insertedTime.setText(String.valueOf(insertedMinutes));
            }
        });

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseTime();
                //insertedTime.setText(String.valueOf(insertedMinutes));
            }
        });
    }

    /**
     * Starts the timer logic.
     */
    private void startTimer() {
        final VibrationEffect vibrationEffect1;
        // Generate the timer task in a different thread
        if ((!running || paused)) {
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
                        if (vibrator != null) {
                            vibrator.vibrate(1000);
                            vibrator = null;
                        }

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
            timer.schedule(timerTask, 1, 1000);
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
        if ((minutes + seconds != 0) && paused || running) {
            seconds = 0;
            //minutes = 0;
            minutes = insertedMinutes;
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

    private void increaseTime() {
        if (!running) {
            insertedMinutes++;
            minutes = insertedMinutes;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateVisualTime(minutes, seconds);
                }
            });
        }
    }

    private void decreaseTime() {
        if (!running && insertedMinutes > 1) {
            insertedMinutes--;
            minutes = insertedMinutes;
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