package com.frolo.muse.logger;


// All constant events and params
interface Event {

    public static String EVENT_APP_LAUNCHED = "APP_LAUNCHED";

    public static String EVENT_ALBUM_OPENED = "ALBUM_OPENED";
    public static String EVENT_ARTIST_OPENED = "ARTIST_OPENED";
    public static String EVENT_GENRE_OPENED = "GENRE_OPENED";
    public static String EVENT_PLAYLIST_OPENED = "PLAYLIST_OPENED";
    public static String EVENT_FOLDER_OPENED = "FOLDER_OPENED";

    public static String EVENT_LIBRARY_OPENED = "LIBRARY_OPENED";
    public static String EVENT_PLAYER_OPENED = "PLAYER_OPENED";
    public static String EVENT_AUDIO_FX_OPENED = "AUDIO_FX_OPENED";
    public static String EVENT_SEARCH_OPENED = "SEARCH_OPENED";
    public static String EVENT_SETTINGS_OPENED = "SETTINGS_OPENED";

    public static String EVENT_RING_CUTTER_OPENED = "RING_CUTTER_OPENED";

    public static String EVENT_THEME_APPLIED = "THEME_APPLIED";

    public static String EVENT_SECTION_CHOOSER_OPENED = "SECTION_CHOOSER_OPENED";

    public static String EVENT_SLEEP_TIMER_SET = "SLEEP_TIMER_SET";

    public static String EVENT_ITEMS_SHARING = "ITEMS_SHARING";

    public static String PARAM_COUNT = "count";
    public static String PARAM_THEME = "theme";
}
