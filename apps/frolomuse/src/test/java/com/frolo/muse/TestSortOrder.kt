package com.frolo.muse

import com.frolo.music.model.SortOrder


class TestSortOrder constructor(
    private val name: String,
    private val key: String
): SortOrder() {

    constructor(key: String): this(key, key)

    override fun getLocalizedName(): String = name

    override fun getKey(): String = key

}