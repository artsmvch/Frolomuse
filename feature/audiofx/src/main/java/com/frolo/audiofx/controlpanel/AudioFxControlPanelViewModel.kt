package com.frolo.audiofx.controlpanel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.*
import com.frolo.audiofx.AudioFx2Feature
import com.frolo.audiofx2.*
import com.frolo.rx.KeyedDisposableContainer
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class AudioFxControlPanelViewModel(
    application: Application
): AndroidViewModel(application) {
    private val keyedDisposables = KeyedDisposableContainer<String>()

    val audioFx2: LiveData<AudioFx2> by lazy {
        MutableLiveData(AudioFx2Feature.getAudioFx2())
    }

    val equalizer: LiveData<Equalizer> =
        Transformations.map(audioFx2) { audioFx2 -> audioFx2?.equalizer}

    private val _equalizerPresets = MediatorLiveData<List<EqualizerPreset>>().apply {
        addSource(equalizer) { it?.also(::loadPresets) }
    }
    val equalizerPresets: LiveData<List<EqualizerPreset>> get() = _equalizerPresets

    val bassBoost: LiveData<BassBoost> by lazy {
        Transformations.map(audioFx2) { audioFx2 -> audioFx2?.bassBoost }
    }
    val virtualizer: LiveData<Virtualizer> by lazy {
        Transformations.map(audioFx2) { audioFx2 -> audioFx2?.virtualizer }
    }
    val loudness: LiveData<Loudness> by lazy {
        Transformations.map(audioFx2) { audioFx2 -> audioFx2?.loudness }
    }

    @SuppressLint("CheckResult")
    private fun loadPresets(equalizer: Equalizer) {
        Single.fromCallable { equalizer.getAllPresets() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { disposable ->
                keyedDisposables.add("load_presets", disposable)
            }
            .subscribe { presets ->
                _equalizerPresets.value = presets
            }
    }

    fun onPresetClick(preset: EqualizerPreset) {
        equalizer.value?.also { safeEqualizer ->
            safeEqualizer.usePreset(preset)
        }
    }

    fun onRemovePresetClick(preset: EqualizerPreset) {
        equalizer.value?.also { safeEqualizer ->
            removePreset(safeEqualizer, preset)
        }
    }

    private fun removePreset(equalizer: Equalizer, preset: EqualizerPreset) {
        Completable.fromAction { equalizer.deletePreset(preset) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { loadPresets(equalizer) }
            .doOnSubscribe { disposable ->
                keyedDisposables.add("remove_preset", disposable)
            }
            .subscribe()
    }

    override fun onCleared() {
        super.onCleared()
        keyedDisposables.dispose()
    }
}