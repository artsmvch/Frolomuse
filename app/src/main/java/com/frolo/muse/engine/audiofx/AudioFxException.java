package com.frolo.muse.engine.audiofx;


/**
 * Exception that is thrown by {@link AudioFxImpl} to report an internal error.
 */
final class AudioFxException extends RuntimeException {

    AudioFxException(Throwable cause) {
        super(cause);
    }

    AudioFxException(String message) {
        super(message);
    }

}