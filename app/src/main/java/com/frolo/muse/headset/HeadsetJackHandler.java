package com.frolo.muse.headset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


/**
 * This handles the change in state of the headset jack, namely:
 * 1) when the headsets were plugged to the jack;
 * 2) when the headsets were unplugged from the jack;
 * 3) when the headsets become weird (unknown state).
 */
public class HeadsetJackHandler extends BroadcastReceiver {

    public static IntentFilter createIntentFilter() {
        return new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0: onHeadsetUnplugged(context); break;
                case 1: onHeadsetPlugged(context); break;
                default: onHeadsetBecomeWeird(context);
            }
        }
    }

    public void onHeadsetUnplugged(Context context) {
        // to be implemented
    }

    public void onHeadsetPlugged(Context context) {
        // to be implemented
    }

    public void onHeadsetBecomeWeird(Context context) {
        // to be implemented
    }
}
