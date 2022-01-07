package com.frolo.music.repository;

import com.frolo.music.model.Album;
import com.frolo.music.model.Artist;
import com.frolo.music.model.Genre;
import com.frolo.music.model.Playlist;
import com.frolo.music.model.Song;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public interface SongRepository extends MediaRepository<Song> {
    /**
     * Queries song that has given path in the disk storage
     * @param path of the song
     * @return song
     */
    Single<Song> getSong(String path);

    Flowable<List<Song>> getSongsOptionally(List<Long> ids);

    Single<Song> update(Song song, String newTitle, String newAlbum, String newArtist, String newGenre);

    Flowable<List<Song>> getSongsFromAlbum(Album album, String sortOrder);
    Flowable<List<Song>> getSongsFromArtist(Artist artist, String sortOrder);
    Flowable<List<Song>> getSongsFromGenre(Genre genre, String sortOrder);
    Flowable<List<Song>> getSongsFromPlaylist(Playlist playlist, String sortOrder);

    // date added is a timestamp in SECONDS
    Flowable<List<Song>> getRecentlyAddedSongs(long dateAdded);

    Completable addSongPlayCount(Song song, int delta);
}
