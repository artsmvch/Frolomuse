package com.frolo.muse.android

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.frolo.muse.OS
import java.io.File


fun Context.resolveUri(file: File): Uri {
    return if (OS.isAtLeastN()) {
        FileProvider.getUriForFile(this, this.packageName + ".fileprovider", file)
    } else{
        Uri.fromFile(file)
    }
}