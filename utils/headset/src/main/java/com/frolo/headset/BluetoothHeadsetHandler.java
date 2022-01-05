package com.frolo.headset;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class BluetoothHeadsetHandler extends BroadcastReceiver {

    enum State {
        CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
    }

    public static IntentFilter createIntentFilter() {
        return new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
    }

    /**
     * Returns the current state of the Bluetooth headset.
     * Returning <code>null</code> may also mean that Bluetooth is turned off.
     * @return the current state of the Bluetooth headset or null.
     */
    @Nullable
    public static State getCurrentState() {

        try {
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                final int stateIntValue = bluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET);
                // BluetoothHeadset.A2DP can also be used for Stereo media devices.
                return _resolveState(stateIntValue);
            }

            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private static State _resolveState(/* @BluetoothHeadset.BtProfileState */ int stateIntValue) {
        switch (stateIntValue) {
            case BluetoothHeadset.STATE_CONNECTING:
                return State.CONNECTING;

            case BluetoothHeadset.STATE_CONNECTED:
                return State.CONNECTED;

            case BluetoothHeadset.STATE_DISCONNECTING:
                return State.DISCONNECTING;

            case BluetoothHeadset.STATE_DISCONNECTED:
                return State.DISCONNECTED;

            default:
                return null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();

        if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            final int stateIntValue = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);

            final State state = _resolveState(stateIntValue);
            if (state != null) {
                onStateChanged(state);
            }

        }
    }

    public void onStateChanged(@NonNull State state) {
        // to override
    }

}
