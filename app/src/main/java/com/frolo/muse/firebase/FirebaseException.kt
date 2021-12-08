package com.frolo.muse.firebase


/**
 * Generic Firebase-related exception.
 */
internal class FirebaseException : Exception {
    constructor(cause: Throwable?): super(cause)
    constructor(message: String?): super(message)
    constructor(message: String, cause: Throwable?): super(message, cause)
}