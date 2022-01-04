package com.frolo.player;


/**
 * An abstract layer that requests audio focus on the system.
 */
public interface AudioFocusRequester {

    /**
     * Requests audio focus on the system.
     * @return true if the focus is granted, false - otherwise.
     */
    boolean requestAudioFocus();

}
