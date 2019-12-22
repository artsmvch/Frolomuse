package com.frolo.muse.di.impl.local;

import android.annotation.SuppressLint;
import android.content.Context;

import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.media.UnknownMediaException;
import com.frolo.muse.repository.AlbumRepository;
import com.frolo.muse.repository.ArtistRepository;
import com.frolo.muse.repository.GenericMediaRepository;
import com.frolo.muse.repository.GenreRepository;
import com.frolo.muse.repository.MyFileRepository;
import com.frolo.muse.repository.PlaylistRepository;
import com.frolo.muse.repository.SongRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function5;


public class GenericMediaRepositoryImpl implements GenericMediaRepository {

    private Context mContext;
    private SongRepository mSongRepo;
    private ArtistRepository mArtistRepo;
    private AlbumRepository mAlbumRepo;
    private GenreRepository mGenreRepo;
    private PlaylistRepository mPlaylistRepo;
    private MyFileRepository mMyFileRepo;

    private final Map<String, String> mSortOrders =
            new HashMap<>(0);

    public GenericMediaRepositoryImpl(
            Context context,
            SongRepository songRepo,
            ArtistRepository artistRepo,
            AlbumRepository albumRepo,
            GenreRepository genreRepo,
            PlaylistRepository playlistRepo,
            MyFileRepository myFileRepo) {

        this.mContext = context;
        this.mSongRepo = songRepo;
        this.mArtistRepo = artistRepo;
        this.mAlbumRepo = albumRepo;
        this.mGenreRepo = genreRepo;
        this.mPlaylistRepo = playlistRepo;
        this.mMyFileRepo = myFileRepo;
    }

    private Function5<List<Song>, List<Album>, List<Artist>, List<Genre>, List<Playlist>, List<Media>> createZipper() {
        return new Function5<List<Song>, List<Album>, List<Artist>, List<Genre>, List<Playlist>, List<Media>>() {
            @Override
            public List<Media> apply(List<Song> songs,
                                     List<Album> albums,
                                     List<Artist> artists,
                                     List<Genre> genres,
                                     List<Playlist> playlists) {
                List<Media> items = new ArrayList<>();
                items.addAll(songs);
                items.addAll(albums);
                items.addAll(artists);
                items.addAll(genres);
                items.addAll(playlists);
                return items;
            }
        };
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }

    @Override
    public Flowable<List<Media>> getAllItems() {
        return Flowable.combineLatest(
                mSongRepo.getAllItems(),
                mAlbumRepo.getAllItems(),
                mArtistRepo.getAllItems(),
                mGenreRepo.getAllItems(),
                mPlaylistRepo.getAllItems(),
                createZipper());
    }

    @Override
    public Flowable<List<Media>> getAllItems(String sortOrder) {
        return Flowable.combineLatest(
                mSongRepo.getAllItems(sortOrder),
                mAlbumRepo.getAllItems(sortOrder),
                mArtistRepo.getAllItems(sortOrder),
                mGenreRepo.getAllItems(sortOrder),
                mPlaylistRepo.getAllItems(sortOrder),
                createZipper());
    }

    @Override
    public Flowable<List<Media>> getFilteredItems(String filter) {
        return Flowable.combineLatest(
                mSongRepo.getFilteredItems(filter),
                mAlbumRepo.getFilteredItems(filter),
                mArtistRepo.getFilteredItems(filter),
                mGenreRepo.getFilteredItems(filter),
                mPlaylistRepo.getFilteredItems(filter),
                createZipper());
    }

    @Override
    public Flowable<Media> getItem(long id) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable delete(Media item) {
        switch (item.getKind()) {
            case Media.SONG:
                return mSongRepo.delete((Song) item);

            case Media.ALBUM:
                return mAlbumRepo.delete((Album) item);

            case Media.ARTIST:
                return mArtistRepo.delete((Artist) item);

            case Media.GENRE:
                return mGenreRepo.delete((Genre) item);

            case Media.PLAYLIST:
                return mPlaylistRepo.delete((Playlist) item);

            case Media.MY_FILE:
            default:
                throw new UnknownMediaException(item);
        }
    }

