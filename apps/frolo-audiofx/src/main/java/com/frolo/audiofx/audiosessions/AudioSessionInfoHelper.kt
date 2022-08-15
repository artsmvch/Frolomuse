package com.frolo.audiofx.audiosessions

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.frolo.audiofx.AudioSessionInfo
import com.frolo.audiofx.R


internal object AudioSessionInfoHelper {
    fun default(context: Context): AudioSessionInfo {
        return GlobalAudioSessionInfoImpl(context)
    }

    fun external(context: Context, packageName: String?, audioSessionId: Int): AudioSessionInfo {
        val applicationInfo: ApplicationInfo? = try {
            context.packageManager.getApplicationInfo(packageName!!, 0)
        } catch (e: Throwable) {
            null
        }
        applicationInfo ?: return default(context)
        return ExternalAudioSessionInfoImpl(context, applicationInfo, audioSessionId)
    }
}

private class GlobalAudioSessionInfoImpl(
    private val context: Context
): AudioSessionInfo {
    override val name: CharSequence =
        context.getString(R.string.global_audio_session)
    override val description: CharSequence =
        context.getString(R.string.global_audio_session_description)
    override val icon: Drawable? =
        AppCompatResources.getDrawable(context, R.drawable.ic_global_mix_info_48)
}

private class ExternalAudioSessionInfoImpl(
    private val context: Context,
    private val applicationInfo: ApplicationInfo,
    private val audioSessionId: Int
): AudioSessionInfo {
    override val name: CharSequence by lazy {
        applicationInfo.runCatching { loadLabel(context.packageManager) }.getOrNull() ?: ""
    }
    override val description: CharSequence? by lazy {
        context.getString(R.string.external_audio_session_description, name)
    }
    override val icon: Drawable? by lazy {
        applicationInfo.runCatching { loadIcon(context.packageManager) }.getOrNull()
    }
}