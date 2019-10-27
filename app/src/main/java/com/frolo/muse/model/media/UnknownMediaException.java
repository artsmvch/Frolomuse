package com.frolo.muse.model.media;


public class UnknownMediaException extends RuntimeException {
    final Media media;

    public UnknownMediaException(Media media) {
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }
}
