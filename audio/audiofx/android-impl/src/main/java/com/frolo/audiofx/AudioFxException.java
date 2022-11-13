package com.frolo.audiofx;


/**
 * Exception that is thrown by {@link AudioFxImpl} to report an internal error.
 */
@Deprecated
final class AudioFxException extends RuntimeException {

    AudioFxException(Throwable cause) {
        super(cause);
    }

    AudioFxException(String message) {
        super(message);
    }

}
