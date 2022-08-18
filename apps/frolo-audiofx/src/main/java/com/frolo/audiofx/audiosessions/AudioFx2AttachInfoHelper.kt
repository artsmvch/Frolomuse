package com.frolo.audiofx.audiosessions

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.frolo.audiofx.AudioFx2AttachInfo
import com.frolo.audiofx.R


internal object AudioFx2AttachInfoHelper {
    fun default(context: Context): AudioFx2AttachInfo {
        return GlobalAudioFx2AttachInfoImpl(context)
    }

    fun external(context: Context, packageName: String?, audioSessionId: Int): AudioFx2AttachInfo {
        val applicationInfo: ApplicationInfo? = try {
            context.packageManager.getApplicationInfo(packageName!!, 0)
        } catch (e: Throwable) {
            null
        }
        applicationInfo ?: return default(context)
        return ExternalAudioFx2AttachInfoImpl(context, applicationInfo, audioSessionId)
    }
}

private class GlobalAudioFx2AttachInfoImpl(
    private val context: Context
): AudioFx2AttachInfo {
    override val name: CharSequence =
        context.getString(R.string.global_audio_session)
    override val description: CharSequence =
        context.getString(R.string.global_audio_session_description)
    override val icon: Drawable? =
        AppCompatResources.getDrawable(context, R.drawable.ic_global_mix_info_48)
}

private class ExternalAudioFx2AttachInfoImpl(
    private val context: Context,
    private val applicationInfo: ApplicationInfo,
    private val audioSessionId: Int
): AudioFx2AttachInfo {
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