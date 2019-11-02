package com.frolo.muse.di.impl.local;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnit4.class)
public class AlbumChunkRepositoryImplTest {

    @Test
    public void test_validateSortOrder_Valid() {
        {
            String sortOrder = SongQuery.Sort.BY_DEFAULT;
            String validated = AlbumChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_TITLE;
            String validated = AlbumChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_ALBUM;
            String validated = AlbumChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_ARTIST;
            String validated = AlbumChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_PLAY_ORDER;
            String validated = AlbumChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }
    }

    @Test
    public void test_validateSortOrder_Invalid() {
        {
            String sortOrder = null;
            String validated = AlbumChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_ALBUM;
            String validated = AlbumChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_PLAY_ORDER;
            String validated = AlbumChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }

        {
            String sortOrder = "qpwoe pz v";
            String validated = AlbumChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }
    }

}