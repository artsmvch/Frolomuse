package com.frolo.player;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;


final class MediaPlayerErrors {

    @NonNull
    static String getWhatMessage(int what) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_IO:
                return "IO";
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                return "MALFORMED";
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                return "NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                return "SERVER_DIED";
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                return "TIMED_OUT";
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                return "UNKNOWN";
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                return "UNSUPPORTED";
            default:
                return String.valueOf(what);
        }
    }

    @NonNull
    static String getErrorMessage(int what, int extra) {
        return "what=" + getWhatMessage(what) + ", extra=" + extra;
    }

    private MediaPlayerErrors() {
    }
}
