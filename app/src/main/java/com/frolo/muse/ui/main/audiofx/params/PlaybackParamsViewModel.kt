package com.frolo.muse.ui.main.audiofx.params

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
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
        private val schedulerProvider: SchedulerProvider,
        private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _speed: MutableLiveData<Float> = MutableLiveData()
    val speed: LiveData<Float> = _speed

    private val _pitch: MutableLiveData<Float> = MutableLiveData()
    val pitch: LiveData<Float> = _pitch

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
        override fun onPrepared(player: Player) {
            _speed.value = player.getSpeed()
            _pitch.value = player.getPitch()
        }
    }

    init {
        player.registerObserver(playerObserver)
        _speed.value = player.getSpeed()
        _pitch.value = player.getPitch()
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
                .subscribeFor(schedulerProvider) {
                    _speed.value = Player.SPEED_NORMAL
                    _pitch.value = Player.PITCH_NORMAL
                }
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
    }

}