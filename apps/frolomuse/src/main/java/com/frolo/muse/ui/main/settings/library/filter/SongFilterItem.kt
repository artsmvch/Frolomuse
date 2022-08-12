package com.frolo.muse.ui.main.settings.library.filter

import com.frolo.music.model.SongType


data class SongFilterItem(
    val type: SongType,
    val isChecked: Boolean
)