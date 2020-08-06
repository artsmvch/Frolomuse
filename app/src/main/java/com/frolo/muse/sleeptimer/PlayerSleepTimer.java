package com.frolo.muse.sleeptimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;


/**
 * Static helper for player sleep timer functionality.
 */
public final class PlayerSleepTimer {

    public final static String ACTION_ALARM_TRIGGERED =
            "com.frolo.muse.sleeptimer.ACTION_ALARM_TRIGGERED";

    private final static int RC_PAUSE_PLAYER = 315;

    private PlayerSleepTimer() { }

    /**
     * Checks if there is a previously set alarm.
     * @param context app context
     * @return true if there is an alarm set up, false - otherwise.
     */
    public static boolean isTimerSetUp(@NonNull Context context) {
        Intent intent = new Intent(ACTION_ALARM_TRIGGERED);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                RC_PAUSE_PLAYER,
                intent,
                PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    /**
     * Resets the previously set alarm.
     * This method has no effect if there is no alarm set up.
     * @param context app context
     * @return true if an alarm was reset successfully, false - otherwise.
     */
    public static boolean resetCurrentSleepTimer(@NonNull Context context) {
        Intent intent = new Intent(ACTION_ALARM_TRIGGERED);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                RC_PAUSE_PLAYER,
                intent,
                PendingIntent.FLAG_NO_CREATE);
        if (pi != null) {
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (manager != null) {
                pi.cancel();
                manager.cancel(pi);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Sets an alarm at some point to pause the player.
     * Duration of sleeping is calculated by {@code hours}, {@code minutes} and {@code seconds}.
     * When the alarm clocks the player will be paused.
     * The alarm can be cancelled with {@link #resetCurrentSleepTimer}.
     * @param context app context
     * @param hours hours of sleeping
     * @param minutes minutes of sleeping
     * @param seconds seconds of sleeping
     * @return true if the alarm is set successfully, false - otherwise.
     */
    public static boolean setAlarm(@NonNull Context context, int hours, int minutes, int seconds) {
        if (hours == 0 && minutes == 0 && seconds == 0)
            return false;  // no sense to setup an alarm

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            long time = System.currentTimeMillis();
            time += hours * 60 * 60 * 1000;
            time += minutes * 60 * 1000;
            time += seconds * 1000;

            Intent intent = new Intent(ACTION_ALARM_TRIGGERED);
            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    RC_PAUSE_PLAYER,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            manager.setExact(AlarmManager.RTC_WAKEUP, time, pi);
            return true;
        }
        return false;
    }

    @NonNull
    public static BroadcastReceiver createBroadcastReceiver(@NonNull final Runnable onAlarmTriggered) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    return;
                }

                final String action = intent.getAction();
                if (action != null && action.equals(PlayerSleepTimer.ACTION_ALARM_TRIGGERED)) {
                    // Need to reset the current sleep timer because its pending intent is still retained,
                    // therefore the app settings may think that an alarm is still set.
                    PlayerSleepTimer.resetCurrentSleepTimer(context);
                    // Running the callback
                    onAlarmTriggered.run();
                }
            }
        };
    }

    @NonNull
    public static IntentFilter createIntentFilter() {
        return new IntentFilter(PlayerSleepTimer.ACTION_ALARM_TRIGGERED);
    }

}
