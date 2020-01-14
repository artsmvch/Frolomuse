package com.frolo.muse.di.impl.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.frolo.muse.engine.Player;
import com.frolo.muse.engine.SongQueue;
import com.frolo.muse.model.Library;
import com.frolo.muse.model.Recently;
import com.frolo.muse.model.media.Media;
import com.frolo.muse.repository.Preferences;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Action;


public class PreferencesImpl implements Preferences {

    private static final String STORAGE_NAME = "mp_shared_preferences";

    // how many times the app is opened
    private static final String KEY_TOTAL_OPEN_COUNT = "open_count";
    // how many times should the app be opened to ask the user to rate it;
    // this value should be increased each time the button 'ask later' pressed
    private static final String KEY_OPEN_COUNT_TO_RATE = "open count to rate";
    // true if rated OR rating denied - false otherwise
    private static final String KEY_RATED = "rated";

    private static final String KEY_LAST_MEDIA_COLLECTION_TYPE = "last_media_collection_type";
    private static final String KEY_LAST_MEDIA_COLLECTION_ID = "last_media_collection_id";
    private static final String KEY_LAST_SONG_ID = "last_song_id";
    private static final String KEY_LAST_PLAYBACK_POSITION = "last_playback_position";

    // player and playback
    private static final String KEY_PLAYBACK_REPEAT_MODE = "playback_repeat_mode";
    private static final String KEY_PLAYBACK_SHUFFLE_MODE = "playback_shuffle_mode";

    // headset and earphones
    private static final String KEY_PAUSE_ON_UNPLUGGED = "pause_on_unplugged";
    private static final String KEY_RESUME_ON_PLUGGED_IN = "resume_on_plugged_in";

    private static final String KEY_ALBUM_GRID_ENABLED = "album_big_item_displaying";
    private static final String KEY_THEME = "app_theme";

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

    public void saveTheme(@Theme int theme) {
        preferences.edit().putInt(KEY_THEME, theme).apply();
    }

    public @Theme int getTheme() {
        final int value = preferences.getInt(KEY_THEME, THEME_DARK_BLUE);
        // check if the given int value is one of the available themes
        switch (value) {
            case THEME_LIGHT:
            case THEME_DARK_BLUE:
            case THEME_DARK_BLUE_ESPECIAL:
            case THEME_DARK_PURPLE: return value;
            // The value is not valid. Return default theme int
            default: return THEME_DARK_BLUE;
        }
    }

    public void saveLastMediaCollectionType(@SongQueue.QueueType int type) {
        preferences.edit().putInt(KEY_LAST_MEDIA_COLLECTION_TYPE, type).apply();
    }

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

    public @SongQueue.QueueType
    int getLastMediaCollectionType() {
        return preferences.getInt(KEY_LAST_MEDIA_COLLECTION_TYPE, SongQueue.CHUNK);
    }

    public long getLastMediaCollectionId() {
        return preferences.getLong(KEY_LAST_MEDIA_COLLECTION_ID, /* no id */ - 1);
    }

    public long getLastSongId() {
        return preferences.getLong(KEY_LAST_SONG_ID, -1);
    }

    @Override
    public int getLastPlaybackPosition() {
        return preferences.getInt(KEY_LAST_PLAYBACK_POSITION, 0);
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

    public int getOpenCount() {
        return preferences.getInt(KEY_TOTAL_OPEN_COUNT, 0);
    }

    public void setOpenCount(int count) {
        preferences.edit().putInt(KEY_TOTAL_OPEN_COUNT, count).apply();
    }

    public int getOpenCountToRate() {
        return preferences.getInt(KEY_OPEN_COUNT_TO_RATE, 5);
    }

    public void setOpenCountToRate(int count) {
        preferences.edit().putInt(KEY_OPEN_COUNT_TO_RATE, count).apply();
    }

    public boolean getRated() {
        return preferences.getBoolean(KEY_RATED, false);
    }

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
        return Query.createFlowable(
                preferences,
                key,
                new Callable<String>() {
                    @Override
                    public String call() {
                        String order = preferences.getString(key, null);
                        switch (section) {
                            case Library.ALBUMS:
                                return AlbumRepositoryImpl.validateSortOrder(order);

                            case Library.ARTISTS:
                                return ArtistRepositoryImpl.validateSortOrder(order);

                            case Library.GENRES:
                                return GenreRepositoryImpl.validateSortOrder(order);

                            case Library.PLAYLISTS:
                                return PlaylistRepositoryImpl.validateSortOrder(order);

                            case Library.FAVOURITES:
                            case Library.RECENTLY_ADDED:
                            case Library.ALL_SONGS:
                                return SongRepositoryImpl.validateSortOrder(order);

                            case Library.ALBUM:
                                return AlbumChunkRepositoryImpl.validateSortOrder(order);

                            case Library.ARTIST:
                                return ArtistChunkRepositoryImpl.validateSortOrder(order);

                            case Library.GENRE:
                                return GenreChunkRepositoryImpl.validateSortOrder(order);

                            case Library.PLAYLIST:
                                return PlaylistChunkRepositoryImpl.validateSortOrder(order);

                            case Library.FOLDERS: {
                                return MyFileRepositoryImpl.validateSortOrder(order);
                            }

                            case Library.MIXED:
                            default:
                                throw new IllegalArgumentException(
                                        "Unsupported library section: " + section
                                );
                        }
                    }
                }
        );
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
        return Query.createFlowable(
                preferences,
                key,
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        return preferences.getBoolean(key, false);
                    }
                }
        );
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
        List<Integer> sections = new ArrayList<>(8);
        sections.add(Library.ALL_SONGS);
        sections.add(Library.ARTISTS);
        sections.add(Library.ALBUMS);
        sections.add(Library.GENRES);
        sections.add(Library.FAVOURITES);
        sections.add(Library.RECENTLY_ADDED);
        sections.add(Library.PLAYLISTS);
        sections.add(Library.FOLDERS);
        return sections;
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
}
