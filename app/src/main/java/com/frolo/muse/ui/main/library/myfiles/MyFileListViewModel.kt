package com.frolo.muse.ui.main.library.myfiles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.get.GetAllMyFilesUseCase
import com.frolo.muse.interactor.media.hidden.AddToHiddenUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.MyFile
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription
import javax.inject.Inject


class MyFileListViewModel @Inject constructor(
        private val player: Player,
        private val getAllMyFilesUseCase: GetAllMyFilesUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<MyFile>,
        clickMediaUseCase: ClickMediaUseCase<MyFile>,
        playMediaUseCase: PlayMediaUseCase<MyFile>,
        shareMediaUseCase: ShareMediaUseCase<MyFile>,
        deleteMediaUseCase: DeleteMediaUseCase<MyFile>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<MyFile>,
        private val setFolderAsDefaultUseCase: SetFolderAsDefaultUseCase,
        private val addToHiddenUseCase: AddToHiddenUseCase,
        private val schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<MyFile>(
        getAllMyFilesUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        changeFavouriteUseCase,
        schedulerProvider,
        navigator,
        eventLogger) {

    private var browserSubscription: Subscription? = null

    private var playingPositionDisposable: Disposable? = null

    private val deletedItemsObserver = Observer<List<MyFile>> { deletedItems ->
        mediaList.value?.also { safeMediaList ->
            submitMediaList(safeMediaList - deletedItems)
        }
    }

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onSongChanged(player: Player, song: Song?, positionInQueue: Int) {
            detectPlayingPosition(mediaList.value, song)
        }
        override fun onPlaybackStarted(player: Player) {
            _isPlaying.value = true
        }
        override fun onPlaybackPaused(player: Player) {
            _isPlaying.value = false
        }
    }

    private val _isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _playingPosition: MutableLiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(mediaList) { list ->
            detectPlayingPosition(list, player.getCurrent())
        }
    }
    val playingPosition: LiveData<Int> = _playingPosition

    private val _root: MutableLiveData<MyFile> by lazy {
        MutableLiveData<MyFile>().apply {
            getAllMyFilesUseCase.getRoot()
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { root ->
                        value = root
                    }
        }
    }
    val root: LiveData<MyFile> get() = _root

    private val _isCollectingSongs: MutableLiveData<Boolean> = MutableLiveData()
    val isCollectingSongs: LiveData<Boolean> = _isCollectingSongs

    private val _showFolderSetDefaultMessageEvent = SingleLiveEvent<Unit>()
    val showFolderSetDefaultMessageEvent: LiveData<Unit> = _showFolderSetDefaultMessageEvent

    private val _showFolderAddedToHiddenMessageEvent = SingleLiveEvent<Unit>()
    val showFolderAddedToHiddenMessageEvent: LiveData<Unit> = _showFolderAddedToHiddenMessageEvent

    init {
        player.registerObserver(playerObserver)
        _isPlaying.value = player.isPlaying()
        deletedItemsEvent.observeForever(deletedItemsObserver)
    }

    private fun detectPlayingPosition(myFileList: List<MyFile>?, song: Song?) {
        Single.fromCallable {
            myFileList?.indexOfFirst { myFile ->
                myFile.isSongFile && myFile.javaFile.absolutePath == song?.source
            } ?: -1
        }
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.main())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSuccess(position: Int) {
                        _playingPosition.value = position
                    }
                    override fun onSubscribe(d: Disposable) {
                        playingPositionDisposable?.dispose()
                        playingPositionDisposable = d
                    }
                    override fun onError(e: Throwable) {
                        logError(e)
                    }
                })
    }

    private fun browse(myFile: MyFile) {
        getAllMyFilesUseCase.browse(myFile)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { s ->
                    browserSubscription?.cancel()
                    browserSubscription = s
                    setLoading(true)
                    _root.value = myFile
                }
                .doFinally { setLoading(false) }
                .subscribeFor { list -> submitMediaList(list) }
    }

    fun onRootClicked() {
        getAllMyFilesUseCase.goBack().let { result ->
            if (result.canGoBack) {
                result.toBrowse?.let { safeMyFile ->
                    browse(safeMyFile)
                }
            }
        }
    }

    override fun setAsDefault(item: MyFile) {
        setFolderAsDefaultUseCase.setFolderAsDefault(item)
                .observeOn(schedulerProvider.main())
                .subscribeFor {
                    _showFolderSetDefaultMessageEvent.call()
                }
    }

    override fun addToHidden(item: MyFile) {
        addToHiddenUseCase.addToHidden(item)
                .observeOn(schedulerProvider.main())
                .subscribeFor {
                    _showFolderAddedToHiddenMessageEvent.call()
                }
    }

    override fun handleItemClick(item: MyFile) {
        when {
            item.isDirectory -> browse(item)
            else -> super.handleItemClick(item)
        }
    }

    override fun handleBackPress() {
        val result = getAllMyFilesUseCase.goBack()
        if (result.canGoBack) {
            result.toBrowse?.let { safeMyFile ->
                browse(safeMyFile)
            }
        } else {
            super.handleBackPress()
        }
    }

    override fun onCleared() {
        browserSubscription?.cancel()
        player.unregisterObserver(playerObserver)
        playingPositionDisposable?.dispose()
        deletedItemsEvent.removeObserver(deletedItemsObserver)
        super.onCleared()
    }

}