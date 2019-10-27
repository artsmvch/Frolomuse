package com.frolo.muse.logger;

import android.content.Context;

import java.util.Arrays;

// Factory
public final class EventLoggers {
    private EventLoggers() { }

    public static EventLogger createFlurry(Context context) {
        return new FlurryEventLogger(context);
    }

    public static EventLogger createCrashlytics(Context context) {
        return new CrashlyticsEventLogger(context);
    }

    public static EventLogger createFirebase() {
        return new FirebaseEventLogger();
    }

    public static EventLogger createMute() {
        return new MuteEventLogger();
    }

    public static EventLogger createDroid() {
        return new AndroidEventLogger();
    }

    public static EventLogger compose(EventLogger... loggers) {
        return new CompositeEventLogger(Arrays.asList(loggers));
    }
}
