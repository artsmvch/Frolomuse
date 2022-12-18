package com.frolo.muse.logger;

import java.util.Map;


public interface EventLogger {
    void log(String event);

    void log(String event, Map<String, String> params);

    void log(Throwable err);

    EventLogger NONE = new EventLogger() {
        @Override
        public void log(String event) {
        }

        @Override
        public void log(String event, Map<String, String> params) {
        }

        @Override
        public void log(Throwable err) {
        }
    };
}
