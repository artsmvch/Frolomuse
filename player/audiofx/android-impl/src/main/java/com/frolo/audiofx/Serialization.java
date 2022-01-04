package com.frolo.audiofx;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


final class Serialization {
    private Serialization() {
    }

    private static JSONArray toJSONArray(short[] arr) {
        JSONArray jsonArray = new JSONArray();
        if (arr != null) {
            for (short sh : arr) {
                jsonArray.put(sh);
            }
        }
        return jsonArray;
    }

    @Nullable
    private static short[] toShortArray(JSONArray arr) {
        try {
            short[] shorts = new short[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                shorts[i] = (short) arr.getInt(i);
            }
            return shorts;
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Serializes the given <code>arr</code> array of shorts in an object of type {@link String}.
     * The output will be text representing elements from the given array separated by commas.
     * @param arr to serialize
     * @return an object of type String
     */
    @Nullable
    static String trySerializeShorts(@Nullable short[] arr) {
        if (arr == null || arr.length == 0)
            return null;

        try {
            final int length = arr.length;
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(arr[i]);
                if (i < length - 1) sb.append(',');
            }
            return sb.toString();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Deserializes an array of shorts from the given <code>s</code> object of type {@link String}.
     * <code>s</code> is expected to be text representing short numbers separated by commas.
     * @param s to deserialize
     * @return deserialized array of shorts
     */
    @Nullable
    static short[] tryDeserializeShorts(@Nullable String s) {
        if (s == null || s.isEmpty())
            return new short[5];

        try {
            String[] levelStrings = s.split(",");
            List<Short> levelList = new ArrayList<>(levelStrings.length);
            for (String levelString : levelStrings) {
                try {
                    short level = Short.parseShort(levelString);
                    levelList.add(level);
                } catch (Throwable ignored) {
                }
            }

            short[] levels = new short[levelList.size()];
            for (int i = 0; i < levelList.size(); i++) {
                levels[i] = levelList.get(i);
            }

            return levels;
        } catch (Throwable t) {
            return new short[5];
        }
    }

    @Nullable
    static String trySerializeCustomPreset(@Nullable CustomPreset preset) {
        if (preset == null)
            return null;

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", preset.getId());
            jsonObject.put("name", preset.getName());
            jsonObject.put("levels", toJSONArray(preset.getLevels()));
            return jsonObject.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    @Nullable
    static CustomPreset tryDeserializeCustomPreset(@Nullable String s) {
        if (s == null || s.isEmpty())
            return null;

        try {
            JSONObject jsonObject = new JSONObject(s);

            long id = jsonObject.getLong("id");
            String name = jsonObject.getString("name");
            short[] levels = toShortArray(jsonObject.getJSONArray("levels"));
            levels = levels != null ? levels : new short[5];

            return new CustomPreset(id, name, levels);
        } catch (Throwable t) {
            return null;
        }
    }

    @Nullable
    static String trySerializeNativePreset(@Nullable NativePreset preset) {
        if (preset == null)
            return null;

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", preset.getIndex());
            jsonObject.put("name", preset.getName());
            return jsonObject.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    @Nullable
    static NativePreset tryDeserializeNativePreset(@Nullable String s) {
        if (s == null || s.isEmpty())
            return null;

        try {
            JSONObject jsonObject = new JSONObject(s);

            short index = (short) jsonObject.getInt("index");
            String name = jsonObject.getString("name");

            // TODO: localize the name according to the current context
            return new NativePreset(index, name);
        } catch (Throwable t) {
            return null;
        }
    }

    static int trySerializeReverb(@Nullable Reverb reverb) {
        if (reverb == null)
            return 0;

        switch (reverb) {
            case NONE:          return 1;
            case LARGE_HALL:    return 2;
            case LARGE_ROOM:    return 3;
            case MEDIUM_HALL:   return 4;
            case MEDIUM_ROOM:   return 5;
            case PLATE:         return 6;
            case SMALL_ROOM:    return 7;
            default:            return 0;
        }
    }

    static Reverb tryDeserializeReverb(int i) {
        switch (i) {
            case 1:     return Reverb.NONE;
            case 2:     return Reverb.LARGE_HALL;
            case 3:     return Reverb.LARGE_ROOM;
            case 4:     return Reverb.MEDIUM_HALL;
            case 5:     return Reverb.MEDIUM_ROOM;
            case 6:     return Reverb.PLATE;
            case 7:     return Reverb.SMALL_ROOM;
            default:    return Reverb.NONE;
        }
    }
}
