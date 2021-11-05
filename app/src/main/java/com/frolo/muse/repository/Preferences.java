package com.frolo.muse.repository;

import com.frolo.muse.engine.Player;
import com.frolo.muse.engine.AudioSourceQueue;
import com.frolo.muse.model.Library;
import com.frolo.muse.model.Recently;
import com.frolo.muse.model.Theme;
import com.frolo.muse.model.VisualizerRendererType;
import com.frolo.muse.model.playback.PlaybackFadingParams;
import com.frolo.muse.model.media.Media;

import org.jetbrains.annotations.Nullable;

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

    @Nullable
    String getLanguage();
    void setLanguage(@Nullable String lang);

    void saveTheme(Theme theme);
    Theme getTheme();

    @Deprecated
    void saveLastMediaCollectionType(int type);
    @Deprecated
    void saveLastMediaCollectionId(long id);
    void saveLastSongId(long id);
    void saveLastPlaybackPosition(int position);
    Completable saveLastMediaCollectionItemIds(List<Long> ids);

    @Deprecated
    int getLastMediaCollectionType();
    @Deprecated
    long getLastMediaCollectionId();
    long getLastSongId();
    int getLastPlaybackPosition();
    Flowable<List<Long>> getLastMediaCollectionItemIds();

    boolean shouldResumeOnPluggedIn();
    void setResumeOnPluggedIn(boolean shouldResume);

    boolean shouldPauseOnUnplugged();
    void setPauseOnUnplugged(boolean shouldPause);

    int getLaunchCount();
    void setLaunchCount(int count);

    int getMinLaunchCountForRatingRequest();
    void setMinLaunchCountForRatingRequest(int count);

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

    Flowable<PlaybackFadingParams> getPlaybackFadingParams();
    Completable setPlaybackFadingParams(PlaybackFadingParams params);

    Flowable<Boolean> shouldShowGreetings();
    Completable markGreetingsShown();
}
