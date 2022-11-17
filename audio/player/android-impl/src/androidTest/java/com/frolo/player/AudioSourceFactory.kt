package com.frolo.player

import android.content.Context
import android.provider.MediaStore
import com.frolo.player.data.AudioSources
import com.frolo.test.mockKT


internal class AudioSourceFactory(
    private val context: Context
) {

    fun getList(preferredSize: Int?): List<AudioSource> {
        val list = getAllAudioSources()
        return if (preferredSize != null) {
            list.take(preferredSize)
        } else {
            list
        }
    }

    private fun getAllAudioSources(): List<AudioSource> {
        val dstList = ArrayList<AudioSource>()
        var index: Int = 0

        // Searching external storage for music files
        val resolver = context.contentResolver
        val audioCursor = resolver.query(URI, PROJECT, null, null, null)
            ?: throw NullPointerException("Query returned null cursor: uri=$URI")
        audioCursor.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val id: Long = index++.toLong()
                    val path = cursor.getString(cursor.getColumnIndex(PROJECT[0]))
                    if (path.isNullOrBlank()) {
                        continue
                    }
                    val audioSource = mockAudioSource(id, path)
                    dstList.add(audioSource)
                } while (cursor.moveToNext())
            }
        }

        // Searching assets for music files
        val assets = context.assets
        val assetAudioSources = assets.list("mp3")
            .orEmpty()
            .map { path -> mockAudioSource(index++.toLong(), path) }
        dstList.addAll(assetAudioSources)

        return dstList
    }

    private fun mockAudioSource(id: Long, path: String): AudioSource {
        val metadata = mockKT<AudioMetadata>()
        return AudioSources.createAudioSource(id, path, metadata)
    }

    companion object {
        private val URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        private val PROJECT: Array<String> = arrayOf(MediaStore.Audio.Media.DATA)
    }

}