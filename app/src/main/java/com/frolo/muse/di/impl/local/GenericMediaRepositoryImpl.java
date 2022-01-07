package com.frolo.muse.di.impl.local;

import android.annotation.SuppressLint;
import android.content.Context;

import com.frolo.music.model.Album;
import com.frolo.music.model.Artist;
import com.frolo.music.model.Genre;
import com.frolo.music.model.Media;
import com.frolo.music.model.MediaFile;
import com.frolo.music.model.MyFile;
import com.frolo.music.model.Playlist;
import com.frolo.music.model.Song;
import com.frolo.music.model.UnknownMediaException;
import com.frolo.music.model.SortOrder;
import com.frolo.music.repository.AlbumRepository;
import com.frolo.music.repository.ArtistRepository;
import com.frolo.music.repository.GenericMediaRepository;
import com.frolo.music.repository.GenreRepository;
import com.frolo.music.repository.MediaFileRepository;
import com.frolo.music.repository.MyFileRepository;
import com.frolo.music.repository.PlaylistRepository;
import com.frolo.music.repository.SongRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function5;


public class GenericMediaRepositoryImpl implements GenericMediaRepository {

    private final Context mContext;
    private final SongRepository mSongRepo;
    private final ArtistRepository mArtistRepo;
    private final AlbumRepository mAlbumRepo;
    private final GenreRepository mGenreRepo;
    private final PlaylistRepository mPlaylistRepo;
    private final MyFileRepository mMyFileRepo;
    private final MediaFileRepository mMediaFileRepo;

    public GenericMediaRepositoryImpl(
        Context context,
        SongRepository songRepo,
        ArtistRepository artistRepo,
        AlbumRepository albumRepo,
        GenreRepository genreRepo,
        PlaylistRepository playlistRepo,
        MyFileRepository myFileRepo,
        MediaFileRepository mediaFileRepo
    ) {

        this.mContext = context;
        this.mSongRepo = songRepo;
        this.mArtistRepo = artistRepo;
        this.mAlbumRepo = albumRepo;
        this.mGenreRepo = genreRepo;
        this.mPlaylistRepo = playlistRepo;
        this.mMyFileRepo = myFileRepo;
        this.mMediaFileRepo = mediaFileRepo;
    }

    private Function5<List<Song>, List<Album>, List<Artist>, List<Genre>, List<Playlist>, List<Media>> createCombiner() {
        return new Function5<List<Song>, List<Album>, List<Artist>, List<Genre>, List<Playlist>, List<Media>>() {
            @Override
            public List<Media> apply(
                    List<Song> songs,
                    List<Album> albums,
                    List<Artist> artists,
                    List<Genre> genres,
                    List<Playlist> playlists
            ) {
                int totalSize = songs.size() + albums.size() + artists.size() + genres.size() + playlists.size();
                List<Media> items = new ArrayList<>(totalSize);
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
    public Single<List<SortOrder>> getSortOrders() {
        return Single.just(Collections.emptyList());
    }

    @Override
    public Flowable<List<Media>> getAllItems() {
        return Flowable.combineLatest(
                mSongRepo.getAllItems(),
                mAlbumRepo.getAllItems(),
                mArtistRepo.getAllItems(),
                mGenreRepo.getAllItems(),
                mPlaylistRepo.getAllItems(),
                createCombiner());
    }

    @Override
    public Flowable<List<Media>> getAllItems(String sortOrder) {
        return Flowable.combineLatest(
                mSongRepo.getAllItems(sortOrder),
                mAlbumRepo.getAllItems(sortOrder),
                mArtistRepo.getAllItems(sortOrder),
                mGenreRepo.getAllItems(sortOrder),
                mPlaylistRepo.getAllItems(sortOrder),
                createCombiner());
    }

    @Override
    public Flowable<List<Media>> getFilteredItems(String namePiece) {
        return Flowable.combineLatest(
                mSongRepo.getFilteredItems(namePiece),
                mAlbumRepo.getFilteredItems(namePiece),
                mArtistRepo.getFilteredItems(namePiece),
                mGenreRepo.getFilteredItems(namePiece),
                mPlaylistRepo.getFilteredItems(namePiece),
                createCombiner());
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

            case Media.MEDIA_FILE:
                return mMediaFileRepo.delete((MediaFile) item);

            case Media.MY_FILE:
            default:
                return Completable.error(new UnknownMediaException(item));
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
    public Completable addToPlaylist(Playlist playlist, Media item) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addItemToPlaylist(
                    mContext.getContentResolver(),
                    playlist.getId(),
                    item);
        } else {
            // New playlist storage
            return collectSongs(item).flatMapCompletable(songs -> PlaylistDatabaseManager.get(mContext)
                    .addPlaylistMembers(playlist.getId(), songs));
        }
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Collection<Media> items) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addItemsToPlaylist(
                    mContext.getContentResolver(),
                    playlist.getId(),
                    items);
        } else {
            // New playlist storage
            return collectSongs(items).flatMapCompletable(songs -> PlaylistDatabaseManager.get(mContext)
                    .addPlaylistMembers(playlist.getId(), songs));
        }
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

            case Media.MEDIA_FILE: {
                return mMediaFileRepo.collectSongs((MediaFile) item);
            }

            default: {
                return Single.error(new UnknownMediaException(item));
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
        return Flowable.combineLatest(
                mSongRepo.getAllFavouriteItems(),
                mAlbumRepo.getAllFavouriteItems(),
                mArtistRepo.getAllFavouriteItems(),
                mGenreRepo.getAllFavouriteItems(),
                mPlaylistRepo.getAllFavouriteItems(),
                createCombiner());
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

            default: {
                return Flowable.error(new UnknownMediaException(item));
            }
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

            default: {
                return Completable.error(new UnknownMediaException(item));
            }
        }
    }

