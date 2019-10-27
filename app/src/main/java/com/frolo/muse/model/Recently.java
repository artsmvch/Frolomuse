package com.frolo.muse.model;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Recently {
    int FOR_LAST_HOUR = 0;
    int FOR_LAST_DAY = 1;
    int FOR_LAST_WEEK = 2;
    int FOR_LAST_MONTH = 3;
    int FOR_LAST_YEAR = 4;

    @IntDef({FOR_LAST_HOUR, FOR_LAST_DAY, FOR_LAST_WEEK, FOR_LAST_MONTH, FOR_LAST_YEAR})
    @Target({ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Period { }
}
