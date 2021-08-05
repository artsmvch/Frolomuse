package java.com.frolo.muse

import com.frolo.muse.model.media.Media


class TestMedia constructor(private val _id: Long): Media {
    override fun getId(): Long = _id
    override fun getKind(): Int = Media.NONE
}