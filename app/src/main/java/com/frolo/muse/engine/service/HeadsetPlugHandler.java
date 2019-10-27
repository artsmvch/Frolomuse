package com.frolo.muse.engine.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class HeadsetPlugHandler extends BroadcastReceiver {

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
