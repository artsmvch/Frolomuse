package com.frolo.muse.engine.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;


// This does'nt work on newer version of Android API.
@Deprecated
public class HeadsetControlHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, final Intent intent) {
        String intentAction = intent.getAction();
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }
        KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            return;
        }

        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_NEXT: {
                    onSkipToNext();
                    break;
                }

                case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {
                    onSkipToPrevious();
                    break;
                }

                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: {
                    onTogglePlayback();
                    break;
                }
            }
        }


        abortBroadcast();
    }

    // to override
    public void onTogglePlayback() {
    }

    // to override
    public void onSkipToNext() {
    }

    // to override
    public void onSkipToPrevious() {
    }
}
