package com.frolo.muse.headset;

import android.content.Context;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.ThreadStrictMode;

import java.util.concurrent.atomic.AtomicReference;


/**
 * HeadsetHandler helps clients handle headset state changes (both wired and wireless).
 */
@MainThread
public final class HeadsetHandler {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String LOG_TAG = HeadsetHandler.class.getSimpleName();

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
        public void onStateChanged(@NonNull State state) {
            if (DEBUG) Log.d(LOG_TAG, "Jack state has been changed: " + state);
            mJackState.set(state);
            _handleState(state, mBluetoothHeadsetState.get());
        }
    };

    private final BluetoothHeadsetHandler mBluetoothHeadsetHandler = new BluetoothHeadsetHandler() {
        @Override
        public void onStateChanged(@NonNull State state) {
            if (DEBUG) Log.d(LOG_TAG, "Bluetooth headset state has been changed: " + state);
            mBluetoothHeadsetState.set(state);
            _handleState(mJackState.get(), state);
        }
    };

    private Context mContext;

    private final AtomicReference<HeadsetJackHandler.State> mJackState =
            new AtomicReference<>(null);
    private final AtomicReference<BluetoothHeadsetHandler.State> mBluetoothHeadsetState =
            new AtomicReference<>(null);

    private boolean mSubscribed = false;

    private HeadsetHandler(Callback callback) {
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
            // So strict only for debug builds
            if (DEBUG) throw new IllegalArgumentException("Handler is subscribed already");
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
        if (DEBUG) Log.d(LOG_TAG, "Subscribed: jack_state=" + mJackState.get() + ", bt_headset_state=" + mBluetoothHeadsetState.get());
    }

    public void dispose() {
        ThreadStrictMode.assertMain();

        if (mSubscribed) {
            if (mContext != null) {
                _dispose(mContext);
                mContext = null;
            }
            mSubscribed = false;
            if (DEBUG) Log.d(LOG_TAG, "Disposed");
        }
    }

    private void _subscribe(Context context) {
        mBluetoothHeadsetState.set(BluetoothHeadsetHandler.getCurrentState());
        mJackState.set(HeadsetJackHandler.getCurrentState(context));
        context.registerReceiver(mJackHandler, HeadsetJackHandler.createIntentFilter());
        context.registerReceiver(mBluetoothHeadsetHandler, BluetoothHeadsetHandler.createIntentFilter());
    }

    private void _dispose(Context context) {
        mBluetoothHeadsetState.set(null);
        mJackState.set(null);
        context.unregisterReceiver(mJackHandler);
        context.unregisterReceiver(mBluetoothHeadsetHandler);
    }

    /**
     * Handles states of the headset jack and the Bluetooth headset.
     * NOTE: if the <code>bluetoothHeadsetState</code> is null, this may mean that Bluetooth is turned off.
     * @param jackState the state of the headset jack
     * @param bluetoothHeadsetState the state of the Bluetooth headset
     */
    private void _handleState(
        @Nullable final HeadsetJackHandler.State jackState,
        @Nullable final BluetoothHeadsetHandler.State bluetoothHeadsetState
    ) {

        final Callback callback = mCallback;

        if (callback == null) {
            return;
        }

        // If either the headset jack is plugged OR the bluetooth headset is connected,
        // then we assume that the headset is connected.
        if (jackState == HeadsetJackHandler.State.PLUGGED
                || bluetoothHeadsetState == BluetoothHeadsetHandler.State.CONNECTED) {
            if (DEBUG) Log.d(LOG_TAG, "Dispatching: CONNECTED");
            callback.onConnected();
            return;
        }

        // If the headset jack is unplugged AND the bluetooth headset is disconnected (or Bluetooth is turned off altogether),
        // then we assume that the headset is disconnected.
        if (jackState == HeadsetJackHandler.State.UNPLUGGED
                && (bluetoothHeadsetState == null || bluetoothHeadsetState == BluetoothHeadsetHandler.State.DISCONNECTED)) {
            if (DEBUG) Log.d(LOG_TAG, "Dispatching: DISCONNECTED");
            callback.onDisconnected();
            return;
        }

        if (jackState == HeadsetJackHandler.State.WEIRD
                && (bluetoothHeadsetState == null || bluetoothHeadsetState == BluetoothHeadsetHandler.State.DISCONNECTED)) {
            if (DEBUG) Log.d(LOG_TAG, "Dispatching: WEIRD");
            callback.onBecomeWeird();
            return;
        }
    }

}
