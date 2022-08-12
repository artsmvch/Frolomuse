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
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_TITLE;
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_ALBUM;
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_ARTIST;
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_PLAY_ORDER;
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_DURATION;
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_DATE_ADDED;
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }
    }

    @Test
    public void test_validateSortOrder_Invalid() {
        {
            String sortOrder = null;
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }

        {
            String sortOrder = SongQuery.Sort.BY_PLAY_ORDER;
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }

        {
            String sortOrder = "sortorderr123";
            String validated = GenreChunkRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(SongQuery.Sort.BY_DEFAULT, validated);
        }
    }

}