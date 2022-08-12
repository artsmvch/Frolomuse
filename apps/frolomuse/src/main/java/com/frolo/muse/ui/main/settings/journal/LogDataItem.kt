package com.frolo.muse.ui.main.settings.journal


data class LogDataItem(
    val time: String,
    val message: String,
    val errorStackTrace: String?
)