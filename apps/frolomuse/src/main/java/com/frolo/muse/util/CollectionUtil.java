package com.frolo.muse.util;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class CollectionUtil {

    public static <E> boolean areListContentsEqual(@Nullable List<E> list1, @Nullable List<E> list2) {
        if (list1 == list2) {
            return true;
        }

        int listSize1 = list1 != null ? list1.size() : 0;
        int listSize2 = list2 != null ? list2.size() : 0;

        if (listSize1 != listSize2) {
            return false;
        }

        for (int i = 0; i < listSize1; i++) {
            E item1 = list1.get(i);
            E item2 = list2.get(i);
            if (!Objects.equals(item1, item2)) {
                return false;
            }
        }

        return true;
    }

    private CollectionUtil() {
    }
}
