package com.frolo.muse.model.media;

import com.frolo.music.model.Song;

import java.util.List;
import java.util.Set;


public final class SelectableSongQuery {
    private final List<Song> allItems;
    private final Set<Song> selection;

    public SelectableSongQuery(List<Song> allItems, Set<Song> selection) {
        this.allItems = allItems;
        this.selection = selection;
    }

    public List<Song> getAllItems() {
        return allItems;
    }

    public Set<Song> getSelection() {
        return selection;
    }
}
