package com.frolo.player;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;


/**
 * AudioSource represents audio that can be played by {@link Player}.
 * Audio sources are distinguished by {@link AudioSource#getId()}.
 * {@link AudioSource#getMetadata()}} returns metadata that contains information about the AudioSource.
 */
public interface AudioSource extends Serializable {

    /**
     * Returns unique identifier of the AudioSource.
     * @return unique identifier of the AudioSource.
     */
    long getId();

    /**
     * Returns the path to the AudioSource.
     * This can be a local filepath or http(https) URL.
     * @return the path to the AudioSource.
     */
    String getSource();

    /**
     * Returns the metadata of the AudioSource.
     * NOTE: should be not null.
     * @return the metadata of the AudioSource.
     */
    @NotNull
    AudioMetadata getMetadata();

}
