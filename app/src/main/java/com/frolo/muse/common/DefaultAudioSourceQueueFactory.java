package com.frolo.muse.common;

import com.frolo.muse.engine.AudioSource;
import com.frolo.muse.engine.AudioSourceQueue;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;

import org.jetbrains.annotations.NotNull;

import java.util.List;


@Deprecated
public final class DefaultAudioSourceQueueFactory extends AudioSourceQueueFactory {

    private static final DefaultAudioSourceQueueFactory sInstance = new DefaultAudioSourceQueueFactory();

    @NotNull
    public static DefaultAudioSourceQueueFactory getInstance() {
        return sInstance;
    }

    private DefaultAudioSourceQueueFactory() {
    }

    @Deprecated
    @Override
    public AudioSourceQueue create(List<? extends Media> targets, List<Song> songs) {

        final List<AudioSource> audioSources = Util.createAudioSourceList(songs);

        if (targets.size() == 1) {
            Media target = targets.get(0);

            @AudioSourceQueue.QueueType
            final int type;
            final long id;
            final String name;

            switch (target.getKind()) {
                case Media.ALBUM:
                    type = AudioSourceQueue.ALBUM;
                    id = target.getId();
                    name = ((Album) target).getName();
                    break;
                case Media.ARTIST:
                    type = AudioSourceQueue.ARTIST;
                    id = target.getId();
                    name = ((Artist) target).getName();
                    break;
                case Media.GENRE:
                    type = AudioSourceQueue.GENRE;
                    id = target.getId();
                    name = ((Genre) target).getName();
                    break;
                case Media.MY_FILE:
                    type = AudioSourceQueue.FOLDER;
                    id = target.getId();
                    name = ((MyFile) target).getJavaFile().getName();
                    break;
                case Media.PLAYLIST:
                    type = AudioSourceQueue.PLAYLIST;
                    name = ((Playlist) target).getName();
                    id = target.getId();
                    break;
                case Media.SONG:
                    type = AudioSourceQueue.SINGLE;
                    name = ((Song) target).getTitle();
                    id = target.getId();
                    break;
                default:
                    type = AudioSourceQueue.CHUNK;
                    id = AudioSourceQueue.NO_ID;
                    name = "";
                    break;
            }

            return AudioSourceQueue.create(type, id, name, audioSources);
        }

        return AudioSourceQueue.create(AudioSourceQueue.CHUNK, AudioSourceQueue.NO_ID, "", audioSources);
    }

}
