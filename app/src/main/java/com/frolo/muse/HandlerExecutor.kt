package com.frolo.muse

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor


class HandlerExecutor(private val handler: Handler): Executor {

    constructor(looper: Looper):  this(Handler(looper))

    override fun execute(command: Runnable?) {
        if (command != null) {
            handler.post(command)
        }
    }

}