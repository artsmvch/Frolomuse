package com.frolo.muse.model.media

import java.io.Serializable


data class MediaFile constructor(
    private val id: Long,
    val name: String?,
    val bucketId: Long,
    val bucketName: String?,
    val relativePath: String?,
    val mimeType: String?
) : Media, Serializable {

    override fun getId(): Long = id

    override fun getKind(): Int {
        return Media.MEDIA_FILE
    }

}