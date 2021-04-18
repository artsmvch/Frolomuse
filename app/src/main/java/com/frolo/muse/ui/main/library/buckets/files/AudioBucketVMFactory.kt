package com.frolo.muse.ui.main.library.buckets.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.engine.Player
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.ExploreMediaBucketUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.MediaBucket
import com.frolo.muse.model.media.MediaFile
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AudioBucketVMFactory constructor(
    appComponent: AppComponent,
    bucketArg: MediaBucket
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var player: Player
    @Inject
    internal lateinit var permissionChecker: PermissionChecker
    /*assisted inject*/
    internal lateinit var exploreMediaBucketUseCase: ExploreMediaBucketUseCase
    @Inject
    internal lateinit var getMediaMenuUseCase: GetMediaMenuUseCase<MediaFile>
    @Inject
    internal lateinit var clickMediaUseCase: ClickMediaUseCase<MediaFile>
    @Inject
    internal lateinit var playMediaUseCase: PlayMediaUseCase<MediaFile>
    @Inject
    internal lateinit var shareMediaUseCase: ShareMediaUseCase<MediaFile>
    @Inject
    internal lateinit var deleteMediaUseCase: DeleteMediaUseCase<MediaFile>
    @Inject
    internal lateinit var getIsFavouriteUseCase: GetIsFavouriteUseCase<MediaFile>
    @Inject
    internal lateinit var changeFavouriteUseCase: ChangeFavouriteUseCase<MediaFile>
    @Inject
    internal lateinit var createSongShortcutUseCase: CreateShortcutUseCase<MediaFile>
    @Inject
    internal lateinit var createPlaylistShortcutUseCase: CreateShortcutUseCase<Playlist>
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var preferences: Preferences
    @Inject
    internal lateinit var navigator: Navigator
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
        exploreMediaBucketUseCase = appComponent
                .provideExploreMediaBucketUseCaseFactory()
                .create(bucketArg)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AudioBucketViewModel(
            player,
            permissionChecker,
            exploreMediaBucketUseCase,
            getMediaMenuUseCase,
            clickMediaUseCase,
            playMediaUseCase,
            shareMediaUseCase,
            deleteMediaUseCase,
            getIsFavouriteUseCase,
            changeFavouriteUseCase,
            createSongShortcutUseCase,
            schedulerProvider,
            navigator,
            eventLogger
        ) as T
    }

}