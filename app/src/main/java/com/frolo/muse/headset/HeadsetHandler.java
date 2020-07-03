package com.frolo.muse.headset;

import android.content.Context;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.frolo.muse.ThreadStrictMode;


/**
 * HeadsetHandler helps clients handle headset state changes (both wired and wireless).
 * TODO: implement handling of bluetooth headsets
 */
@MainThread
public final class HeadsetHandler {

    public interface Callback {
        void onConnected();
        void onDisconnected();
        void onBecomeWeird();
    }

    public static HeadsetHandler create(@NonNull Callback callback) {
        return new HeadsetHandler(callback);
    }

    private final Callback mCallback;

    private final HeadsetJackHandler mJackHandler = new HeadsetJackHandler() {
        @Override
        public void onHeadsetPlugged(Context context) {
            mCallback.onConnected();
        }

        @Override
        public void onHeadsetUnplugged(Context context) {
            mCallback.onDisconnected();
        }

        @Override
        public void onHeadsetBecomeWeird(Context context) {
            mCallback.onBecomeWeird();
        }
    };

    private Context mContext;

    private boolean mSubscribed = false;

    private HeadsetHandler(@NonNull Callback callback) {
        mCallback = callback;
    }

    /**
     * Subscribe the handler to the given <code>context</code> to listen to headset state changes.
     * The call to this method should be paired with a call to {@link HeadsetHandler#dispose()} to avoid memory leaks.
     * @param context to subscribe
     */
    public void subscribe(@NonNull Context context) {
        ThreadStrictMode.assertMain();

        if (mSubscribed) {
            throw new IllegalArgumentException("Handler is subscribed already");
        }

        if (mContext != null) {
            // Disposing from the old context.
            // In fact, this should not happen.
            _dispose(mContext);
        }

        mContext = context;
        // subscribing to the new context
        _subscribe(context);
        mSubscribed = true;
    }

    public void dispose() {
        ThreadStrictMode.assertMain();
        if (mSubscribed) {
            if (mContext != null) {
                _dispose(mContext);
                mContext = null;
            }
            mSubscribed = false;
        }
    }

    private void _subscribe(Context context) {
        context.registerReceiver(mJackHandler, HeadsetJackHandler.createIntentFilter());
    }

    private void _dispose(Context context) {
        context.unregisterReceiver(mJackHandler);
    }

}
