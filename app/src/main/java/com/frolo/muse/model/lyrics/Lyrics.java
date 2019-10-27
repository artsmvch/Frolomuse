package com.frolo.muse.model.lyrics;

public final class Lyrics {
    private final static String EMPTY_TEXT = "";

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
}
