package com.frolo.muse.di.impl.local;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnit4.class)
public class GenreChunkRepositoryImplTest {

    @Test
    public void test_validateSortOrder_Valid() {
        {
            String sortOrder = SongQuery.Sort.BY_DEFAULT;
            String validated = GenreChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_TITLE;
            String validated = GenreChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_ALBUM;
            String validated = GenreChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_ARTIST;
            String validated = GenreChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_PLAY_ORDER;
            String validated = GenreChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }
    }

    @Test
    public void test_validateSortOrder_Invalid() {
        {
            String sortOrder = null;
            String validated = GenreChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_PLAY_ORDER;
            String validated = GenreChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }

        {
            String sortOrder = "sortorderr123";
            String validated = GenreChunkRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }
    }

}