    @Override
    public Single<Boolean> isShortcutSupported(Media item) {
        return Single.defer(new Callable<SingleSource<Boolean>>() {
            @Override
            public SingleSource<Boolean> call() throws Exception {
                switch (item.getKind()) {
                    case Media.SONG: {
                        return mSongRepo.isShortcutSupported((Song) item);
                    }

                    case Media.ALBUM: {
                        return mAlbumRepo.isShortcutSupported((Album) item);
                    }

                    case Media.ARTIST: {
                        return mArtistRepo.isShortcutSupported((Artist) item);
                    }

                    case Media.GENRE: {
                        return mGenreRepo.isShortcutSupported((Genre) item);
                    }

                    case Media.PLAYLIST: {
                        return mPlaylistRepo.isShortcutSupported((Playlist) item);
                    }

                    case Media.MY_FILE: {
                        return mMyFileRepo.isShortcutSupported((MyFile) item);
                    }

                    case Media.MEDIA_FILE: {
                        return mMediaFileRepo.isShortcutSupported((MediaFile) item);
                    }

                    default: {
                        return Single.error(new UnknownMediaException(item));
                    }
                }
            }
        });
    }

    @Override
    public Completable createShortcut(Media item) {
        return Completable.defer(new Callable<CompletableSource>() {
            @Override
            public CompletableSource call() throws Exception {
                switch (item.getKind()) {
                    case Media.SONG: {
                        return mSongRepo.createShortcut((Song) item);
                    }

                    case Media.ALBUM: {
                        return mAlbumRepo.createShortcut((Album) item);
                    }

                    case Media.ARTIST: {
                        return mArtistRepo.createShortcut((Artist) item);
                    }

                    case Media.GENRE: {
                        return mGenreRepo.createShortcut((Genre) item);
                    }

                    case Media.PLAYLIST: {
                        return mPlaylistRepo.createShortcut((Playlist) item);
                    }

                    case Media.MY_FILE: {
                        return mMyFileRepo.createShortcut((MyFile) item);
                    }

                    case Media.MEDIA_FILE: {
                        return mMediaFileRepo.createShortcut((MediaFile) item);
                    }

                    default: {
                        return Completable.error(new UnknownMediaException(item));
                    }
                }
            }
        });
    }

}
