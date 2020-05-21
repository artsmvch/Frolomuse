package com.frolo.muse.permission;


/**
 * Abstract layer for checking permissions.
 */
public interface PermissionChecker {

    /**
     * Checks if the app is permitted to query media content.
     * If the app does not have this permission then a {@link SecurityException} is thrown.
     * @throws SecurityException if the app does not have permission to query media content
     */
    void requireQueryMediaContentPermission() throws SecurityException;

}
