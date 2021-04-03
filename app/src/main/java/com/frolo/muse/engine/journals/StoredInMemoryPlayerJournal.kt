package com.frolo.muse.engine.journals

import androidx.annotation.WorkerThread
import com.frolo.muse.engine.PlayerJournal
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue


class StoredInMemoryPlayerJournal(val size: Int = DEFAULT_SIZE) : PlayerJournal {

    private val logExecutor: Executor by lazy {
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable).apply {
                name = "StoredInMemoryPlayerJournal"
            }
        }
    }

    private val logDataQueue = LinkedBlockingQueue<LogData>(size)

    private val logDataSnapshotProcessor = BehaviorProcessor.create<List<LogData>>()

    private fun timestamp(): Long = System.currentTimeMillis()

    @WorkerThread
    private fun appendLogData(data: LogData) {
        logDataQueue.add(data)
        logDataSnapshotProcessor.onNext(logDataQueue.toList())
    }

    override fun logMessage(message: String?) {
        val timestamp = timestamp()
        logExecutor.execute {
            appendLogData(LogData.Message(timestamp, message))
        }
    }

    override fun logError(message: String?, error: Throwable?) {
        val timestamp = timestamp()
        logExecutor.execute {
            appendLogData(LogData.Error(timestamp, message, error))
        }
    }

    fun observeSnapshot(): Flowable<List<LogData>> {
        return logDataSnapshotProcessor
    }

    sealed class LogData {
        abstract val timestamp: Long

        data class Message(
            override val timestamp: Long,
            val value: String?
        ): LogData()

        data class Error(
            override val timestamp: Long,
            val message: String?,
            val value: Throwable?
        ): LogData()
    }

    companion object {
        private const val DEFAULT_SIZE = 100 * 1024
    }

}