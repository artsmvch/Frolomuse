package com.frolo.muse.repository;

import com.frolo.muse.engine.Player;
import com.frolo.muse.engine.SongQueue;
import com.frolo.muse.model.Library;
import com.frolo.muse.model.Recently;
import com.frolo.muse.model.Theme;
import com.frolo.muse.model.VisualizerRendererType;
import com.frolo.muse.model.media.Media;

import java.io.PrintWriter;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;


public interface Preferences {

    void dump(PrintWriter pw);

    boolean isAlbumGridEnabled();
    void setAlbumGridEnabled(boolean enabled);

    @Player.RepeatMode int loadRepeatMode();
    void saveRepeatMode(@Player.RepeatMode int mode);

    @Player.ShuffleMode int loadShuffleMode();
    void saveShuffleMode(@Player.ShuffleMode int mode);

    void saveTheme(Theme theme);
    Theme getTheme();

    void saveLastMediaCollectionType(@SongQueue.QueueType int type);
    void saveLastMediaCollectionId(long id);
    void saveLastSongId(long id);
    void saveLastPlaybackPosition(int position);

    @SongQueue.QueueType int getLastMediaCollectionType();
    long getLastMediaCollectionId();
    long getLastSongId();
    int getLastPlaybackPosition();

    boolean shouldResumeOnPluggedIn();
    void setResumeOnPluggedIn(boolean shouldResume);

    boolean shouldPauseOnUnplugged();
    void setPauseOnUnplugged(boolean shouldPause);

    int getOpenCount();
    void setOpenCount(int count);

    int getOpenCountToRate();
    void setOpenCountToRate(int count);

    boolean getRated();
    void setRated(boolean rated);

    @Deprecated
    String getSortOrder(@Media.Kind int kind);
    @Deprecated
    void saveSortOrder(@Media.Kind int kind, String sortOrder);
    @Deprecated
    boolean isSortOrderReversed(@Media.Kind int kind);
    @Deprecated
    void saveSortOrderReversed(@Media.Kind int kind, boolean reversed);

    Flowable<String> getSortOrderForSection(@Library.Section int section);
    Completable saveSortOrderForSection(@Library.Section int section, String sortOrder);
    Flowable<Boolean> isSortOrderReversedForSection(@Library.Section int section);
    Completable saveSortOrderReversedForSection(@Library.Section int section, boolean reversed);

    boolean isLibrarySectionEnabled(@Library.Section int section);
    void setLibrarySectionEnabled(@Library.Section int section, boolean enabled);

    /**
     * Returns library sections in certain order
     * @return library sections in certain order
     */
    List</*@Library.Section*/ Integer> getLibrarySections();
    void setLibrarySections(List</*@Library.Section*/ Integer> sections);

    @Recently.Period int getRecentlyAddedPeriod();
    void setRecentlyAddedPeriod(@Recently.Period int period);

    Flowable<VisualizerRendererType> getVisualizerRendererType();
    Completable setVisualizerRendererType(VisualizerRendererType type);

    /**
     * All audio files with duration less than this value are excluded from the library search.
     * NOTE: the duration is considered in seconds.
     */
    Flowable<Integer> getMinAudioFileDuration();
    Completable setMinAudioFileDuration(int minDuration);
}
