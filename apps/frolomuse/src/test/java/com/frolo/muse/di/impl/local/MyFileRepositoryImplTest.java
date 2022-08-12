package com.frolo.muse.di.impl.local;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnit4.class)
public class MyFileRepositoryImplTest {

    @Test
    public void test_validateSortOrder_Valid() {
        {
            String sortOrder = MyFileQuery.Sort.BY_FILENAME;
            String validated = MyFileRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertEquals(sortOrder, validated);
        }
    }

    @Test
    public void test_validateSortOrder_Invalid() {
        {
            String sortOrder = null;
            String validated = MyFileRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(MyFileQuery.Sort.BY_FILENAME, validated);
        }

        {
            String sortOrder = MyFileQuery.Sort.BY_FILENAME + "_";
            String validated = MyFileRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(MyFileQuery.Sort.BY_FILENAME, validated);
        }

        {
            String sortOrder = "file";
            String validated = MyFileRepositoryImpl.getSortOrderOrDefault(sortOrder);
            assertNotEquals(sortOrder, validated);
            assertEquals(MyFileQuery.Sort.BY_FILENAME, validated);
        }
    }

}
