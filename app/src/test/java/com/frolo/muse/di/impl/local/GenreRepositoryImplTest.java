package com.frolo.muse.di.impl.local;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnit4.class)
public class GenreRepositoryImplTest {

    @Test
    public void test_validateSortOrder_Valid() {
        {
            String sortOrder = GenreQuery.Sort.BY_NAME;
            String validated = GenreRepositoryImpl.validateSortOrder(sortOrder);
            assertEquals(sortOrder, validated);
        }
    }

    @Test
    public void test_validateSortOrder_Invalid() {
        {
            String sortOrder = null;
            String validated = GenreRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(GenreQuery.Sort.BY_NAME, validated);
        }

        {
            String sortOrder = GenreQuery.Sort.BY_NAME + "1";
            String validated = GenreRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(GenreQuery.Sort.BY_NAME, validated);
        }

        {
            String sortOrder = "aawd.v.wv;w";
            String validated = GenreRepositoryImpl.validateSortOrder(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(GenreQuery.Sort.BY_NAME, validated);
        }
    }

}
