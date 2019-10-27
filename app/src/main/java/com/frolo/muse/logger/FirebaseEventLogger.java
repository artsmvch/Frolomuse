package com.frolo.muse.logger;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class FirebaseEventLogger implements EventLogger {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private static class EventLogCallback implements Runnable {
        final String event;
        final Map<String, String> params;

        EventLogCallback(String event, Map<String, String> params) {
            this.event = event;
            this.params = params;
        }

        @Override
        public void run() {
            // Logging here
        }
    }

    private static class ErrLogCallback implements Runnable {
        final Throwable err;

        ErrLogCallback(Throwable err) {
            this.err = err;
        }

        @Override
        public void run() {
            // Logging here
        }
    }

    @Override
    public void log(String event) {
        mExecutor.execute(new EventLogCallback(event, null));
    }

    @Override
    public void log(String event, Map<String, String> params) {
        mExecutor.execute(new EventLogCallback(event, params));
    }

    @Override
    public void log(Throwable err) {
        mExecutor.execute(new ErrLogCallback(err));
    }
}
