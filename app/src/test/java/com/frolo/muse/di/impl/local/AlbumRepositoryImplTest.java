package com.frolo.muse.di.impl.local;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnit4.class)
public class AlbumRepositoryImplTest {

    @Test
    public void test_validateSortOrder_Valid() {
        {
            String sortOrder = AlbumQuery.Sort.BY_ALBUM;
            String validated = AlbumRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = AlbumQuery.Sort.BY_NUMBER_OF_SONGS;
            String validated = AlbumRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }
    }

    @Test
    public void test_validateSortOrder_Invalid() {
        {
            String sortOrder = null;
            String validated = AlbumRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(AlbumQuery.Sort.BY_ALBUM, validated);
        }

        {
            String sortOrder = AlbumQuery.Sort.BY_ALBUM + "q";
            String validated = AlbumRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(AlbumQuery.Sort.BY_ALBUM, validated);
        }

        {
            String sortOrder = "aawd.v.wv;w";
            String validated = AlbumRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(AlbumQuery.Sort.BY_ALBUM, validated);
        }
    }

}
