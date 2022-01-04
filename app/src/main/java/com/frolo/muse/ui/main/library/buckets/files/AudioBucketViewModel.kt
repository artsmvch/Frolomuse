package com.frolo.muse.ui.main.library.buckets.files

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.ExploreMediaBucketUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.MediaBucket
import com.frolo.muse.model.media.MediaFile
import com.frolo.muse.router.AppRouter
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel


class AudioBucketViewModel constructor(
    private val player: Player,
    permissionChecker: PermissionChecker,
    private val exploreMediaBucketUseCase: ExploreMediaBucketUseCase,
    getMediaMenuUseCase: GetMediaMenuUseCase<MediaFile>,
    clickMediaUseCase: ClickMediaUseCase<MediaFile>,
    playMediaUseCase: PlayMediaUseCase<MediaFile>,
    shareMediaUseCase: ShareMediaUseCase<MediaFile>,
    deleteMediaUseCase: DeleteMediaUseCase<MediaFile>,
    getIsFavouriteUseCase: GetIsFavouriteUseCase<MediaFile>,
    changeFavouriteUseCase: ChangeFavouriteUseCase<MediaFile>,
    createShortcutUseCase: CreateShortcutUseCase<MediaFile>,
    private val schedulerProvider: SchedulerProvider,
    appRouter: AppRouter,
    eventLogger: EventLogger
): AbsMediaCollectionViewModel<MediaFile>(
    permissionChecker,
    exploreMediaBucketUseCase,
    getMediaMenuUseCase,
    clickMediaUseCase,
    playMediaUseCase,
    shareMediaUseCase,
    deleteMediaUseCase,
    getIsFavouriteUseCase,
    changeFavouriteUseCase,
    createShortcutUseCase,
    schedulerProvider,
    appRouter,
    eventLogger
) {

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
            detectPlayingPosition(mediaList.value, item)
        }

        override fun onPlaybackStarted(player: Player) {
            _isPlaying.value = true
        }

        override fun onPlaybackPaused(player: Player) {
            _isPlaying.value = false
        }
    }

    val bucket: LiveData<MediaBucket> by lazy {
        MutableLiveData<MediaBucket>().apply {
            exploreMediaBucketUseCase.getBucket()
                .observeOn(schedulerProvider.main())
                .subscribeFor { value = it }
        }
    }

    // Player state
    private val _isPlaying = MutableLiveData<Boolean>(player.isPlaying())
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _playingPosition = MediatorLiveData<Int>().apply {
        addSource(mediaList) { list ->
            detectPlayingPosition(list, player.getCurrent())
        }
    }
    val playingPosition: LiveData<Int> get() = _playingPosition

    init {
        player.registerObserver(playerObserver)
    }

    private fun detectPlayingPosition(list: List<MediaFile>?, currentAudioSource: AudioSource?) {
        exploreMediaBucketUseCase.detectPlayingPosition(list, currentAudioSource)
            .observeOn(schedulerProvider.main())
            .subscribeFor(key = "detect_playing_position") { position ->
                _playingPosition.value = position
            }
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
    }

}