package com.frolo.muse.database.entity


/**
 * Exception that is thrown when an anomaly is detected in a playlist,
 * such as incorrect order of references between member entities.
 */
class PlaylistAnomaly(msg: String): IllegalStateException(msg)