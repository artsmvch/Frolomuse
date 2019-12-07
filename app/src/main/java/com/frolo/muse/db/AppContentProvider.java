package com.frolo.muse.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.Trace;

/**
 * Content Provider of this application
 */
public class AppContentProvider extends ContentProvider {
    private static final String TAG = AppContentProvider.class.getSimpleName();

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String DB_NAME;
    private static final int DB_VERSION;
    private static final String AUTHORITY;
    static {
        DB_NAME = AppMediaStore.DB_NAME;
        DB_VERSION = AppMediaStore.DB_VERSION;
        AUTHORITY = AppMediaStore.AUTHORITY;
    }

    private static final String SQL_CREATE_FAVOURITES = "create table " + AppMediaStore.Favourites.TABLE + "("
            + AppMediaStore.Favourites._ID + " integer primary key autoincrement not null, "
            + AppMediaStore.Favourites.PATH + " text, "
            + AppMediaStore.Favourites.TIME_ADDED + " long);";

    private static final String SQL_CREATE_PRESETS = "create table " + AppMediaStore.Presets.TABLE + "("
            + AppMediaStore.Presets._ID + " integer primary key, "
            + AppMediaStore.Presets.NAME + " text, "
            + AppMediaStore.Presets.LEVELS + " blob);";

    private static final String SQL_CREATE_LYRICS = "create table " + AppMediaStore.Lyrics.TABLE + "("
            + AppMediaStore.Lyrics._ID + " integer primary key, "
            + AppMediaStore.Lyrics.TEXT + " text, "
            + AppMediaStore.Lyrics.TIME_ADDED + " long);";

    private static final String SQL_CREATE_HIDDEN_FILES = "create table " + AppMediaStore.HiddenFiles.TABLE + "("
            + AppMediaStore.HiddenFiles._ID + " integer primary key, "
            + AppMediaStore.HiddenFiles.ABSOLUTE_PATH + " text, "
            + AppMediaStore.HiddenFiles.TIME_HIDDEN + " long, "
            + "UNIQUE(" + AppMediaStore.HiddenFiles.ABSOLUTE_PATH + "));";

    // TYPES
    private static final String FAVOURITE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + '.' + AppMediaStore.Favourites.TABLE;
    private static final String FAVOURITE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + '.' + AppMediaStore.Favourites.TABLE;
    private static final String PRESET_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + '.' + AppMediaStore.Presets.TABLE;
    private static final String PRESET_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + '.' + AppMediaStore.Presets.TABLE;
    private static final String LYRICS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + '.' + AppMediaStore.Lyrics.TABLE;
    private static final String LYRICS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + '.' + AppMediaStore.Lyrics.TABLE;
    private static final String HIDDEN_FILES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + '.' + AppMediaStore.HiddenFiles.TABLE;
    private static final String HIDDEN_FILES_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + '.' + AppMediaStore.HiddenFiles.TABLE;

    //FOR URI MATCHER
    private static final int URI_FAVOURITE = 1;
    private static final int URI_FAVOURITE_ID = 2;
    private static final int URI_PRESET = 3;
    private static final int URI_PRESET_ID = 4;
    private static final int URI_LYRICS = 5;
    private static final int URI_LYRICS_ID = 6;
    private static final int URI_HIDDEN_FILES = 7;
    private static final int URI_HIDDEN_FILES_ID = 8;

