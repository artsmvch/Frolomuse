package com.frolo.muse.logger;

import android.content.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


// Factories
public final class EventLoggerFactory {
    private EventLoggerFactory() { }

    public static EventLogger createFirebase(Context context) {
        return new FirebaseEventLogger(context);
    }

    public static EventLogger createConsole() {
        return new ConsoleEventLogger();
    }

    public static EventLogger compose(List<EventLogger> loggers) {
        return new CompositeEventLogger(Collections.unmodifiableList(loggers));
    }

    public static EventLogger compose(EventLogger... loggers) {
        return compose(Arrays.asList(loggers));
    }
}
