package com.frolo.muse.engine

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.frolo.muse.BuildConfig
import com.frolo.muse.ThreadStrictMode
import com.frolo.muse.Trace
import com.frolo.muse.model.media.Song
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlin.math.max
import kotlin.math.min


/**
 * Thread-safe implementation of [Player] interface.
 * All long engine operations are performed on the engine thread using [engineHandler].
 * All event operations are performed on the event thread using [eventHandler].
 */
class PlayerEngine constructor(
        private val engineHandler: Handler,
        private val eventHandler: Handler,
        private val audioFxApplicable: AudioFxApplicable,
        private val observerRegistry: ObserverRegistry,
        audioManager: AudioManager?
): Player {

    companion object {
        private const val LOG_TAG = "PlayerEngine"

        // AB const
        private const val MIN_AB_INTERVAL = 250
    }

    // Tokens for the engine handler
    private val tokenSkipTo = Any()

    private var engine: MediaPlayer = createEngine()

    private val audioFocusHandler = AudioFocusHandler(audioManager, this)

    private val lock = Any()

    @Volatile
    private var originSongQueue: SongQueue? = null

    @Volatile
    private var currentSongQueue: SongQueue = SongQueue.empty()

    @Volatile
    private var currentPositionInQueue = -1

    @Volatile
    private var currentSong: Song? = null

    @Volatile
    @Player.ShuffleMode
    private var shuffleMode = Player.SHUFFLE_OFF

    @Volatile
    @Player.RepeatMode
    private var repeatMode = Player.REPEAT_OFF


    // [!] The following are flags that indicate the state of the engine in which it should be.

    // Indicates the preparation state of the engine
    @Volatile
    private var isPreparedFlag: Boolean = false

    // Indicate the playback state of the engine
    @Volatile
    private var isPlayingFlag: Boolean = false

    // AB
    private var pointA: Int? = null
    private var pointB: Int? = null

    private var disposableAB: Disposable? = null

    /****************************
     ****** HELPER METHODS ******
     ***************************/

    private fun isInDebugMode() = BuildConfig.DEBUG

    private fun getEngineErrorMessage(what: Int): String {
        return when(what) {
            MediaPlayer.MEDIA_ERROR_IO -> "IO"
            MediaPlayer.MEDIA_ERROR_MALFORMED -> "MALFORMED"
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> "NOT_VALID_FOR_PROGRESSIVE_PLAYBACK"
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "SERVER_DIED"
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "TIMED_OUT"
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> "UNKNOWN"
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "UNSUPPORTED"
            else -> "UNKNOWN"
        }
    }

    private fun generateEngineError(what: Int): Throwable {
        return RuntimeException(getEngineErrorMessage(what))
    }

    private fun isOnEngineThread(): Boolean {
        return Thread.currentThread() == engineHandler.looper.thread
    }

    private fun isOnEventThread(): Boolean {
        return Thread.currentThread() == eventHandler.looper.thread
    }

    private fun handleEngineErrorInternal(what: Int) {
        val err = generateEngineError(what)
        Trace.e(LOG_TAG, err)

        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            // It's a critical error
            if (isInDebugMode()) {
                throw err
            }

            try {
                engine.release()
            } catch (e: Throwable) {
                Trace.e(LOG_TAG, "Failed to release the engine", e)
            }

            engine = createEngine()

            isPreparedFlag = false
            // in case the error is critical, do not start the playback
            isPlayingFlag = false

            currentSong?.also { safeSong ->
                engine.runCatching {
                    Trace.d(LOG_TAG, "Preparing: [src=${safeSong.source}")

                    reset()
                    setDataSource(safeSong.source)
                    prepare()
                    // assumed prepared
                    isPreparedFlag = true

                    execOnEventThread {
                        observerRegistry.onPrepared(this@PlayerEngine)
                    }

                    // applying audio fx
                    audioFxApplicable.apply(audioSessionId)

                    observerRegistry.onPlaybackPaused(this@PlayerEngine)
                }.onFailure { err ->
                    Trace.e(LOG_TAG, err)
                    execOnEventThread {
                        observerRegistry.onPlaybackPaused(this@PlayerEngine)
                    }
                }
            }
        }
    }

    /*Creates new engine*/
    private fun createEngine(): MediaPlayer {
        return MediaPlayer().apply {
            setAuxEffectSendLevel(1.0f)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val attrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build()
                setAudioAttributes(attrs)
            }
            setOnErrorListener { _, what, _ ->
                handleEngineErrorInternal(what)

                // [!] Always return true.
                // Returning false may stop the playback.
                true
            }
            setOnCompletionListener {
                Trace.d(LOG_TAG, "Engine completed")
                execOnEngineThread {
                    skipToNextInternal(byUser = false)
                }
            }
            setOnPreparedListener {
                Trace.d(LOG_TAG, "Engine prepared")
                // No action here!
                // Because we use synchronized MediaPlayer.prepare() method
            }
        }
    }

    /**
     * Executes the given [block] on the engine thread using appropriate handler.
     * If the [clearAllPending] is true, then all pending tasks for this thread will be cleared.
     * If the [token] is not null, then the callback previously added with this [token] will be removed.
     */
    private inline fun execOnEngineThread(
        clearAllPending: Boolean = false,
        token: Any? = null,
        crossinline block: () -> Unit
    ) {
        if (clearAllPending) {
            engineHandler.removeCallbacksAndMessages(null)
        }

        if (isOnEngineThread()) {
            block()
        } else {
            if (token != null) {
                engineHandler.removeCallbacksAndMessages(token)
                engineHandler.postAtTime({ block() }, token, 0)
            } else {
                engineHandler.post { block() }
            }
        }
    }

    private inline fun execOnEventThread(crossinline block: () -> Unit) {
        if (isOnEventThread()) {
            block()
        } else {
            eventHandler.post { block() }
        }
    }

    /****************************
     ******* INTERNAL API *******
     ***************************/

    /**
     * Just call this method in case the current song was set to null,
     * but it was supposed to be resolved normally.
     */
    private fun resolveUndefinedStateInternal(startPlaying: Boolean) {
        ThreadStrictMode.assertBackground()
        if (currentSongQueue.isEmpty) {
            currentPositionInQueue = -1
            currentSong = null
            resetInternal()
        } else {
            if (currentPositionInQueue < 0) {
                currentPositionInQueue = 0
            } else if (currentPositionInQueue >= currentSongQueue.length) {
                currentPositionInQueue = currentSongQueue.length - 1
            }

            currentSong = currentSongQueue.getItemAt(currentPositionInQueue)
            handleSwitchToSong(currentSong, 0, startPlaying)
        }
    }

    private fun handleSwitchToSong(song: Song?, playbackPosition: Int, startPlaying: Boolean) {
        ThreadStrictMode.assertBackground()

        execOnEventThread {
            resetABInternal()
            observerRegistry.onSongChanged(this, song, currentPositionInQueue)
        }

        if (song != null) {
            engine.runCatching {
                Trace.d(LOG_TAG, "Preparing: [src=${song.source}, startPlating=$startPlaying]")
                isPreparedFlag = false
                isPlayingFlag = startPlaying

                reset()
                setDataSource(song.source)
                //engine.prepareAsync()
                prepare()
                // assumed prepared
                isPreparedFlag = true

                execOnEventThread {
                    observerRegistry.onPrepared(this@PlayerEngine)
                }

                // applying audio fx
                audioFxApplicable.apply(audioSessionId)

                // seeking to the given position
                seekTo(min(playbackPosition, duration))

                if (startPlaying) {
                    startInternal()
                } else {
                    execOnEventThread {
                        observerRegistry.onPlaybackPaused(this@PlayerEngine)
                    }
                }
            }.onFailure { err ->
                Trace.e(LOG_TAG, err)
                isPlayingFlag = false
                execOnEventThread {
                    observerRegistry.onPlaybackPaused(this@PlayerEngine)
                }
            }
        } else {
            engine.runCatching {
                reset()
            }.onFailure { err ->
                Trace.e(LOG_TAG, err)
            }
            execOnEventThread {
                observerRegistry.onPlaybackPaused(this@PlayerEngine)
            }
        }
    }

    private fun resetInternal() {
        ThreadStrictMode.assertBackground()
        synchronized(lock) {
            execOnEventThread {
                resetABInternal()
                observerRegistry.onSongChanged(this, currentSong, currentPositionInQueue)
            }

            Trace.d(LOG_TAG, "Resetting")
            engine.runCatching {
                isPreparedFlag = false
                isPlayingFlag = false
                reset()
            }.onFailure { err ->
                Trace.e(err)
            }

            execOnEventThread {
                observerRegistry.onPlaybackPaused(this)
            }
        }
    }

    private fun startInternal() {
        ThreadStrictMode.assertBackground()
        synchronized(lock) {
            Trace.d(LOG_TAG, "Starting")
            engine.runCatching {
                isPlayingFlag = true
                if (isPreparedFlag) {
                    if (audioFocusHandler.requestAudioFocus()) {
                        start()
                        // dispatch always!
                        execOnEventThread {
                            observerRegistry.onPlaybackStarted(this@PlayerEngine)
                        }
                    }
                }
            }.onFailure { err ->
                Trace.e(LOG_TAG, err)
            }
        }
    }

    private fun pauseInternal() {
        ThreadStrictMode.assertBackground()
        synchronized(lock) {
            Trace.d(LOG_TAG, "Pausing")
            engine.runCatching {
                isPlayingFlag = false
                if (isPreparedFlag) {
                    pause()
                    execOnEventThread {
                        observerRegistry.onPlaybackPaused(this@PlayerEngine)
                    }
                }
            }.onFailure { err ->
                Trace.e(LOG_TAG, err)
            }
        }
    }

    private fun toggleInternal() {
        ThreadStrictMode.assertBackground()
        synchronized(lock) {
            Trace.d(LOG_TAG, "Toggling")
            engine.runCatching {
                val wasPlaying = isPlayingFlag
                isPlayingFlag = !wasPlaying
                if (isPreparedFlag) {
                    if (wasPlaying) {
                        pause()
                        execOnEventThread {
                            observerRegistry.onPlaybackPaused(this@PlayerEngine)
                        }
                    } else if (audioFocusHandler.requestAudioFocus()) {
                        start()
                        execOnEventThread {
                            observerRegistry.onPlaybackStarted(this@PlayerEngine)
                        }
                    }
                }
            }.onFailure { err ->
                Trace.e(LOG_TAG, err)
            }
        }
    }

    private fun skipToPreviousInternal(byUser: Boolean) {
        ThreadStrictMode.assertBackground()
        synchronized(lock) {
            Trace.d(LOG_TAG, "Skipping to previous: [byUser=$byUser]")
            if (!currentSongQueue.isEmpty) {
                if (!byUser && repeatMode == Player.REPEAT_ONE) {
                    handleSwitchToSong(
                            song = currentSong,
                            playbackPosition = 0,
                            startPlaying = isPlayingFlag)
                } else {
                    currentPositionInQueue--
                    val newPositionInQueue = when {
                        currentPositionInQueue < 0 -> currentSongQueue.length - 1
                        else -> currentPositionInQueue
                    }
                    currentPositionInQueue = newPositionInQueue
                    currentSong = if (newPositionInQueue >= 0) {
                        currentSongQueue.getItemAt(newPositionInQueue)
                    } else {
                        null
                    }
                    handleSwitchToSong(
                            song = currentSong,
                            playbackPosition = 0,
                            startPlaying = isPlayingFlag)
                }
            }
        }
    }

    private fun skipToNextInternal(byUser: Boolean) {
        ThreadStrictMode.assertBackground()
        synchronized(lock) {
            Trace.d(LOG_TAG, "Skipping to next: [byUser=$byUser]")
            if (!currentSongQueue.isEmpty) {
                if (!byUser && repeatMode == Player.REPEAT_ONE) {
                    handleSwitchToSong(
                            song = currentSong,
                            playbackPosition = 0,
                            startPlaying = isPlayingFlag)
                } else {
                    currentPositionInQueue++
                    when {
                        currentPositionInQueue >= currentSongQueue.length -> {
                            currentPositionInQueue = 0
                            currentSong = if (currentPositionInQueue >= 0) {
                                currentSongQueue.getItemAt(currentPositionInQueue)
                            } else {
                                null
                            }

                            handleSwitchToSong(
                                    song = currentSong,
                                    playbackPosition = 0,
                                    startPlaying = (isPlayingFlag && byUser) || (isPlayingFlag && repeatMode == Player.REPEAT_PLAYLIST))
                        }
                        else -> {
                            currentSong = if (currentPositionInQueue >= 0) {
                                currentSongQueue.getItemAt(currentPositionInQueue)
                            } else {
                                null
                            }

                            handleSwitchToSong(
                                    song = currentSong,
                                    playbackPosition = 0,
                                    startPlaying = isPlayingFlag)
                        }
                    }
                }
            }
        }
    }

    private fun skipToInternal(position: Int, byUser: Boolean, forceStartPlaying: Boolean) {
        ThreadStrictMode.assertBackground()
        synchronized(lock) {
            if (position == currentPositionInQueue) {
                return
            }

            if (position < 0) {
                Trace.w(LOG_TAG, "Cannot skip to negative position: [position=$position]")
            } else {
                Trace.d(LOG_TAG, "Skipping to: [position=$position, byUser=$byUser]")
                if (position < currentSongQueue.length) {
                    currentPositionInQueue = position
                    currentSong = currentSongQueue.getItemAt(position)
                    handleSwitchToSong(
                            song = currentSong,
                            playbackPosition = 0,
                            startPlaying = isPlayingFlag || forceStartPlaying)
                }
            }
        }
    }

    private fun skipToInternal(song: Song, byUser: Boolean, forceStartPlaying: Boolean) {
        ThreadStrictMode.assertBackground()
        synchronized(lock) {
            Trace.d(LOG_TAG, "Skipping to: [song=$song, byUser=$byUser]")
            val newPositionInQueue = currentSongQueue.indexOf(song)
            if (newPositionInQueue >= 0) {
                currentPositionInQueue = newPositionInQueue
                currentSong = song
                handleSwitchToSong(
                        song = currentSong,
                        playbackPosition = 0,
                        startPlaying = isPlayingFlag || forceStartPlaying)
            }
        }
    }

    /****************************
     ******** PUBLIC API ********
     ***************************/

    override fun registerObserver(observer: PlayerObserver) {
        execOnEventThread {
            observerRegistry.register(observer)
        }
    }

    override fun unregisterObserver(observer: PlayerObserver) {
        execOnEventThread {
            observerRegistry.unregister(observer)
        }
    }

    override fun prepare(queue: SongQueue, song: Song, startPlaying: Boolean) {
        prepare(queue, song, 0, startPlaying)
    }

    override fun prepare(queue: SongQueue, song: Song, playbackPosition: Int, startPlaying: Boolean) {
        execOnEngineThread {
            synchronized(lock) {
                // attach origin first
                originSongQueue = queue

                val newQueue = queue.clone().apply {
                    if (shuffleMode == Player.SHUFFLE_ON) {
                        shuffle(song)
                    }
                }
                currentSongQueue = newQueue

                execOnEventThread {
                    observerRegistry.onQueueChanged(this, newQueue)
                }

                // Queue attached. Now we need to setup current song and its position in the queue
                val positionInQueue = newQueue.indexOf(song)
                currentPositionInQueue = positionInQueue
                currentSong = if (positionInQueue >= 0) song else null

                currentSong.let { song ->
                    if (song == null) {
                        resetInternal()
                    } else {
                        handleSwitchToSong(
                                song = currentSong,
                                playbackPosition = playbackPosition,
                                startPlaying = startPlaying)
                    }
                }

            }
        }
    }

    override fun shutdown() {
        // allow releasing resources on the main thread
        execOnEventThread {
            // remove ALL callbacks in engine and event handlers
            engineHandler.removeCallbacksAndMessages(null)
            eventHandler.removeCallbacksAndMessages(null)

            engine.runCatching {
                release()
            }.onFailure { err ->
                Trace.e(LOG_TAG, err)
            }

            audioFxApplicable.save()

            // the last one we must do
            observerRegistry.onShutdown(this)
            observerRegistry.clear()
        }
    }

    override fun skipToPrevious() {
        execOnEngineThread(clearAllPending = true) {
            skipToPreviousInternal(true)
        }
    }

    override fun skipToNext() {
        execOnEngineThread(clearAllPending = true) {
            skipToNextInternal(true)
        }
    }

    override fun skipTo(position: Int, forceStartPlaying: Boolean) {
        execOnEngineThread(clearAllPending = true, token = tokenSkipTo) {
            skipToInternal(position, true, forceStartPlaying)
        }
    }

    override fun skipTo(song: Song, forceStartPlaying: Boolean) {
        execOnEngineThread(clearAllPending = true, token = tokenSkipTo) {
            skipToInternal(song, true, forceStartPlaying)
        }
    }

    override fun isPrepared() = isPreparedFlag

    override fun isPlaying() = isPlayingFlag

    override fun getAudiSessionId(): Int {
        return engine.runCatching {
            audioSessionId
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }.getOrDefault(0)
    }

    override fun getCurrent(): Song? = currentSong

    override fun getCurrentPositionInQueue(): Int = currentPositionInQueue

    override fun getCurrentQueue(): SongQueue? = currentSongQueue

    override fun getProgress(): Int {
        return engine.runCatching {
            if (isPreparedFlag) currentPosition else 0
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }.getOrDefault(0)
    }

    override fun seekTo(position: Int) {
        engine.runCatching {
            if (isPreparedFlag) {
                seekTo(position)
                execOnEventThread {
                    observerRegistry.onSoughtTo(this@PlayerEngine, position)
                }
            }
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }
    }

    override fun getDuration(): Int {
        return engine.runCatching {
            if (isPreparedFlag) duration else 0
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }.getOrDefault(0)
    }

    override fun start() {
        execOnEngineThread {
            startInternal()
        }
    }

    override fun pause() {
        execOnEngineThread {
            pauseInternal()
        }
    }

    override fun toggle() {
        execOnEngineThread {
            toggleInternal()
        }
    }

    override fun update(song: Song) {
        execOnEngineThread {
            originSongQueue?.let { queue ->
                val index = queue.indexOf(song)
                if (index >= 0) {
                    queue.setItemAt(index, song)
                }
            }
            currentSongQueue.let { queue ->
                val index = queue.indexOf(song)
                if (index >= 0) {
                    queue.setItemAt(index, song)
                }
            }
            if (currentSong?.source == song.source) {
                currentSong = song
                execOnEventThread {
                    observerRegistry.onSongChanged(this, song, currentPositionInQueue)
                }
            }
        }
    }

    override fun remove(position: Int) {
        execOnEngineThread {
            synchronized(lock) {
                val targetSong = currentSongQueue.getItemAt(position)

                originSongQueue?.let { queue ->
                    queue.remove(targetSong)
                }

                currentSongQueue.let { queue ->
                    queue.removeAt(position)
                }

                when {
                    position < currentPositionInQueue -> {
                        currentPositionInQueue--
                    }

                    position == currentPositionInQueue -> {
                        currentSong = null
                        resolveUndefinedStateInternal(isPlayingFlag)
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun removeAll(songs: Collection<Song>) {
        execOnEngineThread {
            synchronized(lock) {
                songs.forEach { song ->
                    val position = songs.indexOf(song)
                    if (position <= currentPositionInQueue) {
                        currentPositionInQueue--
                    }
                    originSongQueue?.remove(song)
                    currentSongQueue.remove(song)
                }

                if (currentSongQueue.isEmpty || songs.contains(currentSong)) {
                    currentSong = null
                    resolveUndefinedStateInternal(isPlayingFlag)
                }
            }
        }
    }

    override fun add(song: Song) {
        addAll(listOf(song))
    }

    override fun addAll(songs: List<Song>) {
        execOnEngineThread {
            synchronized(lock) {
                originSongQueue?.addAll(songs)

                currentSongQueue.addAll(songs)

                if (currentSong == null && !currentSongQueue.isEmpty) {
                    currentPositionInQueue = 0
                    currentSong = currentSongQueue.getItemAt(currentPositionInQueue)

                    handleSwitchToSong(
                            song = currentSong,
                            playbackPosition = 0,
                            startPlaying = false)
                }
            }
        }
    }

    override fun addNext(song: Song) {
        addAllNext(listOf(song))
    }

    override fun addAllNext(songs: List<Song>) {
        execOnEngineThread {
            synchronized(lock) {
                max(0, currentPositionInQueue).let { targetPosition ->
                    originSongQueue?.also { queue ->
                        queue.addAll(targetPosition + 1, songs)
                    }

                    currentSongQueue.addAll(targetPosition + 1, songs)
                }

                if (currentSong == null && !currentSongQueue.isEmpty) {
                    currentPositionInQueue = 0
                    currentSong = currentSongQueue.getItemAt(currentPositionInQueue)

                    handleSwitchToSong(
                            song = currentSong,
                            playbackPosition = 0,
                            startPlaying = false)
                }
            }
        }
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        execOnEngineThread {
            synchronized(lock) {
                originSongQueue?.let { queue ->
                    if (fromPosition >= 0 && toPosition >= 0) {
                        queue.moveItem(fromPosition, toPosition)
                    }
                }

                if (currentPositionInQueue == fromPosition) {
                    currentPositionInQueue = toPosition
                } else if (currentPositionInQueue < fromPosition) {
                    if (currentPositionInQueue < toPosition) {
                        // no changes
                    } else {
                        currentPositionInQueue++
                    }
                } else if (currentPositionInQueue > fromPosition) {
                    if (currentPositionInQueue > toPosition) {
                        // no changes
                    } else {
                        currentPositionInQueue--
                    }
                }

                currentSongQueue.also { songQueue ->
                    if (fromPosition >= 0 && toPosition >= 0) {
                        songQueue.moveItem(fromPosition, toPosition)
                    }
                }
            }
        }
    }

    override fun getAudioFx(): AudioFx = audioFxApplicable

    /********************************
     *********** AB ENGINE **********
     *******************************/

    private fun startABInternal() {
        ThreadStrictMode.assertMain()
        disposableAB?.dispose()
        disposableAB = Completable.fromAction {
            var a = pointA
            var b = pointB
            while (a != null && b != null) {
                try {
                    val pos = getProgress()
                    if (pos < a - 100) {
                        seekTo(a)
                        continue
                    }

                    val sleep = b - pos
                    if (sleep > 0) {
                        Thread.sleep(sleep.toLong())
                    }
                    seekTo(a)

                    a = pointA
                    b = pointB
                } catch (exc: InterruptedException) {
                    break
                }
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    private fun setAInternal(a: Int?) {
        ThreadStrictMode.assertMain()
        if (a == null) {
            this.pointA = null
            return
        }

        var validPointA = a
        if (validPointA > getDuration() - MIN_AB_INTERVAL) {
            validPointA = getDuration() - MIN_AB_INTERVAL
        }

        val b = pointB
        if (b != null && a > b - MIN_AB_INTERVAL) {
            validPointA = b - MIN_AB_INTERVAL
        }
        if (validPointA < 0) {
            validPointA = 0
        }
        this.pointA = validPointA
        startABInternal()
        observerRegistry.onABChanged(this, pointA != null, pointB != null)
    }

    private fun setBInternal(b: Int?) {
        ThreadStrictMode.assertMain()
        if (b == null) {
            this.pointB = null
            return
        }

        var validPointB = b
        if (validPointB < 0) {
            validPointB = 0
        }

        val a = pointA
        if (a != null && b < a + MIN_AB_INTERVAL) {
            validPointB = a + MIN_AB_INTERVAL
        }
        if (validPointB > getDuration()) {
            validPointB = getDuration()
        }
        this.pointB = validPointB
        startABInternal()
        observerRegistry.onABChanged(this, pointA != null, pointB != null)
    }

    private fun resetABInternal() {
        ThreadStrictMode.assertMain()
        pointA = null
        pointB = null
        disposableAB?.dispose()
        disposableAB = null
        observerRegistry.onABChanged(this, pointA != null, pointB != null)
    }

    override fun isAPointed(): Boolean = pointA != null

    override fun isBPointed(): Boolean = pointB != null

    override fun pointA(position: Int) {
        execOnEventThread {
            setAInternal(position)
        }
    }

    override fun pointB(position: Int) {
        execOnEventThread {
            setBInternal(position)
        }
    }

    override fun resetAB() {
        execOnEventThread {
            resetABInternal()
        }
    }

    override fun rewindForward(interval: Int) {
        engine.runCatching {
            val newPosition = currentPosition + interval
            seekTo(newPosition)
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }
    }

    override fun rewindBackward(interval: Int) {
        engine.runCatching {
            val newPosition = currentPosition - interval
            seekTo(newPosition)
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getSpeed(): Float {
        return engine.runCatching {
            playbackParams.speed
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }.getOrDefault(Player.SPEED_NORMAL)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setSpeed(speed: Float) {
        engine.runCatching {
            val params = playbackParams
            params.speed = speed
            playbackParams = params
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getPitch(): Float {
        return engine.runCatching {
            playbackParams.pitch
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }.getOrDefault(Player.SPEED_NORMAL)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setPitch(pitch: Float) {
        engine.runCatching {
            val params = playbackParams
            params.pitch = pitch
            playbackParams = params
        }.onFailure { err ->
            Trace.e(LOG_TAG, err)
        }
    }

    override fun getShuffleMode(): Int = shuffleMode

    override fun setShuffleMode(mode: Int) {
        execOnEngineThread {
            synchronized(lock) {
                shuffleMode = mode
                if (mode == Player.SHUFFLE_OFF) {
                    currentSongQueue.copyItemsFrom(originSongQueue ?: SongQueue.empty())
                } else {
                    currentSongQueue.shuffle(currentSong)
                }
                currentPositionInQueue = currentSongQueue.indexOf(currentSong)

                execOnEventThread {
                    observerRegistry.onShuffleModeChanged(this, mode)
                }
            }
        }
    }

    override fun getRepeatMode(): Int = repeatMode

    override fun setRepeatMode(mode: Int) {
        execOnEngineThread {
            synchronized(lock) {
                repeatMode = mode

                execOnEventThread {
                    observerRegistry.onRepeatModeChanged(this, mode)
                }
            }
        }
    }

}