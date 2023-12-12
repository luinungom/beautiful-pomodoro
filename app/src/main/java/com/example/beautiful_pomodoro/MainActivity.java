package com.example.beautiful_pomodoro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private int seconds = 0;
    private int minutes = 0;
    private TextView timerText;
    private TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign visual elements to objects
        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        ImageButton startButton = findViewById(R.id.start_button);
        timerText = findViewById(R.id.timerText);


        // Load background gradient
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        // Set startButton listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
            }
        });

    }

    /**
     * Starts the timer logic.
     */
    public void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                seconds ++;
                if (seconds == 60) {
                    seconds = 0;
                    minutes++;
                }
                // Get need to update the timer in the same thread that handles the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateVisualTime(minutes, seconds);
                    }
                });
            }
        };
        // Generate the timer in a different thread
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    /**
     * Manages the visual representation of the elapsed time.
     * @param minutes Numeric value for minutes.
     * @param seconds Numeric value for seconds.
     */
    private void updateVisualTime(int minutes, int seconds) {
        String time = (minutes <= 9 ? "0" : "") + minutes + ":" + (seconds<=9 ? "0" : "") + seconds;
        timerText.setText(time);
    }

    /**
     * Kills the timerTask when the application is stopped for avoiding memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

}