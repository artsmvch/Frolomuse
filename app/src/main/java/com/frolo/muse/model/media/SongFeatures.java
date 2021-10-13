package com.frolo.muse.model.media;

import com.frolo.muse.OS;

import org.jetbrains.annotations.NotNull;


public final class SongFeatures {

    public static boolean isSongTypeSupported(@NotNull SongType type) {
        if (type == SongType.AUDIOBOOK) {
            return OS.isAtLeastQ();
        }
        return true;
    }

    private SongFeatures() {
    }
}
