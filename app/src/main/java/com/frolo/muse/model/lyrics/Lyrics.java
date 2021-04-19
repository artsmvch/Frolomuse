package com.frolo.muse.model.lyrics;

import java.util.Objects;


public final class Lyrics {
    private final static String EMPTY_TEXT = "";

    public final static String DIVIDER = "\n";

    private final String text;

    public Lyrics(String text) {
        this.text = (text != null ? text : EMPTY_TEXT);
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lyrics lyrics = (Lyrics) o;
        return Objects.equals(text, lyrics.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}
