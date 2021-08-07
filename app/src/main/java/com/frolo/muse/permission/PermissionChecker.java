package com.frolo.muse.permission;


/**
 * Abstract layer for checking permissions.
 */
public interface PermissionChecker {

    /**
     * Checks if the app is permitted to query media content.
     * @return true if the app has permission to query media content, false - otherwise
     */
    boolean isQueryMediaContentPermissionGranted();

    /**
     * Checks if the app is permitted to query media content.
     * The implementation may try to get this permission, if not already.
     * Finally, if the app does not have this permission and cannot get it then a {@link SecurityException} is thrown.
     * @throws SecurityException if the app does not have permission to query media content and failed to acquire it
     */
    void requireQueryMediaContentPermission() throws SecurityException;

    /**
     * @return true if the media permission should be requested in the settings.
     */
    boolean shouldRequestMediaPermissionInSettings();

}
