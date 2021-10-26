package com.frolo.muse.di.impl.remote.lyrics;

import androidx.annotation.NonNull;

import com.frolo.muse.model.lyrics.Lyrics;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


// omg
final class SongLyricsApi implements LyricsApi {

    private static StringBuilder maybeAppendDivider(StringBuilder builder) {
        if (builder.length() > 0) {
            char last = builder.charAt(builder.length() - 1);
            if (last == '\n') {
                return builder;
            } else {
                return builder.append(Lyrics.DIVIDER);
            }
        }
        return builder;
    }

    private static String encode(String raw) {
        try {
            return URLEncoder.encode(raw, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return raw;
        }
    }

    @NotNull
    private static String buildGoogleSearchUrl(String artistName, String songName) {
        return "https://www.google.com/search?&q=" + artistName + "+" + songName + "+lyrics+songlyrics";
    }

    @NonNull
    private static String buildTargetSiteUrl(String artistName, String songName) {
        return "http://www.songlyrics.com/" + encode(artistName) + "/" + encode(songName) + "-lyrics/";
    }

    @Override
    public Lyrics getLyrics(String artistName, String songName) throws Exception {
        String targetUrl = buildTargetSiteUrl(artistName, songName);

        // To scrap songlyrics
        Document document = Jsoup.connect(targetUrl).get();
        Element lyricsElements = document.getElementById("songLyricsDiv");

        StringBuilder textBuilder = new StringBuilder();
        int nodeCount = lyricsElements.childNodeSize();
        for (int i = 0; i < nodeCount; i++) {
            Node node = lyricsElements.childNode(i);
            if (node instanceof TextNode) {
                String text = ((TextNode) node).text();
                if (text != null && !text.trim().isEmpty()) {
                    textBuilder.append(text);
                    textBuilder = maybeAppendDivider(textBuilder);
                }
            }
            if (i < nodeCount - 1) {
                textBuilder = maybeAppendDivider(textBuilder);
            }
        }

        if (textBuilder.length() - artistName.length() - songName.length() < 100) {
            throw new NullPointerException();
        }

        String text = textBuilder.toString();
        return new Lyrics(text);
    }

    SongLyricsApi() {
    }
}
