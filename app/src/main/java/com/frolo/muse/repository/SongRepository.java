package com.frolo.muse.repository;

import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public interface SongRepository extends MediaRepository<Song> {
    /**
     * Queries song that has given path in the disk storage
     * @param path of the song
     * @return song
     */
    Single<Song> getSong(String path);

    Single<Song> update(Song song, String newTitle, String newAlbum, String newArtist, String newGenre);

    Flowable<List<Song>> getSongsFromAlbum(Album album, String sortOrder);
    Flowable<List<Song>> getSongsFromArtist(Artist artist, String sortOrder);
    Flowable<List<Song>> getSongsFromGenre(Genre genre, String sortOrder);
    Flowable<List<Song>> getSongsFromPlaylist(Playlist playlist, String sortOrder);

    // date added is a timestamp in SECONDS
    Flowable<List<Song>> getRecentlyAddedSongs(long dateAdded);
}
