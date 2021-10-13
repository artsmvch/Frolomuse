package com.frolo.muse.ui.main.settings.library.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.muse.engine.Player
import com.frolo.muse.interactor.media.get.retainItemsWithSongTypes
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logLibrarySongFilterSaved
import com.frolo.muse.logger.logLibrarySongFilterViewed
import com.frolo.muse.model.media.SongFeatures
import com.frolo.muse.model.media.SongFilter
import com.frolo.muse.model.media.SongType
import com.frolo.muse.repository.LibraryPreferences
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class LibrarySongFilterViewModel @Inject constructor(
    private val preferences: LibraryPreferences,
    private val schedulerProvider: SchedulerProvider,
    private val player: Player,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _songFilter by lazy {
        MutableLiveData<SongFilter>().apply {
            preferences.songFilter
                .observeOn(schedulerProvider.main())
                .subscribeFor { songFilter ->
                    value = songFilter
                }
        }
    }

    private val _songFilterItems = MediatorLiveData<List<SongFilterItem>>().apply {
        addSource(_songFilter) { songFilter ->
            value = createSongFilterItems(songFilter)
        }
    }
    val songFilterItems: LiveData<List<SongFilterItem>> get() = _songFilterItems

    private val _closeEvent = SingleLiveEvent<Unit>()
    val closeEvent: LiveData<Unit> get() = _closeEvent

    private fun createSongFilterItems(filter: SongFilter): List<SongFilterItem> {
        return listOf(
            createSongFilterItem(filter, SongType.MUSIC),
            createSongFilterItem(filter, SongType.PODCAST),
            createSongFilterItem(filter, SongType.RINGTONE),
            createSongFilterItem(filter, SongType.ALARM),
            createSongFilterItem(filter, SongType.NOTIFICATION),
            createSongFilterItem(filter, SongType.AUDIOBOOK)
        ).filter { item ->
            SongFeatures.isSongTypeSupported(item.type)
        }
    }

    private fun createSongFilterItem(filter: SongFilter, targetType: SongType): SongFilterItem {
        return SongFilterItem(targetType, filter.types.contains(targetType))
    }

    fun onFirstCreate() {
        eventLogger.logLibrarySongFilterViewed()
    }

    fun onStart() {
    }

    fun onStop() {
    }

    fun onItemCheckedChange(changedItem: SongFilterItem, isChecked: Boolean) {
        val currentItems = songFilterItems.value ?: return
        val newItems = currentItems.map { item ->
            if (item.type == changedItem.type) {
                item.copy(isChecked = isChecked)
            } else {
                item
            }
        }
        _songFilterItems.value = newItems
    }

    fun onSaveClicked() {
        val currentItems = songFilterItems.value ?: kotlin.run {
            _closeEvent.call()
            return
        }

        val oldFilter = _songFilter.value
        val enabledTypes = currentItems.mapNotNull { item ->
            if (item.isChecked) item.type else null
        }
        if (oldFilter == null || enabledTypes != oldFilter.types) {
            preferences.setSongTypes(enabledTypes)
                .doOnComplete { player.retainItemsWithSongTypes(enabledTypes) }
                .observeOn(schedulerProvider.main())
                .doOnComplete { eventLogger.logLibrarySongFilterSaved() }
                .subscribeFor { _closeEvent.call() }
        } else {
            _closeEvent.call()
        }
    }

}