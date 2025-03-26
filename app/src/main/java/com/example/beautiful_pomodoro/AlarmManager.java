package com.example.beautiful_pomodoro;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

/**
 * Singleton class, controls the alarm status.
 */
public class AlarmManager {
    private static AlarmManager instance;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private final Context context;
    private boolean isPlaying;

    private AlarmManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized AlarmManager getInstance(Context context) {
        if (instance == null) {
            instance = new AlarmManager(context);
        }
        return instance;
    }

    public void playAlarm() {
        if (isPlaying) return;

        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            mediaPlayer = MediaPlayer.create(context, alarmSound);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

            vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(3000);

            isPlaying = true;
        } catch (Exception e) {
            Log.e("AlarmError", "Alarm error", e);
            stopAlarm();
        }
    }

    public void stopAlarm() {
        if (!isPlaying) return;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
        }

        isPlaying = false;
    }

    public boolean isAlarmPlaying() {
        return isPlaying;
    }
}