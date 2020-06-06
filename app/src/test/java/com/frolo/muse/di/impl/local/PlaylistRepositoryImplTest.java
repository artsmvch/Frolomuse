package com.frolo.muse.di.impl.local;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnit4.class)
public class PlaylistRepositoryImplTest {

    @Test
    public void test_validateSortOrder_Valid() {
        {
            String sortOrder = PlaylistQuery.Sort.BY_NAME;
            String validated = PlaylistRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = PlaylistQuery.Sort.BY_DATE_ADDED;
            String validated = PlaylistRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }

        {
            String sortOrder = PlaylistQuery.Sort.BY_DATE_MODIFIED;
            String validated = PlaylistRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }
    }

    @Test
    public void test_validateSortOrder_Invalid() {
        {
            String sortOrder = null;
            String validated = PlaylistRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(PlaylistQuery.Sort.BY_NAME, validated);
        }

        {
            String sortOrder = PlaylistQuery.Sort.BY_NAME + "z";
            String validated = PlaylistRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(PlaylistQuery.Sort.BY_NAME, validated);
        }

        {
            String sortOrder = "sort orderrr";
            String validated = PlaylistRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(PlaylistQuery.Sort.BY_NAME, validated);
        }
    }

}