    @Override
    public Completable delete(Collection<Media> items) {
        List<Completable> sources = new ArrayList<>(items.size());
        for (Media item : items) {
            sources.add(delete(item));
        }
        return Completable.merge(sources);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Media item) {
        return PlaylistHelper.addItemToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                item);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Media> items) {
        return PlaylistHelper.addItemsToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                items);
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public Single<List<Song>> collectSongs(Media item) {

        switch (item.getKind()) {
            case Media.SONG: {
                return mSongRepo.collectSongs((Song) item);
            }

            case Media.ALBUM: {
                return mAlbumRepo.collectSongs((Album) item);
            }

            case Media.ARTIST: {
                return mArtistRepo.collectSongs((Artist) item);
            }

            case Media.GENRE: {
                return mGenreRepo.collectSongs((Genre) item);
            }

            case Media.PLAYLIST: {
                return mPlaylistRepo.collectSongs((Playlist) item);
            }

            case Media.MY_FILE: {
                return mMyFileRepo.collectSongs((MyFile) item);
            }

            default: {
                return Single.error(
                        new UnknownMediaException(item));
            }
        }
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<Media> items) {
        return Observable.fromIterable(items)
                .concatMapSingle(new Function<Media, SingleSource<List<Song>>>() {
                    @Override public SingleSource<List<Song>> apply(Media media) {
                        return collectSongs(media);
                    }
                })
                .flatMapIterable(new Function<List<Song>, Iterable<Song>>() {
                    @Override public Iterable<Song> apply(List<Song> songs) {
                        return songs;
                    }
                })
                .toList();
    }

    @Override
    public Flowable<List<Media>> getAllFavouriteItems() {
        return Flowable.zip(
                mSongRepo.getAllFavouriteItems(),
                mArtistRepo.getAllFavouriteItems(),
                mAlbumRepo.getAllFavouriteItems(),
                mGenreRepo.getAllFavouriteItems(),
                mPlaylistRepo.getAllFavouriteItems(),
                new Function5<List<Song>, List<Artist>, List<Album>, List<Genre>, List<Playlist>, List<Media>>() {
                    @Override
                    public List<Media> apply(List<Song> songs, List<Artist> artists, List<Album> albums, List<Genre> genres, List<Playlist> playlists) throws Exception {
                        List<Media> list = new ArrayList<>();
                        list.addAll(songs);
                        list.addAll(artists);
                        list.addAll(artists);
                        list.addAll(genres);
                        list.addAll(playlists);
                        return list;
                    }
                });
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public Flowable<Boolean> isFavourite(Media item) {
        switch (item.getKind()) {
            case Media.SONG: {
                return mSongRepo.isFavourite((Song) item);
            }

            case Media.ALBUM: {
                return mAlbumRepo.isFavourite((Album) item);
            }

            case Media.ARTIST: {
                return mArtistRepo.isFavourite((Artist) item);
            }

            case Media.GENRE: {
                return mGenreRepo.isFavourite((Genre) item);
            }

            case Media.PLAYLIST: {
                return mPlaylistRepo.isFavourite((Playlist) item);
            }

            default: return Flowable.error(
                    new UnknownMediaException(item)
            );
        }
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public Completable changeFavourite(Media item) {
        switch (item.getKind()) {
            case Media.SONG: {
                return mSongRepo.changeFavourite((Song) item);
            }

            case Media.ALBUM: {
                return mAlbumRepo.changeFavourite((Album) item);
            }

            case Media.ARTIST: {
                return mArtistRepo.changeFavourite((Artist) item);
            }

            case Media.GENRE: {
                return mGenreRepo.changeFavourite((Genre) item);
            }

            case Media.PLAYLIST: {
                return mPlaylistRepo.changeFavourite((Playlist) item);
            }

            default: return Completable.error(
                    new UnknownMediaException(item)
            );
        }
    }
}
