package com.frolo.muse.ui.main.settings.library.filter

import com.frolo.muse.model.media.SongType


data class SongFilterItem(
    val type: SongType,
    val isChecked: Boolean
)