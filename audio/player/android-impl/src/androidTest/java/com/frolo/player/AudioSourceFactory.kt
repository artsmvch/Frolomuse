package com.frolo.player

import android.content.Context
import android.provider.MediaStore
import com.frolo.player.data.AudioSources
import com.frolo.test.mockKT


internal class AudioSourceFactory(
    private val context: Context
) {

    fun getList(preferredSize: Int?): List<AudioSource> {
        val resolver = context.contentResolver
        val audioCursor = resolver.query(URI, PROJECT, null, null, null)
            ?: throw NullPointerException("Query returned null cursor: uri=$URI")

        val dstList = ArrayList<AudioSource>(preferredSize ?: 10)

        audioCursor.use { cursor ->
            var index: Int = 0
            if (cursor.moveToFirst()) {
                do {
                    val id: Long = index++.toLong()
                    val path = cursor.getString(cursor.getColumnIndex(PROJECT[0]))
                    if (path.isNullOrBlank()) {
                        continue
                    }
                    val audioSource = mockAudioSource(id, path)
                    dstList.add(audioSource)
                    if (preferredSize != null && dstList.size >= preferredSize) {
                        break
                    }
                } while (cursor.moveToNext())
            }
        }

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