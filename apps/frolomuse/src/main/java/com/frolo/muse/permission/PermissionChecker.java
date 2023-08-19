package com.frolo.muse.permission;


/**
 * Abstract layer for checking permissions.
 */
public interface PermissionChecker {

    /**
     * Checks if the app is permitted to read audio.
     * @return true if the app has permission to read audio, false - otherwise
     */
    boolean isReadAudioPermissionGranted();

    /**
     * This method should only be called after the read audio permission request has been processed.
     * @return true if the read audio permission should be requested in the settings.
     */
    boolean shouldRequestReadAudioPermissionInSettings();
}
