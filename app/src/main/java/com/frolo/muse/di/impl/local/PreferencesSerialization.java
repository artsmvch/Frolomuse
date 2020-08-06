package com.frolo.muse.di.impl.local;

import com.frolo.muse.model.crossfade.CrossFadeParams;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;


final class PreferencesSerialization {

    @Nullable
    static String trySerializeCrossFadeParams(@Nullable CrossFadeParams params) {
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
    static CrossFadeParams tryDeserializeCrossFadeParams(@Nullable String value) {
        if (value == null) {
            return null;
        }

        try {
            final JSONObject jsonObject = new JSONObject(value);
            final int interval = jsonObject.getInt("interval");
            final boolean smartInterval = jsonObject.getBoolean("smart_interval");
            return CrossFadeParams.create(interval, smartInterval);
        } catch (JSONException e) {
            return null;
        }
    }

    private PreferencesSerialization() {
    }

}
