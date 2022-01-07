package com.frolo.muse.common

import com.frolo.player.AudioSource
import com.frolo.player.AudioType
import com.frolo.music.model.Song
import com.frolo.music.model.SongType

val Song.durationInSeconds: Int get() = duration / 1000

fun SongType.toAudioType(): AudioType = Util.toAudioType(this)

fun Song.toAudioSource(): AudioSource {
    return if (this is AudioSource) this
    else Util.createAudioSource(this)
}

fun List<Song>.toAudioSources(): List<AudioSource> = map { it.toAudioSource() }