package com.frolo.muse.permission


interface PermissionRequester {

    fun isPermissionGranted(perm: Perm): Boolean

    fun arePermissionGranted(vararg perms: Perm): Boolean

    fun requestPermissions(vararg perms: Perm,
                           onResult: (granted: Boolean) -> Unit)

    fun getCausePermissions(err: Throwable): List<Perm>

}