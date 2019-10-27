package com.frolo.muse.engine;

import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.Song;

import java.util.List;


public abstract class SongQueueFactory {

    public abstract SongQueue create(
            List<?extends Media> targets,
            List<Song> songs);

    public SongQueue create(
            @SongQueue.QueueType int type,
            long id,
            String name,
            List<Song> songs) {
        return SongQueue.create(type, id, name, songs);
    }
}
