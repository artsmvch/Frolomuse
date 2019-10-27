package com.frolo.muse.engine.service;

import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.frolo.muse.BuildConfig;


public class MediaSessionCallback extends MediaSessionCompat.Callback {
    private final static String TAG = MediaSessionCallback.class.getSimpleName();

    @Override
    public boolean onMediaButtonEvent(@NonNull Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (event != null) {
            dump(event);
            final int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                int keyCode = event.getKeyCode();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_HEADSETHOOK: {
                        // This may be the center button on earphones.
                        // So let's handle it as for the toggle button.
                        onTogglePlayback();
                        return true;
                    }

                    case KeyEvent.KEYCODE_MEDIA_NEXT: {
                        onSkipToNext();
                        return true;
                    }

                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {
                        onSkipToPrevious();
                        return true;
                    }

                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: {
                        onTogglePlayback();
                        return true;
                    }
                }
            }
        }

        return super.onMediaButtonEvent(intent);
    }

    private void dump(KeyEvent event) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Action:[" + event.getAction() + "]");
        }
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
