package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.frolo.muse.R;
import com.frolo.muse.content.AppMediaStore;
import com.frolo.muse.model.preset.CustomPreset;
import com.frolo.muse.model.preset.VoidPreset;
import com.frolo.muse.repository.PresetRepository;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Action;


public class PresetRepositoryImpl implements PresetRepository {

    private final static Uri URI = AppMediaStore.Presets.getContentUri();

    private final static String[] EMPTY_PROJECTION = new String[] {};

    private final static String[] PROJECTION = {
            AppMediaStore.Presets._ID,
            AppMediaStore.Presets.NAME,
            AppMediaStore.Presets.LEVELS };


    private final static Query.Builder<CustomPreset> BUILDER =
            new Query.Builder<CustomPreset>() {
                @Override
                public CustomPreset build(Cursor cursor, String[] projection) {
                    byte[] byteLevels = cursor.getBlob(
                            cursor.getColumnIndex(projection[2]));

                    return new CustomPreset(
                            cursor.getLong(cursor.getColumnIndex(projection[0])),
                            cursor.getString(cursor.getColumnIndex(projection[1])),
                            convertBytesToShorts(byteLevels)
                    );
                }
            };

    private static byte[] convertShortsToBytes(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];
        ByteBuffer.wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .put(shorts);
        return bytes;
    }

    private static short[] convertBytesToShorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .get(shorts);
        return shorts;
    }

    private final Context mContext;

    public PresetRepositoryImpl(Context context) {
        this.mContext = context;
    }

    @Override
    public Flowable<List<CustomPreset>> getPresets() {
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        return Query.query(
                mContext.getContentResolver(),
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER);
    }

    @Override
    public Single<VoidPreset> getVoidPreset() {
        return Single.just(new VoidPreset(mContext.getString(R.string.preset_none)));
    }

    @Override
    public Flowable<CustomPreset> getPresetById(final long id) {
        return Query.querySingle(
                mContext.getContentResolver(),
                URI,
                PROJECTION,
                id,
                BUILDER);
    }

    @Override
    public Single<CustomPreset> create(final String name, final short[] levels) {
        return Single.fromCallable(new Callable<CustomPreset>() {
            @Override public CustomPreset call() throws Exception {

                if (name.trim().isEmpty()) {
                    throw new Exception(
                            mContext.getString(R.string.name_is_empty));
                }

                ContentResolver resolver = mContext.getContentResolver();

                final boolean exists;
                try (Cursor cursor = resolver.query(
                        URI, EMPTY_PROJECTION, AppMediaStore.Presets.NAME + " = ?",
                        new String[]{ name }, null)) {
                    exists = cursor != null && cursor.getCount() != 0;
                }

                if (exists) {
                    throw new Exception(
                            mContext.getString(R.string.such_name_already_exists));
                }

                ContentValues values = new ContentValues();
                values.put(AppMediaStore.Presets.NAME, name);
                byte[] byteLevels = convertShortsToBytes(levels);
                values.put(AppMediaStore.Presets.LEVELS, byteLevels);

                Uri uri = resolver.insert(URI, values);
                if (uri == null) {
                    throw new Exception("Insert OP returned null uri");
                }

                long id = Long.parseLong(uri.getLastPathSegment());

                if (id >= 0) {
                    return new CustomPreset(id, name, levels);
                } else {
                    throw new Exception("Failed to save preset");
                }
            }
        });
    }

    @Override
    public Completable delete(final CustomPreset preset) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                ContentResolver resolver = mContext.getContentResolver();

                String selection = AppMediaStore.Presets._ID + " =?";
                String[] selectionArgs = { String.valueOf(preset.getId()) };

                int deletedCount = resolver.delete(URI, selection, selectionArgs);

                if (deletedCount != 1) {
                    throw new Exception("Failed to delete preset: " + preset);
                }
            }
        });
    }
}
