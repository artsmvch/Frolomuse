package com.frolo.audiofx;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.CopyOnWriteArraySet;


/**
 * The thread-safe registry for observers of type {@link AudioFxObserver}.
 * NOTE: All observer methods are called on the main thread.
 */
final class AudioFxObserverRegistry {

    static AudioFxObserverRegistry create(@NonNull Context context, @NonNull AudioFx audioFx) {
        return new AudioFxObserverRegistry(context, audioFx);
    }

    private class DispatcherHandler extends Handler {
        static final int MSG_ENABLED = 0;
        static final int MSG_DISABLED = 1;
        static final int MSG_BAND_LEVEL_CHANGED = 3;
        static final int MSG_PRESET_USED = 4;
        static final int MSG_BASS_STRENGTH_CHANGED = 5;
        static final int MSG_VIRTUALIZER_STRENGTH_CHANGED = 6;
        static final int MSG_REVERB_CHANGED = 7;

        DispatcherHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case MSG_ENABLED: {
                    for (AudioFxObserver observer : mObservers) {
                        observer.onEnabled(mAudioFx);
                    }
                    break;
                }

                case MSG_DISABLED: {
                    for (AudioFxObserver observer : mObservers) {
                        observer.onDisabled(mAudioFx);
                    }
                    break;
                }

                case MSG_BAND_LEVEL_CHANGED: {
                    final short band = (short) msg.arg1;
                    final short level = (short) msg.arg2;
                    for (AudioFxObserver observer : mObservers) {
                        observer.onBandLevelChanged(mAudioFx, band, level);
                    }
                    break;
                }

                case MSG_PRESET_USED: {
                    final Object obj = msg.obj;
                    if (obj instanceof Preset) {
                        for (AudioFxObserver observer : mObservers) {
                            observer.onPresetUsed(mAudioFx, (Preset) obj);
                        }
                    }
                    break;
                }

                case MSG_BASS_STRENGTH_CHANGED: {
                    final short bassStrength = (short) msg.arg1;
                    for (AudioFxObserver observer : mObservers) {
                        observer.onBassStrengthChanged(mAudioFx, bassStrength);
                    }
                    break;
                }

                case MSG_VIRTUALIZER_STRENGTH_CHANGED: {
                    final short bassStrength = (short) msg.arg1;
                    for (AudioFxObserver observer : mObservers) {
                        observer.onVirtualizerStrengthChanged(mAudioFx, bassStrength);
                    }
                    break;
                }

                case MSG_REVERB_CHANGED: {
                    final Object obj = msg.obj;
                    if (obj instanceof Reverb) {
                        for (AudioFxObserver observer : mObservers) {
                            observer.onReverbUsed(mAudioFx, (Reverb) obj);
                        }
                    }
                    break;
                }
            }
        }
    }

    private final AudioFx mAudioFx;

    private final DispatcherHandler mHandler;

    private final CopyOnWriteArraySet<AudioFxObserver> mObservers = new CopyOnWriteArraySet<>();

    private AudioFxObserverRegistry(@NonNull Context context, @NonNull AudioFx audioFx) {
        mAudioFx = audioFx;
        mHandler = new DispatcherHandler(context.getMainLooper());
    }

    void register(@Nullable AudioFxObserver observer) {
        if (observer == null) {
            return;
        }

        mObservers.add(observer);
    }

    void unregister(@Nullable AudioFxObserver observer) {
        if (observer == null) {
            return;
        }

        mObservers.remove(observer);
    }

    synchronized void dispatchEnabled() {
        mHandler.removeMessages(DispatcherHandler.MSG_ENABLED);
        mHandler.removeMessages(DispatcherHandler.MSG_DISABLED);
        final Message message = mHandler.obtainMessage(DispatcherHandler.MSG_ENABLED);
        message.sendToTarget();
    }

    synchronized void dispatchDisabled() {
        mHandler.removeMessages(DispatcherHandler.MSG_ENABLED);
        mHandler.removeMessages(DispatcherHandler.MSG_DISABLED);
        final Message message = mHandler.obtainMessage(DispatcherHandler.MSG_DISABLED);
        message.sendToTarget();
    }

    synchronized void dispatchBandLevelChanged(short band, short level) {
        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_BAND_LEVEL_CHANGED, band, level);
        message.sendToTarget();
    }

    synchronized void dispatchPresetUsed(Preset preset) {
        mHandler.removeMessages(DispatcherHandler.MSG_PRESET_USED);
        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_PRESET_USED, preset);
        message.sendToTarget();
    }

    synchronized void dispatchBassStrengthChanged(short strength) {
        mHandler.removeMessages(DispatcherHandler.MSG_BASS_STRENGTH_CHANGED);
        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_BASS_STRENGTH_CHANGED, strength, 0);
        message.sendToTarget();
    }

    synchronized void dispatchVirtualizerStrengthChanged(short strength) {
        mHandler.removeMessages(DispatcherHandler.MSG_VIRTUALIZER_STRENGTH_CHANGED);
        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_VIRTUALIZER_STRENGTH_CHANGED, strength, 0);
        message.sendToTarget();
    }

    synchronized void dispatchReverbUsed(Reverb reverb) {
        mHandler.removeMessages(DispatcherHandler.MSG_REVERB_CHANGED);
        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_REVERB_CHANGED, reverb);
        message.sendToTarget();
    }

}
