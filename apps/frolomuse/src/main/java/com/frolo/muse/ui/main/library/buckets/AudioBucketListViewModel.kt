package com.frolo.muse.ui.main.library.buckets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.arch.support.SingleLiveEvent
import com.frolo.arch.support.call
import com.frolo.arch.support.combine
import com.frolo.muse.interactor.media.get.GetAudioBucketsUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.MediaBucket
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class AudioBucketListViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val getAudioBucketsUseCase: GetAudioBucketsUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    // Internal state
    private var _isFetchingData: Boolean = false
    private var _hasFetchedData: Boolean = false

    // Live data
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _buckets = MutableLiveData<List<MediaBucket>>()
    val buckets: LiveData<List<MediaBucket>> get() = _buckets

    val placeholderVisible: LiveData<Boolean> by lazy {
        combine(isLoading, buckets) { loading: Boolean?, list: List<*>? ->
            list.isNullOrEmpty() && loading != true
        }
    }

    private val _requestRESPermissionEvent = SingleLiveEvent<Unit>()
    //val requestRESPermissionEvent: LiveData<Unit> get() = _requestRESPermissionEvent

    private val _openBucketEvent = SingleLiveEvent<MediaBucket>()
    val openBucketEvent: LiveData<MediaBucket> get() = _openBucketEvent

    private fun doFetch() {
        // TODO: need to check the read files permission
        if (!permissionChecker.isReadAudioPermissionGranted) {
            _requestRESPermissionEvent.call()
            return
        }

        getAudioBucketsUseCase.getBuckets()
            .observeOn(schedulerProvider.main())
            .doOnSubscribe {
                _isFetchingData = true
                if (buckets.value.isNullOrEmpty()) {
                    _isLoading.value = true
                }
            }
            .doOnNext {
                _hasFetchedData = true
                _isLoading.value = false
            }
            .doOnError { error ->
                if (error is SecurityException) {
                    _requestRESPermissionEvent.call()
                    if (_buckets.value == null) {
                        _buckets.value = emptyList()
                    }
                }
                _isLoading.value = false
            }
            .doFinally {
                _isFetchingData = false
            }
            .subscribeFor(key = "do_fetch") { list ->
                _buckets.value = list
            }
    }

    private fun maybeDoFetch() {
        if (!_isFetchingData && !_hasFetchedData) {
            doFetch()
        }
    }

    fun onActive() {
        maybeDoFetch()
    }

    fun onRESPermissionGranted() {
        maybeDoFetch()
    }

    fun onBucketClicked(bucket: MediaBucket) {
        _openBucketEvent.value = bucket
    }

    fun onBucketLongClicked(bucket: MediaBucket) {
        _openBucketEvent.value = bucket
    }

}