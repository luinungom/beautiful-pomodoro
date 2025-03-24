package com.example.beautiful_pomodoro;

/**
 * Singleton class for keeping user inserted time after stopping the timer.
 */
public class TimerState {
    private static TimerState instance;
    public byte initialMinutes = 1;

    public static TimerState getInstance() {
        if (instance == null) {
            instance = new TimerState();
            instance.initialMinutes = 1;
        }
        return instance;
    }
}
