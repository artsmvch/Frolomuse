package com.frolo.muse.di.impl.permission

import android.Manifest
import com.frolo.muse.permission.Perm
import com.frolo.muse.permission.PermissionRequester
import com.frolo.muse.ui.base.BaseFragment


class PermissionRequesterImpl constructor(
        private val fragment: BaseFragment
) : PermissionRequester {

    private fun Perm.toAndroidPermission(): String {
        return when(this) {
            Perm.READ_STORAGE -> Manifest.permission.READ_EXTERNAL_STORAGE
            Perm.WRITE_STORAGE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
            Perm.RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
        }
    }

    private fun String.toPerm(): Perm {
        return when(this) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> Perm.READ_STORAGE
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> Perm.WRITE_STORAGE
            Manifest.permission.RECORD_AUDIO -> Perm.RECORD_AUDIO
            else -> throw IllegalArgumentException(
                    "Unknown Android permission: $this"
            )
        }
    }

    override fun isPermissionGranted(perm: Perm): Boolean {
        return fragment.isPermissionGranted(
                perm.toAndroidPermission())
    }

    override fun arePermissionGranted(vararg perms: Perm): Boolean {
        for (perm in perms) {
            if (!fragment.isPermissionGranted(perm.toAndroidPermission())) {
                return false
            }
        }
        return true
    }

    override fun requestPermissions(vararg perms: Perm,
                                    onResult: (granted: Boolean) -> Unit) {
        val androidPermissions = perms.map { it.toAndroidPermission() }.toTypedArray()
        fragment.requestRxPermissions(*androidPermissions) { granted ->
            onResult(granted)
        }
    }

    override fun getCausePermissions(err: Throwable): List<Perm> {
        throw UnsupportedOperationException()
    }

}