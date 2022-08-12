package com.frolo.muse.di.impl.remote.lyrics;

import androidx.annotation.WorkerThread;

import com.frolo.muse.model.lyrics.Lyrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


// This API is currently not available
@Deprecated
final class OrionLyricsApi implements LyricsApi {

    private final static String BASE_URL = "https://orion.apiseeds.com/api/music/lyric";
    private final static String PARAM_API_KEY = "apikey";

    private final String apiKey;

    OrionLyricsApi(String apiKey) {
        this.apiKey = apiKey;
    }

    private String buildUrl(String artist, String songName, String apiKey) throws UnsupportedEncodingException {
        String encodedArtist = URLEncoder.encode(artist, "UTF-8");
        String encodedSongName = URLEncoder.encode(songName, "UTF-8");
        return BASE_URL + '/' + encodedArtist + '/' + encodedSongName + '?' + PARAM_API_KEY + '=' + apiKey;
    }

    private String readStream(InputStream stream) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }

    private Lyrics parseJson(String json) throws JSONException {
        JSONObject rootObject = new JSONObject(json);
        JSONObject resultObject = rootObject.getJSONObject("result");
        JSONObject trackObject = resultObject.getJSONObject("track");
        String text = trackObject.getString("text");
        return new Lyrics(text);
    }

    @Override
    @WorkerThread
    public Lyrics getLyrics(String artist, String songName) throws Exception {
        String urlPath = buildUrl(artist, songName, apiKey);
        URL url = new URL(urlPath);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String json = readStream(in);
            return parseJson(json);
        } finally {
            urlConnection.disconnect();
        }
    }
}
