package com.frolo.muse.repository;

import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.Song;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface MediaRepository<E extends Media> {

    // return Map(Sort name to Sort order)
    Single<Map<String, String>> getSortOrders();

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
     * @param filter to filter songs
     * @return songs with names containing filter
     */
    Flowable<List<E>> getFilteredItems(String filter);

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
    @Deprecated
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

    Completable addToPlaylist(long playlistId, E item);
    Completable addToPlaylist(long playlistId, Collection<E> items);


    /* ********************************************
     * ********************************************
     * ********************************************
     * ********************************************
     * ****************   SONGS   *****************
     * ********************************************
     * ********************************************
     * ********************************************
     * ***************************************** */
    Single<List<Song>> collectSongs(E item);
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
    Completable createShortcut(E item);

}
