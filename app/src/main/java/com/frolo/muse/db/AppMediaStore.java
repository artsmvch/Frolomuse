package com.frolo.muse.db;

import android.net.Uri;
import android.provider.BaseColumns;

import com.frolo.muse.BuildConfig;

/**
 * Using interface of this class, applications can access to the data in the app
 */
public final class AppMediaStore {
    // no need to create instances
    private AppMediaStore() {
    }

    static final String DB_NAME = "Frolomuse_db";
    static final int DB_VERSION = Versions.V_3;

    static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".MediaStore";

    static final String SCHEME = "content";

    public static class Favourites implements BaseColumns {
        private Favourites() {
        }

        static final String TABLE = "favourites";
        public static final String PATH = "path";
        public static final String TIME_ADDED = "time_added";
        static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY + '/' + TABLE);

        public static Uri getContentUri() {
            return CONTENT_URI;
        }
    }

    public static class Presets implements BaseColumns {
        private Presets() {
        }

        static final String TABLE = "presets";
        public static final String NAME = "name";
        public static final String LEVELS = "levels";
        public static final String TIME_ADDED = "time_added";

        static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY + '/' + TABLE);

        public static Uri getContentUri() {
            return CONTENT_URI;
        }
    }

    public static class Lyrics implements BaseColumns {
        private Lyrics() {
        }

        static final String TABLE = "lyrics";
        public static final String TEXT = "text";
        public static final String TIME_ADDED = "time_added";

        static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY + '/' + TABLE);

        public static Uri getContentUri() {
            return CONTENT_URI;
        }
    }

    public static class HiddenFiles implements BaseColumns {
        private HiddenFiles() {
        }

        static final String TABLE = "hidden_files";
        public static final String ABSOLUTE_PATH = "absolute_path";
        public static final String TIME_HIDDEN = "time_hidden";

        static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY + '/' + TABLE);

        public static Uri getContentUri() {
            return CONTENT_URI;
        }
    }
}
