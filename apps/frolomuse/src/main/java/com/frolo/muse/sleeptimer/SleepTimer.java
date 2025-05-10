package com.frolo.muse.sleeptimer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * Sleep timer singleton.
 */
public final class SleepTimer {
    private static final int MSG_TRIGGER = 1337;

    private static final Object sInstanceLock = new Object();
    @GuardedBy("sInstanceLock")
    private static SleepTimer sInstance = null;

    public interface TimerTriggerListener {
        void onTimerTriggered();
    }

    public static SleepTimer getInstance() {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new SleepTimer();
            }
            return sInstance;
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_TRIGGER) {
                for (TimerTriggerListener listener : listeners) {
                    listener.onTimerTriggered();
                }
                return true;
            }
            return false;
        }
    });
    private final Set<TimerTriggerListener> listeners =
            Collections.newSetFromMap(new WeakHashMap<>());

    private SleepTimer() {
    }

    /**
     * Checks if there is an active timer set.
     * @return true if there is a timer set up, false - otherwise.
     */
    public boolean isTimerSetUp() {
        return handler.hasMessages(MSG_TRIGGER);
    }

    /**
     * Resets the previously set timer. This method has no effect if there is no timer set up.
     */
    public boolean resetCurrentTimer() {
        boolean wasSet = handler.hasMessages(MSG_TRIGGER);
        if (wasSet) {
            handler.removeMessages(MSG_TRIGGER);
        }
        return wasSet;
    }

    /**
     * Sets a timer to trigger at some point in the future.
     * Delay is calculated by {@code hours}, {@code minutes} and {@code seconds}.
     * The timer can be cancelled with {@link #resetCurrentTimer}.
     * @param hours hours of sleeping
     * @param minutes minutes of sleeping
     * @param seconds seconds of sleeping
     * @return true if the sleep timer is set successfully, false - otherwise.
     */
    public boolean setTimer(int hours, int minutes, int seconds) {
        if (hours == 0 && minutes == 0 && seconds == 0) {
            return false;  // No sense to setup an alarm
        }

        int delay = 0;
        delay += hours * 60 * 60 * 1000;
        delay += minutes * 60 * 1000;
        delay += seconds * 1000;

        handler.removeMessages(MSG_TRIGGER);
        handler.sendEmptyMessageDelayed(MSG_TRIGGER, delay);

        return true;
    }

    public void addListener(@NonNull TimerTriggerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(@NonNull TimerTriggerListener listener) {
        listeners.remove(listener);
    }
}
