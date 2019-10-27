package com.frolo.muse.ui.main.library.myfiles

import androidx.collection.LruCache
import com.frolo.muse.model.media.MyFile


class MyFileCache constructor(
        maxSize: Int = 1_000
) : LruCache<MyFile, List<MyFile>>(maxSize) {

    override fun sizeOf(key: MyFile, value: List<MyFile>): Int {
        return value.size
    }

}