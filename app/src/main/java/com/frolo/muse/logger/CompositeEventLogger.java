package com.frolo.muse.logger;

import java.util.List;
import java.util.Map;

class CompositeEventLogger implements EventLogger {
    private final List<EventLogger> loggers;

    CompositeEventLogger(List<EventLogger> loggers) {
        this.loggers = loggers;
    }

    @Override
    public void log(String event) {
        for (EventLogger logger : loggers) {
            logger.log(event);
        }
    }

    @Override
    public void log(String event, Map<String, String> params) {
        for (EventLogger logger : loggers) {
            logger.log(event, params);
        }
    }

    @Override
    public void log(Throwable err) {
        for (EventLogger logger : loggers) {
            logger.log(err);
        }
    }
}
