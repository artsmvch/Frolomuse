package com.frolo.player;

import androidx.annotation.NonNull;


/**
 * An abstract layer that requests audio focus on the system.
 */
public interface AudioFocusRequester {

    /**
     * Requests audio focus on the system.
     * @return true if the focus is granted, false - otherwise.
     */
    boolean requestAudioFocus();

    interface Factory {
        @NonNull
        AudioFocusRequester create(@NonNull Player player);
    }

}
