package com.frolo.muse.common

import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.AudioType
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.media.SongType

val Song.durationInSeconds: Int get() = duration / 1000

fun SongType.toAudioType(): AudioType = Util.toAudioType(this)

fun Song.toAudioSource(): AudioSource {
    return if (this is AudioSource) this
    else Util.createAudioSource(this)
}

fun List<Song>.toAudioSources(): List<AudioSource> = map { it.toAudioSource() }