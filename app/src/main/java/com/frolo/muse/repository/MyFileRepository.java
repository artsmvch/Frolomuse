package com.frolo.muse.repository;

import com.frolo.music.model.MyFile;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Deprecated
public interface MyFileRepository extends MediaRepository<MyFile> {
    /**
     * Depends on the environment so we need to split it from implementation;
     * @return the root file of the FileSystem
     */
    Single<MyFile> getRootFile();

    Single<MyFile> getDefaultFolder();

    Completable setDefaultFolder(MyFile folder);

    /**
     * This method searches {@code parent} only for the files that are songs or are folders that contain songs;
     * Should be implemented in the most fast way and wrapped into {@link Flowable}
     * Observing is happening in a worker thread;
     * @param parent from what the method starts the search
     * @param sortOrderKey to sort the list
     * @return only songs and folders that have songs inside themselves
     */
    Flowable<List<MyFile>> browse(MyFile parent, String sortOrderKey);

    Flowable<List<MyFile>> getHiddenFiles();

    Completable setFileHidden(MyFile item, boolean hidden);
}
