package com.frolo.muse.android

import android.content.Context
import android.content.pm.PackageManager


val Context.firstPackageInstallTime: Long
    get() {
        return try {
            packageManager.getPackageInfo(packageName, 0).firstInstallTime
        } catch (e: PackageManager.NameNotFoundException) {
            System.currentTimeMillis()
        }
    }

val Context.lastPackageUpdateTime: Long
    get() {
        return try {
            packageManager.getPackageInfo(packageName, 0).lastUpdateTime
        } catch (e: PackageManager.NameNotFoundException) {
            System.currentTimeMillis()
        }
    }