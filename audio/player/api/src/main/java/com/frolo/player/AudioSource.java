package com.frolo.player;

import androidx.annotation.NonNull;

import java.io.Serializable;


/**
 * AudioSource represents audio that can be played by {@link Player}.
 * Audio sources are distinguished by {@link AudioSource#getURI()}.
 * {@link AudioSource#getMetadata()}} returns metadata that contains information about the AudioSource.
 */
public interface AudioSource extends Serializable {

    /**
     * Returns the uniform resource identifier (URI) of the AudioSource.
     * This can be a local filepath or http(https) URL.
     * @return the URI of the AudioSource.
     */
    String getURI();

    /**
     * Returns the metadata of the AudioSource.
     * NOTE: should be not null.
     * @return the metadata of the AudioSource.
     */
    @NonNull
    AudioMetadata getMetadata();

}
