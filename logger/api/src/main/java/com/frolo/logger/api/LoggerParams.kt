package com.frolo.logger.api

data class LoggerParams(
    val logDelegate: LogDelegate,
    val tagPrefix: String? = null
)