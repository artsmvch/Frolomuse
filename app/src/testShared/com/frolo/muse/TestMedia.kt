package com.frolo.muse

import com.frolo.music.model.Media


class TestMedia constructor(private val _id: Long): Media {
    override fun getId(): Long = _id
    override fun getKind(): Int = Media.NONE
}