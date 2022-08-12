package com.frolo.logger.api

data class LoggerParams(
    val isEnabled: Boolean,
    val logDelegate: LogDelegate,
    val tagPrefix: String? = null
)