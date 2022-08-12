package com.frolo.muse.di.impl.remote.lyrics;

import androidx.annotation.WorkerThread;

import com.frolo.muse.model.lyrics.Lyrics;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;


// Lmao, see how it works
final class MetroLyricsApi implements LyricsApi {

    @NotNull
    private String buildUrl(String artistName, String songName) {
        return "https://www.google.com/search?&q=" + artistName + "+" + songName + "+lyrics+metrolyrics";
    }

    private int getMetroLyricsUrlStartIndex(String s) {
        return s.indexOf("http");
    }

    private int getMetroLyricsUrlEndIndex(String s) {
        int htmlIndex = s.indexOf("html");
        if (htmlIndex >= 0) {
            return htmlIndex + 4;
        }
//        int dotComIndex = s.indexOf(".com");
//        if (dotComIndex >= 0) {
//            return dotComIndex + 4;
//        }
        return -1;
    }

    @Override
    @WorkerThread
    @NotNull
    public Lyrics getLyrics(String artistName, String songName) throws Exception {
        String url = buildUrl(artistName, songName);

        // To scrap Google
        Document googlePage = Jsoup.connect(url).get();
        Element elem1 = googlePage.getElementsByClass("g").first();
        Element data = elem1.getElementsByTag("a").first();
        String dataString = data.toString();

        int urlStartIndex = getMetroLyricsUrlStartIndex(dataString);
        if (urlStartIndex < 0) {
            throw new IllegalStateException("MetroLyrics URL start not found while parsing");
        }
        int urlEndIndex = getMetroLyricsUrlEndIndex(dataString);
        if (urlEndIndex < 0) {
            throw new IllegalStateException("MetroLyrics URL end not found while parsing");
        }

        final String metroLyricsUrl = dataString.substring(urlStartIndex, urlEndIndex);

        // To scrap MetroLyrics
        Document document = Jsoup.connect(metroLyricsUrl).get();
        Elements verseElements = document.getElementsByClass("verse");

        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < verseElements.size(); i++) {
            Element verseElement = verseElements.get(i);
            for (TextNode node : verseElement.textNodes()) {
                String text = node.text();
                if (text != null && !text.trim().isEmpty()) {
                    textBuilder.append(text).append(Lyrics.DIVIDER);
                }
            }
            if (i < verseElements.size() - 1) {
                textBuilder.append(Lyrics.DIVIDER);
            }
        }
        String text = textBuilder.toString();
        return new Lyrics(text);
    }

    MetroLyricsApi() {
    }
}
