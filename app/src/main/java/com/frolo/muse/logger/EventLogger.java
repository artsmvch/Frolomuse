package com.frolo.muse.logger;

import java.util.Map;


public interface EventLogger extends Event {
    void log(String event);

    void log(String event, Map<String, String> params);

    void log(Throwable err);
}
