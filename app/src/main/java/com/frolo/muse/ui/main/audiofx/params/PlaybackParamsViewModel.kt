package com.frolo.muse.ui.main.audiofx.params

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.liveDataOf
import com.frolo.muse.arch.map
import com.frolo.muse.billing.TrialStatus
import com.frolo.muse.engine.AdvancedPlaybackParams
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.PlayerWrapper
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Completable
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.M)
class PlaybackParamsViewModel @Inject constructor(
    private val player: Player,
    private val premiumManager: PremiumManager,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val advancedPlaybackParams: AdvancedPlaybackParams? = lookupAdvancedPlaybackParams(player)

    val isTrialVersion: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            premiumManager.getTrialStatus()
                .observeOn(schedulerProvider.main())
                .subscribeFor { trialStatus ->
                    value = trialStatus == TrialStatus.Activated
                }
        }
    }

    val isPersistenceAvailable: LiveData<Boolean> = liveDataOf(advancedPlaybackParams != null)

    private val _isPersisted = MutableLiveData<Boolean>().apply {
        value = if (advancedPlaybackParams != null) {
            // We count it persisted only if both speed and pitch are persisted
            advancedPlaybackParams.isSpeedPersisted()
                    && advancedPlaybackParams.isPitchPersisted()
        } else {
            false
        }
    }
    val doNotPersistPlaybackParams: LiveData<Boolean> = _isPersisted.map(true) { isPersisted ->
        isPersisted != true
    }

    private val _speed: MutableLiveData<Float> = MutableLiveData()
    val speed: LiveData<Float> get() = _speed

    private val _pitch: MutableLiveData<Float> = MutableLiveData()
    val pitch: LiveData<Float> get() = _pitch

    private val speedPublisher: PublishProcessor<Float> by lazy {
        PublishProcessor.create<Float>().also { publisher ->
            publisher
                    .onBackpressureLatest()
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .onErrorReturnItem(Player.SPEED_NORMAL)
                    .subscribeOn(schedulerProvider.worker())
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { value -> player.setSpeed(value) }
        }
    }

    private val pitchPublisher: PublishProcessor<Float> by lazy {
        PublishProcessor.create<Float>().also { publisher ->
            publisher
                    .onBackpressureLatest()
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .onErrorReturnItem(Player.PITCH_NORMAL)
                    .subscribeOn(schedulerProvider.worker())
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { value -> player.setPitch(value) }
        }
    }

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onPrepared(player: Player, duration: Int, progress: Int) {
            _speed.value = player.getSpeed()
            _pitch.value = player.getPitch()
        }
    }

    init {
        player.registerObserver(playerObserver)
        _speed.value = player.getSpeed()
        _pitch.value = player.getPitch()
    }

    fun onDoNotPersistPlaybackParamsToggled(isChecked: Boolean) {
        val isPersisted: Boolean = !isChecked
        _isPersisted.value = isPersisted
        advancedPlaybackParams?.apply {
            setSpeedPersisted(isPersisted)
            setPitchPersisted(isPersisted)
        }
    }

    fun onSeekSpeed(speed: Float) {
        speedPublisher.onNext(speed)
    }

    fun onSeekPitch(pitch: Float) {
        pitchPublisher.onNext(pitch)
    }

    fun onNormalizeButtonClicked() {
        Completable.fromAction {
            player.setSpeed(Player.SPEED_NORMAL)
            player.setPitch(Player.PITCH_NORMAL)
        }
                .observeOn(schedulerProvider.main())
                .subscribeFor {
                    _speed.value = Player.SPEED_NORMAL
                    _pitch.value = Player.PITCH_NORMAL
                }
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
    }

    companion object {

        private fun lookupAdvancedPlaybackParams(player: Player): AdvancedPlaybackParams? {
            var wrappedPlayer: Player? = player
            do {
                if (wrappedPlayer is AdvancedPlaybackParams) {
                    return wrappedPlayer
                }
                wrappedPlayer = (wrappedPlayer as? PlayerWrapper)?.wrapped
            } while (wrappedPlayer != null)
            return null
        }

    }

}