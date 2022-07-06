package com.frolo.muse.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.frolo.arch.support.distinctUntilChanged


internal interface SlideState {
    val playerSheetSlideOffset: Float
    val queueSheetSlideOffset: Float
}

internal interface MainSheetsStateViewModel {
    val isDimmed: LiveData<Boolean>
    val slideState: LiveData<SlideState>

    fun dispatchScreenChanged()
    fun dispatchPlayerSheetSlideOffset(slideOffset: Float)
    fun dispatchQueueSheetSlideOffset(slideOffset: Float)
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
        queueSheetSlideOffset = 0f
    )

    private val _isDimmed = MutableLiveData<Boolean>(false)
    override val isDimmed: LiveData<Boolean> get() = _isDimmed.distinctUntilChanged()

    private val _slideState = MutableLiveData<SlideState>(slideStateImpl)
    override val slideState: LiveData<SlideState> get() = _slideState

    override fun dispatchScreenChanged() {
        _slideState.value = slideStateImpl
    }

    override fun dispatchPlayerSheetSlideOffset(slideOffset: Float) {
        val oldValue = slideStateImpl.playerSheetSlideOffset
        slideStateImpl.playerSheetSlideOffset = slideOffset
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

    private data class SlideStateImpl(
        override var playerSheetSlideOffset: Float,
        override var queueSheetSlideOffset: Float
    ) : SlideState
}