package com.frolo.muse

import com.frolo.muse.logger.EventLogger


class TestEventLogger : EventLogger {
    override fun log(event: String?) = Unit

    override fun log(event: String?, params: MutableMap<String, String>?) = Unit

    override fun log(err: Throwable?) = Unit
}