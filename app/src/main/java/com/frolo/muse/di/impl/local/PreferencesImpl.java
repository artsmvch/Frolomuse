package com.frolo.muse.di.impl.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.frolo.muse.BuildConfig;
import com.frolo.player.Player;
import com.frolo.muse.model.Library;
import com.frolo.muse.model.Recently;
import com.frolo.muse.model.Theme;
import com.frolo.muse.model.VisualizerRendererType;
import com.frolo.muse.model.playback.PlaybackFadingParams;
import com.frolo.music.model.Media;
import com.frolo.muse.repository.Preferences;
import com.frolo.rxpreference.RxOptional;
import com.frolo.rxpreference.RxPreference;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class PreferencesImpl implements Preferences {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String STORAGE_NAME = "mp_shared_preferences";

    // how many times the app was launched
    private static final String KEY_TOTAL_LAUNCH_COUNT = "open_count";
    // how many times should the app be opened to ask the user to rate it;
    // this value should be increased each time the button 'ask later' pressed
    @Deprecated
    private static final String KEY_OPEN_COUNT_TO_RATE = "open count to rate";
    // true if rated OR rating denied - false otherwise
    @Deprecated
    private static final String KEY_RATED = "rated";

    private static final String KEY_LAST_MEDIA_COLLECTION_TYPE = "last_media_collection_type";
    private static final String KEY_LAST_MEDIA_COLLECTION_ID = "last_media_collection_id";
    private static final String KEY_LAST_SONG_ID = "last_song_id";
    private static final String KEY_LAST_PLAYBACK_POSITION = "last_playback_position";
    private static final String KEY_LAST_MEDIA_COLLECTION_ITEM_IDS = "last_media_collection_item_ids";

    // player and playback
    private static final String KEY_PLAYBACK_REPEAT_MODE = "playback_repeat_mode";
    private static final String KEY_PLAYBACK_SHUFFLE_MODE = "playback_shuffle_mode";

    // headset and earphones
    private static final String KEY_PAUSE_ON_UNPLUGGED = "pause_on_unplugged";
    private static final String KEY_RESUME_ON_PLUGGED_IN = "resume_on_plugged_in";

    private static final String KEY_ALBUM_GRID_ENABLED = "album_big_item_displaying";
    private static final String KEY_LANGUAGE = "app_language_key";
    private static final String KEY_THEME = "app_theme";

    private static final String KEY_VISUALIZER_RENDERER_TYPE = "visualizer_renderer_type";

    @Deprecated
    private static final String KEY_MIN_AUDIO_FILE_DURATION = "min_audio_file_duration";

    // Playback Fading parameters
    private static final String KEY_PLAYBACK_FADING_PARAMS = "playback_fading_params";

    // Greetings
    private static final String KEY_GREETINGS_SHOWN = "greetings_show";

    private static final List<Integer> sDefaultLibrarySections;
    static {
        List<Integer> sections = new ArrayList<>(9);
        sections.add(Library.ALL_SONGS);
        sections.add(Library.ARTISTS);
        sections.add(Library.ALBUMS);
        sections.add(Library.GENRES);
        sections.add(Library.FAVOURITES);
        sections.add(Library.RECENTLY_ADDED);
        sections.add(Library.PLAYLISTS);
        sections.add(Library.FOLDERS);
        sections.add(Library.MOST_PLAYED);
        sDefaultLibrarySections = sections;
    }

    // This is very important. Must be determined by A / B testing.
    private static final Theme DEFAULT_THEME = Theme.DARK_PURPLE;

    // Sort orders
    @Deprecated
    private static final String KEY_SORT_ORDER = "sort_order";
    @Deprecated
    private String getKeySortOrder(@Media.Kind int kind) {
        return KEY_SORT_ORDER + kind;
    }

    // Sort orders reversion
    @Deprecated
    private static final String KEY_SORT_ORDER_REVERSED = "sort_order_reversed";
    @Deprecated
    private String getKeySortReversedOrder(@Media.Kind int kind) {
        return KEY_SORT_ORDER_REVERSED + kind;
    }

    // Sort orders for sections
    private static final String KEY_SORT_ORDER_FOR_SECTION = "sort_order_for_section";
    private String getKeySortOrderForSection(@Library.Section int section) {
        return KEY_SORT_ORDER_FOR_SECTION + section;
    }

    // Sort orders reversion for sections
    private static final String KEY_SORT_ORDER_REVERSED_FOR_SECTION = "sort_order_reversed_for_section";
    private String getKeySortReversedOrderForSection(@Library.Section int section) {
        return KEY_SORT_ORDER_REVERSED_FOR_SECTION + section;
    }

    // Library sections
    private static final String KEY_LIBRARY_SECTION_ENABLED = "library_section_enabled";
    private String getKeyLibrarySectionEnabled(@Library.Section int section) {
        return KEY_LIBRARY_SECTION_ENABLED + section;
    }

    private static final String KEY_LIBRARY_SECTIONS = "library_sections";

    // recently added period
    private static final String KEY_RECENTLY_ADDED_PERIOD = "recently_added_period";

    private final SharedPreferences preferences;

    public PreferencesImpl(Context context) {
        this.preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String toString() {
        StringBuilder print = new StringBuilder();
        Map<String, ?> preferences = this.preferences.getAll();
        Set<String> keys = preferences.keySet();
        for (String key : keys) {
            print.append(key).append("=").append(preferences.get(key)).append("\n");
        }
        return print.toString();
    }

    @Override
    public void dump(PrintWriter pw) {
        pw.print(toString());
    }

    public boolean isAlbumGridEnabled() {
        return preferences.getBoolean(KEY_ALBUM_GRID_ENABLED, true);
    }

    public void setAlbumGridEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_ALBUM_GRID_ENABLED, enabled).apply();
    }

    public @Player.RepeatMode int loadRepeatMode() {
        return preferences.getInt(KEY_PLAYBACK_REPEAT_MODE, Player.REPEAT_OFF);
    }

    public void saveRepeatMode(@Player.RepeatMode int mode) {
        preferences.edit().putInt(KEY_PLAYBACK_REPEAT_MODE, mode).apply();
    }

    public @Player.ShuffleMode int loadShuffleMode() {
        return preferences.getInt(KEY_PLAYBACK_SHUFFLE_MODE, Player.SHUFFLE_OFF);
    }

    public void saveShuffleMode(@Player.ShuffleMode int mode) {
        preferences.edit().putInt(KEY_PLAYBACK_SHUFFLE_MODE, mode).apply();
    }

    @Nullable
    @Override
    public String getLanguage() {
        try {
            return preferences.getString(KEY_LANGUAGE, null);
        } catch (Throwable err) {
            if (DEBUG) throw err;
            return null;
        }
    }

    @Override
    public void setLanguage(@Nullable String lang) {
        try {
            preferences.edit().putString(KEY_LANGUAGE, lang).apply();
        } catch (Throwable err) {
            if (DEBUG) throw err;
        }
    }

    @Override
    public void saveTheme(Theme theme) {
        try {
            if (theme != null) {
                preferences.edit().putInt(KEY_THEME, theme.getId()).apply();
            }
        } catch (Throwable err) {
            if (DEBUG) throw err;
        }
    }

    @Override
    public Theme getTheme() {
        try {
            final int themeId = preferences.getInt(KEY_THEME, DEFAULT_THEME.getId());
            if (!preferences.contains(KEY_THEME)) {
                // Save the first value to be the default
                preferences.edit().putInt(KEY_THEME, themeId).apply();
            }
            return Theme.findByIdOrDefault(themeId, DEFAULT_THEME);
        } catch (Throwable err) {
            if (DEBUG) throw err;
            return DEFAULT_THEME;
        }
    }

    @Deprecated
    @Override
    public void saveLastMediaCollectionType(int type) {
        preferences.edit().putInt(KEY_LAST_MEDIA_COLLECTION_TYPE, type).apply();
    }

    @Deprecated
    @Override
    public void saveLastMediaCollectionId(long id) {
        preferences.edit().putLong(KEY_LAST_MEDIA_COLLECTION_ID, id).apply();
    }

    @Override
    public void saveLastSongId(long id) {
        preferences.edit().putLong(KEY_LAST_SONG_ID, id).apply();
    }

    @Override
    public void saveLastPlaybackPosition(int position) {
        preferences.edit().putInt(KEY_LAST_PLAYBACK_POSITION, position).apply();
    }

    @Override
    public Completable saveLastMediaCollectionItemIds(final List<Long> ids) {
        Single<String> serializationSource =
                Single.fromCallable(() -> PreferencesSerialization.trySerializeItemIds(ids));
        return serializationSource.subscribeOn(Schedulers.computation())
                .flatMapCompletable(value -> RxPreference.ofString(preferences, KEY_LAST_MEDIA_COLLECTION_ITEM_IDS).set(value));
    }

    @Deprecated
    @Override
    public int getLastMediaCollectionType() {
        return preferences.getInt(KEY_LAST_MEDIA_COLLECTION_TYPE, /* deprecated */ 6);
    }

    @Deprecated
    @Override
    public long getLastMediaCollectionId() {
        return preferences.getLong(KEY_LAST_MEDIA_COLLECTION_ID, /* no id */ - 1);
    }

    @Override
    public long getLastSongId() {
        return preferences.getLong(KEY_LAST_SONG_ID, -1);
    }

    @Override
    public int getLastPlaybackPosition() {
        return preferences.getInt(KEY_LAST_PLAYBACK_POSITION, 0);
    }

    @Override
    public Flowable<List<Long>> getLastMediaCollectionItemIds() {
        return RxPreference.ofString(preferences, KEY_LAST_MEDIA_COLLECTION_ITEM_IDS)
            .get()
            .observeOn(Schedulers.computation())
            .map(optional -> {
                if (optional.isPresent()) {
                    return PreferencesSerialization.tryDeserializeItemIds(optional.get());
                } else {
                    return Collections.emptyList();
                }
            });
    }

    public boolean shouldResumeOnPluggedIn() {
        return preferences.getBoolean(KEY_RESUME_ON_PLUGGED_IN, false);
    }

    public void setResumeOnPluggedIn(boolean shouldResume) {
        preferences.edit().putBoolean(KEY_RESUME_ON_PLUGGED_IN, shouldResume).apply();
    }

    public boolean shouldPauseOnUnplugged() {
        return preferences.getBoolean(KEY_PAUSE_ON_UNPLUGGED, true);
    }

    public void setPauseOnUnplugged(boolean shouldPause) {
        preferences.edit().putBoolean(KEY_PAUSE_ON_UNPLUGGED, shouldPause).apply();
    }

    public int getLaunchCount() {
        return preferences.getInt(KEY_TOTAL_LAUNCH_COUNT, 0);
    }

    public void setLaunchCount(int count) {
        preferences.edit().putInt(KEY_TOTAL_LAUNCH_COUNT, count).apply();
    }

    @Deprecated
    public int getMinLaunchCountForRatingRequest() {
        return preferences.getInt(KEY_OPEN_COUNT_TO_RATE, 5);
    }

    @Deprecated
    public void setMinLaunchCountForRatingRequest(int count) {
        preferences.edit().putInt(KEY_OPEN_COUNT_TO_RATE, count).apply();
    }

    @Deprecated
    public boolean getRated() {
        return preferences.getBoolean(KEY_RATED, false);
    }

    @Deprecated
    public void setRated(boolean rated) {
        preferences.edit().putBoolean(KEY_RATED, rated).apply();
    }

    @Deprecated
    @Override
    public String getSortOrder(int kind) {
        String order = preferences.getString(getKeySortOrder(kind), null);
        switch (kind) {
            case Media.ALBUM: {
                if (isNullOrNotMatchingAnyOthers(order,
                        AlbumQuery.Sort.BY_ALBUM,
                        AlbumQuery.Sort.BY_NUMBER_OF_SONGS))
                    return AlbumQuery.Sort.BY_ALBUM;
                else return order;
            }
            case Media.ARTIST: {
                if (isNullOrNotMatchingAnyOthers(order,
                        ArtistQuery.Sort.BY_ARTIST,
                        ArtistQuery.Sort.BY_NUMBER_OF_ALBUMS,
                        ArtistQuery.Sort.BY_NUMBER_OF_TRACKS))
                    return ArtistQuery.Sort.BY_ARTIST;
                else return order;
            }
            case Media.GENRE: {
                if (isNullOrNotMatchingAnyOthers(order,
                        GenreQuery.Sort.BY_NAME))
                    return GenreQuery.Sort.BY_NAME;
                else return order;
            }
            case Media.PLAYLIST: {
                if (isNullOrNotMatchingAnyOthers(order,
                        PlaylistQuery.Sort.BY_NAME,
                        PlaylistQuery.Sort.BY_DATE_ADDED,
                        PlaylistQuery.Sort.BY_DATE_MODIFIED))
                    return PlaylistQuery.Sort.BY_NAME;
                else return order;
            }
            case Media.SONG: {
                if (isNullOrNotMatchingAnyOthers(order,
                        SongQuery.Sort.BY_TITLE,
                        SongQuery.Sort.BY_ALBUM,
                        SongQuery.Sort.BY_ARTIST))
                    return SongQuery.Sort.BY_TITLE;
                else return order;
            }
            case Media.MY_FILE: {
                if (isNullOrNotMatchingAnyOthers(order,
                        MyFileQuery.Sort.BY_FILENAME))
                    return MyFileQuery.Sort.BY_FILENAME;
                else return order;
            }
            default: throw new IllegalArgumentException("Unknown kind of media: " + kind);
        }
    }

    private boolean isNullOrNotMatchingAnyOthers(String candidate, String... others) {
        if (candidate == null) {
            return true;
        } else {
            for (String other : others) {
                if (candidate.equals(other)) return false;
            }
            return true;
        }
    }

    @Deprecated
    @Override
    public void saveSortOrder(int kind, String sortOrder) {
        preferences.edit().putString(getKeySortOrder(kind), sortOrder).apply();
    }

    @Deprecated
    @Override
    public boolean isSortOrderReversed(int kind) {
        return preferences.getBoolean(getKeySortReversedOrder(kind), false);
    }

    @Deprecated
    @Override
    public void saveSortOrderReversed(int kind, boolean reversed) {
        preferences.edit().putBoolean(getKeySortReversedOrder(kind), reversed).apply();
    }

    @Override
    public Flowable<String> getSortOrderForSection(final @Library.Section int section) {
        final String key = getKeySortOrderForSection(section);
        return RxPreference.ofString(preferences, key).get().map(optional -> {
            final String order = optional.orElse(null);
            switch (section) {
                case Library.ALBUMS:
                    return AlbumRepositoryImpl.getSortOrderOrDefault(order);

                case Library.ARTISTS:
                    return ArtistRepositoryImpl.getSortOrderOrDefault(order);

                case Library.GENRES:
                    return GenreRepositoryImpl.getSortOrderOrDefault(order);

                case Library.PLAYLISTS:
                    return PlaylistRepositoryImpl.getSortOrderOrDefault(order);

                case Library.FAVOURITES:
                case Library.RECENTLY_ADDED:
                case Library.ALL_SONGS:
                    return SongRepositoryImpl.getSortOrderOrDefault(order);

                case Library.ALBUM:
                    return AlbumChunkRepositoryImpl.getSortOrderOrDefault(order);

                case Library.ARTIST:
                    return ArtistChunkRepositoryImpl.getSortOrderOrDefault(order);

                case Library.GENRE:
                    return GenreChunkRepositoryImpl.getSortOrderOrDefault(order);

                case Library.PLAYLIST:
                    return PlaylistChunkRepositoryImpl.getSortOrderOrDefault(order);

                case Library.FOLDERS: {
                    return MyFileRepositoryImpl.getSortOrderOrDefault(order);
                }

                case Library.MIXED:
                case Library.MOST_PLAYED:
                default: {
                    throw new IllegalArgumentException("Unsupported library section: " + section);
                }
            }
        });
    }

    @Override
    public Completable saveSortOrderForSection(
            final @Library.Section int section,
            final String sortOrder
    ) {
        return Completable.fromAction(
                new Action() {
                    @Override
                    public void run() {
                        final String key = getKeySortOrderForSection(section);
                        preferences.edit()
                                .putString(key, sortOrder)
                                .apply();
                    }
                }
        );
    }

    @Override
    public Flowable<Boolean> isSortOrderReversedForSection(@Library.Section int section) {
        final String key = getKeySortReversedOrderForSection(section);
        return RxPreference.ofBoolean(preferences, key).get(false);
    }

    @Override
    public Completable saveSortOrderReversedForSection(
            final @Library.Section int section,
            final boolean reversed
    ) {
        return Completable.fromAction(
                new Action() {
                    @Override
                    public void run() {
                        final String key = getKeySortReversedOrderForSection(section);
                        preferences.edit()
                                .putBoolean(key, reversed)
                                .apply();
                    }
                }
        );
    }

    @Override
    public boolean isLibrarySectionEnabled(@Library.Section int section) {
        return preferences.getBoolean(getKeyLibrarySectionEnabled(section), true);
    }

    @Override
    public void setLibrarySectionEnabled(@Library.Section int section, boolean enabled) {
        preferences.edit().putBoolean(getKeyLibrarySectionEnabled(section), enabled).apply();
    }

    private List<Integer> getDefaultLibrarySections() {
        return new ArrayList<>(sDefaultLibrarySections);
    }

    @Override
    public List<Integer> getLibrarySections() {
        String stringValue = preferences.getString(KEY_LIBRARY_SECTIONS, null);
        if (stringValue == null || stringValue.isEmpty()) {
            return getDefaultLibrarySections();
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(stringValue, ",");
            List<Integer> sections = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                String nextToken = tokenizer.nextToken();
                Integer section = Integer.valueOf(nextToken);
                sections.add(section);
            }

            // checking that no sections are lost
            for (Integer s : sDefaultLibrarySections) {
                if (!sections.contains(s)) sections.add(s);
            }

            return sections;
        } catch (Exception e) { // if any exception occurs
            return getDefaultLibrarySections();
        }
    }

    @Override
    public void setLibrarySections(List<Integer> sections) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sections.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(sections.get(i));
        }
        preferences.edit().putString(KEY_LIBRARY_SECTIONS, sb.toString()).apply();
    }

    @Override
    public @Recently.Period int getRecentlyAddedPeriod() {
        return preferences.getInt(KEY_RECENTLY_ADDED_PERIOD, Recently.FOR_LAST_WEEK);
    }

    @Override
    public void setRecentlyAddedPeriod(@Recently.Period int period) {
        preferences.edit().putInt(KEY_RECENTLY_ADDED_PERIOD, period).apply();
    }

    @Override
    public Flowable<VisualizerRendererType> getVisualizerRendererType() {
        return RxPreference.ofInt(preferences, KEY_VISUALIZER_RENDERER_TYPE)
            .get()
            .map(new Function<RxOptional<Integer>, VisualizerRendererType>() {
                @Override
                public VisualizerRendererType apply(RxOptional<Integer> optional) {
                    VisualizerRendererType mapped = null;
                    if (optional.isPresent()) {
                        mapped = mapIntToVisualizerRendererType(optional.get());
                    }
                    return mapped != null ? mapped : VisualizerRendererType.LINE_SPECTRUM;
                }
            });
    }

    @Override
    public Completable setVisualizerRendererType(final VisualizerRendererType type) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                final int intValue = mapVisualizerRendererTypeToInt(type);
                preferences.edit().putInt(KEY_VISUALIZER_RENDERER_TYPE, intValue).apply();
            }
        });
    }

    private static int mapVisualizerRendererTypeToInt(VisualizerRendererType type) {
        if (type == null) return -1;

        switch (type) {
            case CIRCLE:            return 0;
            case CIRCLE_SPECTRUM:   return 1;
            case LINE:              return 2;
            case LINE_SPECTRUM:     return 3;
            case SPECTRUM:          return 4;
            default:                return 3;
        }
    }

    @Nullable
    private static VisualizerRendererType mapIntToVisualizerRendererType(Integer value) {
        if (value == null) return null;

        switch (value) {
            case 0:     return VisualizerRendererType.CIRCLE;
            case 1:     return VisualizerRendererType.CIRCLE_SPECTRUM;
            case 2:     return VisualizerRendererType.LINE;
            case 3:     return VisualizerRendererType.LINE_SPECTRUM;
            case 4:     return VisualizerRendererType.SPECTRUM;
            default:    return null;
        }
    }

    @Override
    public Flowable<PlaybackFadingParams> getPlaybackFadingParams() {
        return RxPreference.ofString(preferences, KEY_PLAYBACK_FADING_PARAMS)
                .get()
                .map(new Function<RxOptional<String>, PlaybackFadingParams>() {
                    @Override
                    public PlaybackFadingParams apply(RxOptional<String> optional) throws Exception {
                        final String value = optional.isPresent() ? optional.get() : null;
                        final PlaybackFadingParams deserialized =
                                PreferencesSerialization.tryDeserializePlaybackFadingParams(value);
                        return deserialized != null ? deserialized : PlaybackFadingParams.none();
                    }
                });
    }

    @Override
    public Completable setPlaybackFadingParams(PlaybackFadingParams params) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                final String value = PreferencesSerialization.trySerializePlaybackFadingParams(params);
                preferences.edit().putString(KEY_PLAYBACK_FADING_PARAMS, value).apply();
            }
        });
    }

    @Override
    public Flowable<Boolean> shouldShowGreetings() {
        if (getLaunchCount() > 0) {
            // the app was opened before
            return Flowable.just(false);
        }
        return RxPreference.ofBoolean(preferences, KEY_GREETINGS_SHOWN)
            .get(false)
            .map(new Function<Boolean, Boolean>() {
                @Override
                public Boolean apply(Boolean value) throws Exception {
                    return !value;
                }
            });
    }

    @Override
    public Completable markGreetingsShown() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                preferences.edit().putBoolean(KEY_GREETINGS_SHOWN, true).apply();
            }
        });
    }
}
