package com.frolo.muse.logger;

import java.util.Map;

class MuteEventLogger implements EventLogger {

    @Override
    public void log(String event) {
    }

    @Override
    public void log(String event, Map<String, String> params) {
    }

    @Override
    public void log(Throwable err) {
    }
}
