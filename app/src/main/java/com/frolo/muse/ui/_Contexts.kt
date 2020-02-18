package com.frolo.muse.ui

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.model.media.Song
import java.io.File

private fun Context.getLinkToAppInStore() = "https://play.google.com/store/apps/details?id=$packageName"

private fun isAndroidN7OrHigher(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}

private fun Context.getUri(file: File): Uri {
    return if (isAndroidN7OrHigher()){
        FileProvider.getUriForFile(this, this.packageName + ".fileprovider", file)
    } else{
        Uri.fromFile(file)
    }
}

// Checks if the package manager can resolve the given intent before starting activity
private fun Context.safelyStartActivity(intent: Intent) {
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

fun Context.isInLandscapeMode(): Boolean {
    val orientation = resources.configuration.orientation
    return orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Context.share(uri: Uri, title: String) {
    val intent = Intent(Intent.ACTION_SEND)
    if (isAndroidN7OrHigher()) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    intent.type = "audio/*"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    safelyStartActivity(Intent.createChooser(intent, title))
}

fun Context.share(uris: List<Uri>, title: String) {
    if (uris.isEmpty())
        return
    if (uris.size == 1) {
        share(uris[0], getString(R.string.share_track))
        return
    }
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
    if (isAndroidN7OrHigher()) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    intent.type = "audio/*"
    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    safelyStartActivity(Intent.createChooser(intent, title))
}

fun Context.share(song: Song) {
    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
    share(uri, getString(R.string.share_track))
}

fun Context.share(songs: List<Song>) {
    val uris = songs.map { ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it.id) }
    share(uris, getString(R.string.share_tracks))
}

fun Context.shareApp() {
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, getLinkToAppInStore())
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.checkout_out))
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        safelyStartActivity(this)
    }
}

fun Context.openRingCutter(src: String) {
    val intent = Intent(Intent.ACTION_EDIT, Uri.parse(src)).apply {
        putExtra("was_get_content_intent", true)
        setClass(this@openRingCutter, com.alexfrolov.ringdroid.RingdroidEditActivity::class.java)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    safelyStartActivity(intent)
}

fun Context.sharePoster(song: Song, file: File) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, getUri(file))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val subject = getString(R.string.checkout_this_track, song.title)
    val text = subject + " " + getLinkToAppInStore()
    intent.putExtra(Intent.EXTRA_TEXT, text)
    safelyStartActivity(intent)
}

fun Context.goToStore() {
    val googlePlayIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName"))

    // At first we want Google Play to handle this intent
    val intent: Intent = if (googlePlayIntent.resolveActivity(packageManager) != null) {
        googlePlayIntent
    } else { // If Google PLay is not installed (almost impossible) search for other Market App
        val uri = Uri.parse("market://details?id=$packageName")
        Intent(Intent.ACTION_VIEW, uri)
    }

    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    }

    safelyStartActivity(intent)
}

fun Context.contactDeveloper() {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", BuildConfig.DEVELOPER_EMAIL, null))
    val subject = getString(R.string.app_name)
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)

    safelyStartActivity(intent)
}

fun Context.helpWithTranslations() {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", BuildConfig.DEVELOPER_EMAIL, null))
    val subject = getString(R.string.app_name) + " " + getString(R.string.help_with_translations)
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)

    safelyStartActivity(intent)
}