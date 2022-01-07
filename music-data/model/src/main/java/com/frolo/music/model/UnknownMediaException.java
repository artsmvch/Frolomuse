package com.frolo.music.model;


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
