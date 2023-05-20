package com.frolo.player;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


final class AudioFocusRequesterImpl implements
        AudioFocusRequester,
        AudioManager.OnAudioFocusChangeListener,
        AutoCloseable {

    @NonNull
    public static AudioFocusRequester.Factory getFactory(@NonNull Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return player -> new AudioFocusRequesterImpl(audioManager, player);
    }

    @NonNull
    private final Player mPlayer;
    @Nullable
    private final AudioManager mAudioManager;

    // This flag is used to remember the last playback state.
    // So we can know if the player was playing before the audio focus loss.
    // And then resume the playback again if needed.
    private final AtomicBoolean mWasPlaying = new AtomicBoolean(false);

    private final AtomicReference<AudioFocusRequest> mAudioFocusRequest = new AtomicReference<>();

    private AudioFocusRequesterImpl(@Nullable AudioManager audioManager, @NonNull Player player) {
        mAudioManager = audioManager;
        mPlayer = player;
    }

    @Override
    public boolean requestAudioFocus() {
        final AudioManager manager = mAudioManager;
        if (manager == null) {
            // Audio manager is null, we can't request audio focus, it's supposed to be granted
            return true;
        }
        final int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
            final AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build();
            mAudioFocusRequest.set(request);
            result = manager.requestAudioFocus(request);
        } else {
            result = manager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        switch (result) {
            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                return false;
            case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                return true;
            default: return true;
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                // Pause playback because your Audio Focus was
                // temporarily stolen, but will be back soon.
                // i.e. for a phone cal
                mWasPlaying.set(mPlayer.isPlaying());
                mPlayer.pause();
                break;
            }

            case AudioManager.AUDIOFOCUS_LOSS: {
                // Stop playback, because you lost the Audio Focus.
                // i.e. the user started some other playback app
                // Remember to unregister your controls/buttons here.
                // And release the kra — Audio Focus!
                // You’re done.
                mWasPlaying.set(mPlayer.isPlaying());
                abandonAudioFocus();
                mPlayer.pause();
                break;
            }

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                // Lower the volume, because something else is also
                // playing audio over you.
                // i.e. for notifications or navigation directions
                // Depending on your audio playback, you may prefer to
                // pause playback here instead. You do you.
                mWasPlaying.set(mPlayer.isPlaying());
                break;
            }

            case AudioManager.AUDIOFOCUS_GAIN: {
                // Resume playback, because you hold the Audio Focus
                // again!
                // i.e. the phone call ended or the nav directions
                // are finished
                // If you implement ducking and lower the volume, be
                // sure to return it to normal here, as well.
                if (mWasPlaying.get()) { // ok, the player was playing, we're good to resume playback
                    mPlayer.start();
                }
                break;
            }

            default: break;
        }
    }

    private void abandonAudioFocus() {
        final AudioManager manager = mAudioManager;
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                final AudioFocusRequest request = mAudioFocusRequest.get();
                if (request != null) {
                    manager.abandonAudioFocusRequest(request);
                }
            } else {
                manager.abandonAudioFocus(this);
            }
        }
    }

    @Override
    public void close() {
        abandonAudioFocus();
    }
}
