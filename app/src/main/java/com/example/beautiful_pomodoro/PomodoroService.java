package com.example.beautiful_pomodoro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class PomodoroService extends Service {

    private PowerManager.WakeLock wakeLock;
    private Handler handler;
    private Runnable timerRunnable;
    private int seconds = 0;
    private int minutes;
    private byte insertedMinutes;
    private static boolean running;
    private static boolean paused = false;
    public static final String ACTION_START_TIMER = "com.example.beautiful_pomodoro.ACTION_START_TIMER";
    public static final String ACTION_PAUSE_TIMER = "com.example.beautiful_pomodoro.ACTION_PAUSE_TIMER";
    public static final String ACTION_STOP_TIMER = "com.example.beautiful_pomodoro.ACTION_STOP_TIMER";
    public static final String ACTION_INCREASE_TIME = "com.example.beautiful_pomodoro.ACTION_INCREASE_TIME";
    public static final String ACTION_DECREASE_TIME = "com.example.beautiful_pomodoro.ACTION_DECREASE_TIME";
    public static final String ACTION_STOP_ALARM = "com.example.beautiful_pomodoro.ACTION_STOP_ALARM";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "PomodoroChannel";
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private int currentVolume;
    private Handler volumeHandler;
    private static final int VOLUME_INCREMENT_INTERVAL = 500; // ms

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Initializes the service by setting up the vibrator and creating the notification channel.
     * This method is called when the service is first created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        insertedMinutes = TimerState.getInstance().initialMinutes;
        minutes = insertedMinutes;
        createNotificationChannel();
    }

    /**
     * Creates the notification channel for the service.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Beautiful Pomodoro",
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
                .setContentTitle("Stay focused")
                .setContentText("Timer is running")
                .setSmallIcon(R.drawable.pomodoro)
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
                    case ACTION_STOP_ALARM:
                        stopAlarm();
                        Log.d("AlarmDebug", "Intento de detener alarma recibido");
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
            // Create Wakelock
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "BeautifulPomodoro::TimerWakeLockTag"
            );
            // WakeLock maximum limit of 1 hour
            wakeLock.acquire(3600000);
            handler = new Handler(Looper.getMainLooper());
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    seconds--;
                    if (seconds < 0) {
                        seconds = 59;
                        minutes--;
                    }
                    startForeground(NOTIFICATION_ID, createNotification());
                    sendTimeUpdateBroadcast();
                    // Ends timer if time ends.
                    if (minutes == 0 && seconds == 0) {
                        playCustomAlarm();
                        stopTimer();
                        running = false;
                        stopSelf();
                    } else {
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.post(timerRunnable);
            running = true;
            paused = false;
        }
    }

    /**
     * Pauses the timer.
     */
    protected void pauseTimer() {
        if (running) {
            // Stop handler
            if (handler != null && timerRunnable != null) {
                handler.removeCallbacks(timerRunnable);
            }
            // Release wakelock
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
            paused = true;
            sendTimeUpdateBroadcast();
        }
    }

    /**
     * Stops the timer.
     */
    protected void stopTimer() {
        if ((minutes + seconds != 0) && paused || running) {
            seconds = 0;
            minutes = insertedMinutes;
            TimerState.getInstance().initialMinutes = insertedMinutes;
            // Stop Handler
            if (handler != null && timerRunnable != null) {
                handler.removeCallbacks(timerRunnable);
            }
            // Release WakeLock
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
            running = false;
            paused = false;
            sendTimeUpdateBroadcast();
            stopSelf();
        }
    }

    /**
     * Adds 1 minute to the timer.
     */
    protected void increaseTime() {
        if (!running && minutes <= 59) {
            TimerState.getInstance().initialMinutes++;
            insertedMinutes = TimerState.getInstance().initialMinutes;
            minutes = insertedMinutes;
            sendTimeUpdateBroadcast();
        }
    }

    /**
     * Decreases timer by 1 minute.
     */
    protected void decreaseTime() {
        if (!running && insertedMinutes > 1) {
            TimerState.getInstance().initialMinutes--;
            insertedMinutes = TimerState.getInstance().initialMinutes;
            minutes = insertedMinutes;
            sendTimeUpdateBroadcast();
        }
    }

    /**
     * Kills the timerTask when the application is stopped for avoiding memory leaks.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

    /**
     * Method for updating visual timer.
     */
    private void sendTimeUpdateBroadcast() {
        Intent intent = new Intent("UPDATE_TIME");
        intent.putExtra("minutes", minutes);
        intent.putExtra("seconds", seconds);
        sendBroadcast(intent);
    }

    private void playCustomAlarm() {
        AlarmManager.getInstance(this).playAlarm();
    }

    private void stopAlarm() {
        AlarmManager.getInstance(this).stopAlarm();
        stopSelf();
    }

}
