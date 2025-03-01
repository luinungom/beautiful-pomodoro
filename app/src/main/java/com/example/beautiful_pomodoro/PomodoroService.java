package com.example.beautiful_pomodoro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class PomodoroService extends Service {

    private int seconds = 0;
    private int minutes = 1;
    private byte insertedMinutes = 1;
    private static TimerTask timerTask;
    private static Timer timer;
    private static boolean running;
    private static boolean paused = false;
    private Vibrator vibrator;
    public static final String ACTION_START_TIMER = "com.example.beautiful_pomodoro.ACTION_START_TIMER";
    public static final String ACTION_PAUSE_TIMER = "com.example.beautiful_pomodoro.ACTION_PAUSE_TIMER";
    public static final String ACTION_STOP_TIMER = "com.example.beautiful_pomodoro.ACTION_STOP_TIMER";
    public static final String ACTION_INCREASE_TIME = "com.example.beautiful_pomodoro.ACTION_INCREASE_TIME";
    public static final String ACTION_DECREASE_TIME = "com.example.beautiful_pomodoro.ACTION_DECREASE_TIME";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "PomodoroChannel";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        createNotificationChannel();
        //startForeground(NOTIFICATION_ID, createNotification());

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pomodoro Timer",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Pomodoro Timer")
                .setContentText("Pomodoro timer is running")
                .setSmallIcon(R.drawable.animated_pomodoro)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();
    }

    /**
     * Starts the timer logic.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_START_TIMER:
                        startTimer();
                        break;
                    case ACTION_PAUSE_TIMER:
                        pauseTimer();
                        break;
                    case ACTION_STOP_TIMER:
                        stopTimer();
                        break;
                    case ACTION_INCREASE_TIME:
                        increaseTime();
                        break;
                    case ACTION_DECREASE_TIME:
                        decreaseTime();
                        break;
                    default:
                        Log.w("PomodoroService", "Unknown action: " + action);
                        break;
                }
            }
        }
        return START_STICKY;
    }

    /**
     * Starts the timer.
     */
    protected void startTimer() {
        if ((!running || paused)) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    seconds--;
                    if (seconds < 0) {
                        seconds = 59;
                        minutes--;
                    }
                    startForeground(NOTIFICATION_ID, createNotification());
                    Intent updateIntent = new Intent("UPDATE_TIME");
                    updateIntent.putExtra("minutes", minutes);
                    updateIntent.putExtra("seconds", seconds);
                    PomodoroService.this.sendBroadcast(updateIntent);
                    // Ends timer if time ends.
                    if (minutes == 0 && seconds == 0) {
                        timer.cancel();
                        running = false;
                        stopSelf();
                        if (vibrator != null) {
                            vibrator.vibrate(1000);
                            vibrator = null;
                        }
                    }
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
    protected void pauseTimer() {
        if (running) {
            timer.cancel();
            paused = true;
            Intent updateIntent = new Intent("UPDATE_TIME");
            updateIntent.putExtra("minutes", minutes);
            updateIntent.putExtra("seconds", seconds);
            sendBroadcast(updateIntent);
        }
    }

    /**
     * Stops the timer.
     */
    protected void stopTimer() {
        if ((minutes + seconds != 0) && paused || running) {
            seconds = 0;
            minutes = insertedMinutes;
            running = false;
            paused = false;
            Intent intent = new Intent("UPDATE_TIME");
            intent.putExtra("minutes", minutes);
            intent.putExtra("seconds", seconds);
            sendBroadcast(intent);
            timerTask.cancel();
            stopSelf();
        }
    }

    /**
     * Adds 1 minute to the timer.
     */
    protected void increaseTime() {
        if (!running) {
            insertedMinutes++;
            minutes = insertedMinutes;
            Intent intent = new Intent("UPDATE_TIME");
            intent.putExtra("minutes", minutes);
            intent.putExtra("seconds", seconds);
            sendBroadcast(intent);
        }
    }

    /**
     * Decreases timer by 1 minute.
     */
    protected void decreaseTime() {
        if (!running && insertedMinutes > 1) {
            insertedMinutes--;
            minutes = insertedMinutes;
            Intent intent = new Intent("UPDATE_TIME");
            intent.putExtra("minutes", minutes);
            intent.putExtra("seconds", seconds);
            sendBroadcast(intent);
        }
    }

    /**
     * Kills the timerTask when the application is stopped for avoiding memory leaks.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timerTask != null) {
            timer.purge();
            timerTask.cancel();
            stopForeground(true);
        }
    }

}
