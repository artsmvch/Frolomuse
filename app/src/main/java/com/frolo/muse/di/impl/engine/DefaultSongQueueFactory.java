package com.frolo.muse.di.impl.engine;

import com.frolo.muse.engine.SongQueue;
import com.frolo.muse.engine.SongQueueFactory;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;

import java.util.List;


public class DefaultSongQueueFactory extends SongQueueFactory {

    @Override
    public SongQueue create(List<? extends Media> targets, List<Song> songs) {
        if (targets.size() == 1) {
            Media target = targets.get(0);

            @SongQueue.QueueType
            final int type;
            final long id;
            final String name;

            switch (target.getKind()) {
                case Media.ALBUM:
                    type = SongQueue.ALBUM;
                    id = target.getId();
                    name = ((Album) target).getName();
                    break;
                case Media.ARTIST:
                    type = SongQueue.ARTIST;
                    id = target.getId();
                    name = ((Artist) target).getName();
                    break;
                case Media.GENRE:
                    type = SongQueue.GENRE;
                    id = target.getId();
                    name = ((Genre) target).getName();
                    break;
                case Media.MY_FILE:
                    type = SongQueue.FOLDER;
                    id = target.getId();
                    name = ((MyFile) target).getJavaFile().getName();
                    break;
                case Media.PLAYLIST:
                    type = SongQueue.PLAYLIST;
                    name = ((Playlist) target).getName();
                    id = target.getId();
                    break;
                case Media.SONG:
                    type = SongQueue.SINGLE;
                    name = ((Song) target).getTitle();
                    id = target.getId();
                    break;
                default:
                    type = SongQueue.CHUNK;
                    id = SongQueue.NO_ID;
                    name = "";
                    break;
            }

            return SongQueue.create(type, id, name, songs);
        }

        return SongQueue.create(SongQueue.CHUNK, SongQueue.NO_ID, "", songs);
    }

}
