package com.frolo.muse.ui.main.settings.journal

import android.annotation.SuppressLint
import android.content.ClipData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.R
import com.frolo.muse.ThrowableUtils
import com.frolo.muse.android.SendTextFileIntent
import com.frolo.muse.android.clipboardManager
import com.frolo.muse.android.startActivitySafely
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.player.PlayerJournal
import com.frolo.muse.player.journals.CompositePlayerJournal
import com.frolo.muse.player.journals.StoredInMemoryPlayerJournal
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.rx.flowable.doOnNextIndexed
import com.frolo.muse.ui.base.BaseAndroidViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.io.File
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.NullPointerException


class PlayerJournalViewModel @Inject constructor(
    frolomuseApp: FrolomuseApp,
    private val playerJournal: PlayerJournal,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseAndroidViewModel(frolomuseApp, eventLogger) {

    private var copyLogsToClipboardDisposable: Disposable? = null

    private var sendLogsDisposable: Disposable? = null

    private val _logDataItems by lazy {
        MutableLiveData<List<LogDataItem>>().apply {
            loadLogDataItems()
        }
    }
    val logDataItems: LiveData<List<LogDataItem>> get() = _logDataItems

    private val _scrollToPosition = SingleLiveEvent<Int>()
    val scrollToPosition: LiveData<Int> get() = _scrollToPosition

    private val _notifyLogsCopied = SingleLiveEvent<Unit>()
    val notifyLogsCopied: LiveData<Unit> get() = _notifyLogsCopied

    private fun loadLogDataItems() {
        Single.fromCallable {
            peekStoredInMemoryPlayerJournal(playerJournal)
                    ?: throw NullPointerException("No StoredInMemoryPlayerJournal found") }
                .subscribeOn(schedulerProvider.computation())
                .flatMapPublisher { it.observeSnapshot() }
                .map { logDataList ->
                    logDataList.map { logData ->
                        val time = DATE_FORMAT.format(logData.timestamp)
                        val message = when (logData) {
                            is StoredInMemoryPlayerJournal.LogData.Message -> logData.value.orEmpty()
                            is StoredInMemoryPlayerJournal.LogData.Error -> logData.message.orEmpty()
                        }
                        val error = (logData as? StoredInMemoryPlayerJournal.LogData.Error)?.value
                        val errorStackTrace = error?.let { ThrowableUtils.stackTraceToString(it) }
                        LogDataItem(
                            time = time,
                            message = message,
                            errorStackTrace = errorStackTrace
                        )
                    }
                }
                .observeOn(schedulerProvider.main())
                .doOnNextIndexed { index, snapshot ->
                    _logDataItems.value = snapshot
                    if (index == 0) {
                        _scrollToPosition.value = (snapshot.size - 1).coerceAtLeast(0)
                    }
                }
                .subscribeFor {  }
    }

    private fun peekStoredInMemoryPlayerJournal(journal: PlayerJournal): StoredInMemoryPlayerJournal? {
        if (journal is StoredInMemoryPlayerJournal) {
            return journal
        }
        if (journal is CompositePlayerJournal) {
            journal.journals.forEach {
                if (it is StoredInMemoryPlayerJournal) {
                    return it
                }
            }
        }
        return null
    }

    private fun getLogsAsText(): Single<String> {
        val items = logDataItems.value.orEmpty()
        return Single.fromCallable {
            val strings = items.map { logDataItem ->
                val stringBuilder = StringBuilder()
                stringBuilder.append(logDataItem.time)
                stringBuilder.append(' ')
                stringBuilder.append(logDataItem.message)
                if (!logDataItem.errorStackTrace.isNullOrBlank()) {
                    stringBuilder.append('\n')
                    stringBuilder.append(logDataItem.errorStackTrace)
                }
                stringBuilder.toString()
            }
            strings.joinToString(separator = "\n\n")
        }
                .subscribeOn(schedulerProvider.computation())
    }

    fun onCopyLogsToClipboard() {
        getLogsAsText()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { text ->
                val clipboardManager = justApplication.clipboardManager
                        ?: throw NullPointerException("ClipboardManager not found")
                val clipData = ClipData.newPlainText(justApplication.getString(R.string.player_logs), text)
                clipboardManager.setPrimaryClip(clipData)
            }
            .doOnSubscribe { disposable ->
                copyLogsToClipboardDisposable?.dispose()
                copyLogsToClipboardDisposable = disposable
                disposable.save()
            }
            .subscribeFor {
                _notifyLogsCopied.call()
            }
    }

    fun onSendLogsClicked() {
        getLogsAsText()
            .observeOn(schedulerProvider.worker())
            .map { text ->
                val cacheFolder = justApplication.cacheDir
                val file = File(cacheFolder, LOGS_FILENAME)
                if (!file.exists()) {
                    file.createNewFile()
                }
                file.printWriter().use { writer ->
                    writer.print(text)
                }
                return@map file
            }
            .observeOn(schedulerProvider.main())
            .doOnSuccess { file ->
                //val actionTitle = justApplication.getString(R.string.send_player_logs_title)
                val intent = SendTextFileIntent(justApplication, file)
                justApplication.startActivitySafely(intent)
            }
            .ignoreElement()
            .doOnSubscribe { disposable ->
                sendLogsDisposable?.dispose()
                sendLogsDisposable = disposable
                disposable.save()
            }
            .subscribeFor { }

    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        private val DATE_FORMAT = SimpleDateFormat("hh:mm:ss")

        private const val LOGS_FILENAME = "player_logs.text"
    }

}