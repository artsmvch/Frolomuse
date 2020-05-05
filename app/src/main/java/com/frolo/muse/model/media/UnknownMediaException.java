package com.frolo.muse.model.media;


public class UnknownMediaException extends RuntimeException {
    final Media media;

    public UnknownMediaException(Media media) {
        super("Unknown kind of media " + media);
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }
}
