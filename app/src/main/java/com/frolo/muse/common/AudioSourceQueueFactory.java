package com.frolo.muse.common;

import com.frolo.muse.engine.AudioSource;
import com.frolo.muse.engine.AudioSourceQueue;
import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Deprecated
public abstract class AudioSourceQueueFactory {

    @Deprecated
    public abstract AudioSourceQueue create(List<?extends Media> targets, List<Song> songs);

    public AudioSourceQueue create(@AudioSourceQueue.QueueType int type, long id, String name, List<Song> songs) {
        final List<AudioSource> items;

        if (songs == null || songs.isEmpty()) {
            items = Collections.emptyList();
        } else {
            items = new ArrayList<>(songs.size());
            for (Song song : songs) {
                items.add(Util.createAudioSource(song));
            }
        }

        return AudioSourceQueue.create(type, id, name, items);
    }
}
