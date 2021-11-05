package com.frolo.muse.di.impl.local;

import com.frolo.muse.model.playback.PlaybackFadingParams;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


final class PreferencesSerialization {

    @Nullable
    static String trySerializePlaybackFadingParams(@Nullable PlaybackFadingParams params) {
        if (params == null) {
            return null;
        }

        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("interval", params.getInterval());
            jsonObject.put("smart_interval", params.isSmartInterval());
            return jsonObject.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    @Nullable
    static PlaybackFadingParams tryDeserializePlaybackFadingParams(@Nullable String value) {
        if (value == null) {
            return null;
        }

        try {
            final JSONObject jsonObject = new JSONObject(value);
            final int interval = jsonObject.getInt("interval");
            final boolean smartInterval = jsonObject.getBoolean("smart_interval");
            return PlaybackFadingParams.create(interval, smartInterval);
        } catch (JSONException e) {
            return null;
        }
    }

    @Nullable
    static String trySerializeItemIds(@Nullable List<Long> ids) {
        if (ids == null) {
            return null;
        }

        try {
            StringBuilder builder = new StringBuilder();
            final int count = ids.size();
            for (int i = 0; i < count; i++) {
                builder.append(ids.get(i));
                if (i < count - 1) {
                    builder.append(',');
                }
            }
            return builder.toString();
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    static List<Long> tryDeserializeItemIds(@Nullable String value) {
        if (value == null) {
            return null;
        }

        try {
            String[] tokens = value.split(",");
            List<Long> list = new ArrayList<>(tokens.length);
            for (String token : tokens) {
                list.add(Long.parseLong(token));
            }
            return list;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private PreferencesSerialization() {
    }

}
