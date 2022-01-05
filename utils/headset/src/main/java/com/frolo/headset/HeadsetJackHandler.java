package com.frolo.headset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * This handles the change in state of the headset jack, namely:
 * 1) when the headsets were plugged to the jack;
 * 2) when the headsets were unplugged from the jack;
 * 3) when the headsets become weird (unknown state).
 */
public class HeadsetJackHandler extends BroadcastReceiver {

    enum State {
        PLUGGED, UNPLUGGED, WEIRD
    }

    public static IntentFilter createIntentFilter() {
        return new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    }

    @Nullable
    public static State getCurrentState(@NonNull Context context) {
        try {
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager == null) {
                return null;
            }

            // deprecated solution
            // audioManager.isWiredHeadsetOn();

            final AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo deviceInfo : audioDevices){
                if(deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET){
                    return State.PLUGGED;
                }
            }

            return State.UNPLUGGED;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();

        if (Intent.ACTION_HEADSET_PLUG.equals(action)) {

            final int state = intent.getIntExtra("state", -1);

            switch (state) {
                case 0: {
                    onStateChanged(State.UNPLUGGED);
                    break;
                }

                case 1: {
                    onStateChanged(State.PLUGGED);
                    break;
                }

                default: {
                    onStateChanged(State.WEIRD);
                    break;
                }
            }
        }
    }

    public void onStateChanged(@NonNull State state) {
        // to override
    }

}
