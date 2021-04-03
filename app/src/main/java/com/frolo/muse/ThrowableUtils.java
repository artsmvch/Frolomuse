package com.frolo.muse;

import java.io.PrintWriter;
import java.io.StringWriter;


public final class ThrowableUtils {

    public static String stackTraceToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private ThrowableUtils() {
    }
}
