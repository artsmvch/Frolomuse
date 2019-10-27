package com.frolo.muse.repository;

import com.frolo.muse.model.media.MyFile;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public interface MyFileRepository extends MediaRepository<MyFile> {
    /**
     * Depends on the environment so we need to split it from implementation;
     * @return the root file of the FileSystem
     */
    Single<MyFile> getRootFile();

    /**
     * This method searches {@code parent} only for the files that are songs or are folders that contain songs;
     * Should be implemented in the most fast way and wrapped into {@link Flowable}
     * Observing is happening in a worker thread;
     * @param parent from what the method starts the search
     * @return only songs and folders that have songs inside themselves
     */
    Flowable<List<MyFile>> browse(MyFile parent);
}
