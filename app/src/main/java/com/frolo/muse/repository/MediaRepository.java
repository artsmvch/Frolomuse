package com.frolo.muse.repository;

import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface MediaRepository<E extends Media> {

    /**
     * Returns the available sort orders for this repository.
     * The priority of the sort orders goes down in the list.
     * The default sort order is the first one in the list.
     * @return available sort orders for this repository
     */
    Single<List<SortOrder>> getSortOrders();

    /* ********************************************
     * ********************************************
     * ********************************************
     * ********************************************
     * *************   QUERYING   *****************
     * ********************************************
     * ********************************************
     * ********************************************
     * ***************************************** */

    /**
     * Queries all songs on the disk storage
     * @return all songs on the disk storage
     */
    Flowable<List<E>> getAllItems();

    /**
     * Queries all songs on the disk storage
     * @param sortOrder to sort on query level
     * @return all songs on the disk storage sorted by param
     */
    Flowable<List<E>> getAllItems(String sortOrder);

    /**
     * Queries all songs filtering them with the given filter
     * @param namePiece to filter songs
     * @return songs with names containing filter
     */
    Flowable<List<E>> getFilteredItems(String namePiece);

    /**
     * Querying {@link E} that has the given id.
     * @param id identifier of the {@link E}
     * @return {@link E} with the given id.
     */
    Flowable<E> getItem(long id);

    /* ********************************************
     * ********************************************
     * ********************************************
     * ********************************************
     * *************   DELETION   *****************
     * ********************************************
     * ********************************************
     * ********************************************
     * ***************************************** */

    /**
     * Deletes the given item from the storage
     * @param item to delete
     * @return true if deletion succeeded, false - otherwise
     */
    Completable delete(E item);

    /**
     * Deletes the given items from the storage
     * @param items to delete
     * @return true if deletion succeeded, false - otherwise
     */
    Completable delete(Collection<E> items);

    /* ********************************************
     * ********************************************
     * ********************************************
     * ********************************************
     * *********   IN PLAYLIST_CHUNK   ************
     * ********************************************
     * ********************************************
     * ********************************************
     * ***************************************** */

    Completable addToPlaylist(Playlist playlist, E item);
    Completable addToPlaylist(Playlist playlist, Collection<E> items);


    /* ********************************************
     * ********************************************
     * ********************************************
     * ********************************************
     * ****************   SONGS   *****************
     * ********************************************
     * ********************************************
     * ********************************************
     * ***************************************** */

    // NOTE: Be careful if you want to change it to observable/flowable.
    Single<List<Song>> collectSongs(E item);
    // NOTE: Be careful if you want to change it to observable/flowable.
    Single<List<Song>> collectSongs(Collection<E> items);

    /* ********************************************
     * ********************************************
     * ********************************************
     * ********************************************
     * *************   FAVOURITES   ***************
     * ********************************************
     * ********************************************
     * ********************************************
     * ***************************************** */

    Flowable<List<E>> getAllFavouriteItems();
    Flowable<Boolean> isFavourite(E item);
    Completable changeFavourite(E item);

    /* ********************************************
     * ********************************************
     * ********************************************
     * ********************************************
     * **************   SHORTCUTS   ***************
     * ********************************************
     * ********************************************
     * ********************************************
     * ***************************************** */

    Single<Boolean> isShortcutSupported(E item);

    Completable createShortcut(E item);

}