    private static final UriMatcher URI_MATCHER;
    // initializing UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, AppMediaStore.Favourites.TABLE, URI_FAVOURITE);
        URI_MATCHER.addURI(AUTHORITY, AppMediaStore.Favourites.TABLE + "/#", URI_FAVOURITE_ID);
        URI_MATCHER.addURI(AUTHORITY, AppMediaStore.Presets.TABLE, URI_PRESET);
        URI_MATCHER.addURI(AUTHORITY, AppMediaStore.Presets.TABLE + "/#", URI_PRESET_ID);
        URI_MATCHER.addURI(AUTHORITY, AppMediaStore.Lyrics.TABLE, URI_LYRICS);
        URI_MATCHER.addURI(AUTHORITY, AppMediaStore.Lyrics.TABLE + "/#", URI_LYRICS_ID);
        URI_MATCHER.addURI(AUTHORITY, AppMediaStore.HiddenFiles.TABLE, URI_HIDDEN_FILES);
        URI_MATCHER.addURI(AUTHORITY, AppMediaStore.HiddenFiles.TABLE + "/#", URI_HIDDEN_FILES_ID);
    }

    private SQLiteDatabase db;
    private SQLiteOpenHelper dbHelper;

    @Override
    public boolean onCreate() {
        Trace.d(TAG, "Creating");
        dbHelper = new DBHelperImpl(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Trace.d(TAG, "Querying " + uri);
        String table;
        Uri observedUri;
        switch (URI_MATCHER.match(uri)) {
            case URI_FAVOURITE: {
                table = AppMediaStore.Favourites.TABLE;
                observedUri = AppMediaStore.Favourites.CONTENT_URI;
                if (TextUtils.isEmpty(sortOrder)) sortOrder = AppMediaStore.Favourites.TIME_ADDED + " ASC";
                break;
            }
            case URI_FAVOURITE_ID: {
                table = AppMediaStore.Favourites.TABLE;
                observedUri = AppMediaStore.Favourites.CONTENT_URI;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.Favourites._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.Favourites._ID + " = " + id;
                break;
            }
            case URI_PRESET: {
                table = AppMediaStore.Presets.TABLE;
                observedUri = AppMediaStore.Presets.CONTENT_URI;
                if (TextUtils.isEmpty(sortOrder)) sortOrder = AppMediaStore.Presets.NAME + " ASC";
                break;
            }
            case URI_PRESET_ID: {
                table = AppMediaStore.Presets.TABLE;
                observedUri = AppMediaStore.Presets.CONTENT_URI;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.Presets._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.Presets._ID + " = " + id;
                break;
            }
            case URI_LYRICS: {
                table = AppMediaStore.Lyrics.TABLE;
                observedUri = AppMediaStore.Lyrics.CONTENT_URI;
                if (TextUtils.isEmpty(sortOrder)) sortOrder = AppMediaStore.Lyrics.TEXT + " ASC";
                break;
            }
            case URI_LYRICS_ID: {
                table = AppMediaStore.Lyrics.TABLE;
                observedUri = AppMediaStore.Lyrics.CONTENT_URI;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.Lyrics._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.Lyrics._ID + " = " + id;
                break;
            }
            case URI_HIDDEN_FILES: {
                table = AppMediaStore.HiddenFiles.TABLE;
                observedUri = AppMediaStore.HiddenFiles.CONTENT_URI;
                if (TextUtils.isEmpty(sortOrder)) sortOrder = AppMediaStore.HiddenFiles.TIME_HIDDEN + " ASC";
                break;
            }
            case URI_HIDDEN_FILES_ID: {
                table = AppMediaStore.HiddenFiles.TABLE;
                observedUri = AppMediaStore.HiddenFiles.CONTENT_URI;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.HiddenFiles._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.HiddenFiles._ID + " = " + id;
                break;
            }
            default: throw new IllegalArgumentException("Wrong Uri: " + uri);
        }
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), observedUri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case URI_FAVOURITE:         return FAVOURITE_CONTENT_TYPE;
            case URI_FAVOURITE_ID:      return FAVOURITE_CONTENT_ITEM_TYPE;
            case URI_PRESET:            return PRESET_CONTENT_TYPE;
            case URI_PRESET_ID:         return PRESET_CONTENT_ITEM_TYPE;
            case URI_LYRICS:            return LYRICS_CONTENT_TYPE;
            case URI_LYRICS_ID:         return LYRICS_CONTENT_ITEM_TYPE;
            case URI_HIDDEN_FILES:      return HIDDEN_FILES_CONTENT_TYPE;
            case URI_HIDDEN_FILES_ID:   return HIDDEN_FILES_CONTENT_ITEM_TYPE;
            default:                    throw new IllegalArgumentException("Wrong Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri,
                      @Nullable ContentValues values) {
        Trace.d(TAG, "Inserting " + uri);
        if (URI_MATCHER.match(uri) == URI_FAVOURITE) {
            db = dbHelper.getWritableDatabase();
            long rowId = db.insert(AppMediaStore.Favourites.TABLE, null, values);
            Uri resultUri = ContentUris.withAppendedId(AppMediaStore.Favourites.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        } else if (URI_MATCHER.match(uri) == URI_PRESET) {
            db = dbHelper.getWritableDatabase();
            long rowId = db.insert(AppMediaStore.Presets.TABLE, null, values);
            Uri resultUri = ContentUris.withAppendedId(AppMediaStore.Presets.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        } else if (URI_MATCHER.match(uri) == URI_LYRICS) {
            db = dbHelper.getWritableDatabase();
            long rowId = db.insert(AppMediaStore.Lyrics.TABLE, null, values);
            Uri resultUri = ContentUris.withAppendedId(AppMediaStore.Lyrics.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        } else if (URI_MATCHER.match(uri) == URI_HIDDEN_FILES) {
            db = dbHelper.getWritableDatabase();
            long rowId = db.insert(AppMediaStore.HiddenFiles.TABLE, null, values);
            Uri resultUri = ContentUris.withAppendedId(AppMediaStore.HiddenFiles.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        }
        throw new IllegalArgumentException("Wrong Uri: " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        Trace.d(TAG, "Deleting " + uri);
        String table;
        switch (URI_MATCHER.match(uri)) {
            case URI_FAVOURITE: table = AppMediaStore.Favourites.TABLE; break;
            case URI_FAVOURITE_ID: {
                table = AppMediaStore.Favourites.TABLE;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.Favourites._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.Favourites._ID + " = " + id;
                break;
            }
            case URI_PRESET: table = AppMediaStore.Presets.TABLE; break;
            case URI_PRESET_ID: {
                table = AppMediaStore.Presets.TABLE;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.Presets._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.Presets._ID + " = " + id;
                break;
            }
            case URI_LYRICS: table = AppMediaStore.Lyrics.TABLE; break;
            case URI_LYRICS_ID: {
                table = AppMediaStore.Lyrics.TABLE;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.Lyrics._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.Lyrics._ID + " = " + id;
                break;
            }
            case URI_HIDDEN_FILES: table = AppMediaStore.HiddenFiles.TABLE; break;
            case URI_HIDDEN_FILES_ID: {
                table = AppMediaStore.HiddenFiles.TABLE;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.HiddenFiles._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.HiddenFiles._ID + " = " + id;
                break;
            }
            default: throw new IllegalArgumentException("Wrong Uri: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int count = db.delete(table, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        Trace.d(TAG, "Updating " + uri);
        String table;
        switch (URI_MATCHER.match(uri)) {
            case URI_FAVOURITE: table = AppMediaStore.Favourites.TABLE; break;
            case URI_FAVOURITE_ID: {
                table = AppMediaStore.Favourites.TABLE;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.Favourites._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.Favourites._ID + " = " + id;
                break;
            }
            case URI_PRESET: table = AppMediaStore.Presets.TABLE; break;
            case URI_PRESET_ID: {
                table = AppMediaStore.Presets.TABLE;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.Presets._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.Presets._ID + " = " + id;
                break;
            }
            case URI_LYRICS: table = AppMediaStore.Lyrics.TABLE; break;
            case URI_LYRICS_ID: {
                table = AppMediaStore.Lyrics.TABLE;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.Lyrics._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.Lyrics._ID + " = " + id;
                break;
            }
            case URI_HIDDEN_FILES: table = AppMediaStore.HiddenFiles.TABLE; break;
            case URI_HIDDEN_FILES_ID: {
                table = AppMediaStore.HiddenFiles.TABLE;
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) selection = AppMediaStore.HiddenFiles._ID + " = " + id;
                else selection = selection + " AND " + AppMediaStore.HiddenFiles._ID + " = " + id;
                break;
            }
            default: throw new IllegalArgumentException("Wrong Uri: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int count = db.update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private void dumpTransaction(Uri uri) {
        Trace.d(TAG, "Transaction URI: " + uri);
    }

    /**
     * Implementation of {@link SQLiteOpenHelper} to help querying DB, inserting, updating and deleting entries;
     */
    private static class DBHelperImpl extends SQLiteOpenHelper {
        DBHelperImpl(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            Trace.d(TAG, "Creating " + this.getClass().getSimpleName());
            db.execSQL(SQL_CREATE_FAVOURITES);
            db.execSQL(SQL_CREATE_PRESETS);
            db.execSQL(SQL_CREATE_LYRICS);
            db.execSQL(SQL_CREATE_HIDDEN_FILES);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < Versions.V_2) {
                // Need to add Lyrics table
                db.execSQL(SQL_CREATE_LYRICS);

                // The 'presets' table was there since the first version BUT the '_id' column was created as not autoincrement.
                // There is a limitation in the SQLite Database Helper that we cannot alter the column to make it autoincrement.
                // The workaround is just to drop the table and create it again.
                db.execSQL("DROP TABLE " + AppMediaStore.Presets.TABLE);
                db.execSQL(SQL_CREATE_PRESETS);
            }

            if (oldVersion < Versions.V_3) {
                // Add HiddenFiles table
                db.execSQL(SQL_CREATE_HIDDEN_FILES);
            }
        }
    }
}
