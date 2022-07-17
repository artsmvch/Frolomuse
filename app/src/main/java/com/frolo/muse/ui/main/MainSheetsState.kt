package com.frolo.muse.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.frolo.arch.support.EventLiveData
import com.frolo.arch.support.call
import com.frolo.arch.support.distinctUntilChanged
import com.frolo.arch.support.map


internal interface SlideState {
    val playerSheetSlideOffset: Float
    val isPlayerSheetUnderStatusBar: Boolean
    val queueSheetSlideOffset: Float
}

internal interface MainSheetsStateViewModel {
    val isDimmed: LiveData<Boolean>
    val slideState: LiveData<SlideState>

    val isPlayerSheetVisible: LiveData<Boolean>
    val isPlayerSheetDraggable: LiveData<Boolean>
    val collapsePlayerSheetEvent: LiveData<Unit>

    fun dispatchScreenChanged()
    fun dispatchPlayerSheetSlideOffset(slideOffset: Float, isUnderStatusBar: Boolean)
    fun dispatchQueueSheetSlideOffset(slideOffset: Float)

    fun setPlayerSheetDraggable(draggable: Boolean)
    fun collapsePlayerSheet()
}

internal fun Fragment.provideMainSheetStateViewModel(): MainSheetsStateViewModel {
    return ViewModelProviders.of(requireActivity()).get(MainSheetsStateViewModelImpl::class.java)
}

internal fun FragmentActivity.provideMainSheetStateViewModel(): MainSheetsStateViewModel {
    return ViewModelProviders.of(this).get(MainSheetsStateViewModelImpl::class.java)
}

// TODO: cannot make it private because of ViewModelProvider$NewInstanceFactory
internal class MainSheetsStateViewModelImpl: ViewModel(), MainSheetsStateViewModel {

    private val dimmingThreshold: Float = 0.6f

    // For re-use
    private val slideStateImpl = SlideStateImpl(
        playerSheetSlideOffset = 0f,
        queueSheetSlideOffset = 0f,
        isPlayerSheetUnderStatusBar = false
    )

    private val _isDimmed = MutableLiveData<Boolean>(false)
    override val isDimmed: LiveData<Boolean> get() = _isDimmed.distinctUntilChanged()

    private val _slideState = MutableLiveData<SlideState>(slideStateImpl)
    override val slideState: LiveData<SlideState> get() = _slideState

    override val isPlayerSheetVisible: LiveData<Boolean> =
        slideState.map(initialValue = false) { slideState ->
            slideState != null && slideState.playerSheetSlideOffset > 0.95f
        }
        .distinctUntilChanged()

    private val _isPlayerSheetDraggable = MutableLiveData<Boolean>(true)
    override val isPlayerSheetDraggable: LiveData<Boolean> = _isPlayerSheetDraggable

    private val _collapsePlayerSheetEvent = EventLiveData<Unit>()
    override val collapsePlayerSheetEvent: LiveData<Unit> = _collapsePlayerSheetEvent

    override fun dispatchScreenChanged() {
        _slideState.value = slideStateImpl
    }

    override fun dispatchPlayerSheetSlideOffset(slideOffset: Float, isUnderStatusBar: Boolean) {
        val oldValue = slideStateImpl.playerSheetSlideOffset
        slideStateImpl.playerSheetSlideOffset = slideOffset
        slideStateImpl.isPlayerSheetUnderStatusBar = isUnderStatusBar
        _slideState.value = slideStateImpl
        if (slideOffset >= dimmingThreshold
            && (oldValue < dimmingThreshold)) {
            _isDimmed.value = true
        } else if (slideOffset < dimmingThreshold
            && (oldValue >= dimmingThreshold)) {
            _isDimmed.value = false
        }
    }

    override fun dispatchQueueSheetSlideOffset(slideOffset: Float) {
        slideStateImpl.queueSheetSlideOffset = slideOffset
        _slideState.value = slideStateImpl
    }

    override fun setPlayerSheetDraggable(draggable: Boolean) {
        _isPlayerSheetDraggable.value = draggable
    }

    override fun collapsePlayerSheet() {
        _collapsePlayerSheetEvent.call()
    }

    private data class SlideStateImpl(
        override var playerSheetSlideOffset: Float,
        override var isPlayerSheetUnderStatusBar: Boolean,
        override var queueSheetSlideOffset: Float
    ) : SlideState
}