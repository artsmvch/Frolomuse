package com.frolo.music.model

import java.io.Serializable


data class MediaFile constructor(
    private val id: Long,
    val name: String?,
    val bucketId: Long,
    val bucketName: String?,
    val relativePath: String?,
    val mimeType: String?
) : Media, Serializable {

    override fun getMediaId(): MediaId = MediaId.createLocal(Media.MEDIA_FILE, id)

}