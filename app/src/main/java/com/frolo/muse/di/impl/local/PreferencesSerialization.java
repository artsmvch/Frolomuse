package com.frolo.muse.di.impl.local;

import com.frolo.muse.model.playback.PlaybackFadingParams;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;


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

    private PreferencesSerialization() {
    }

}
