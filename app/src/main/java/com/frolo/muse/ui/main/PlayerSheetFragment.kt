package com.frolo.muse.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observeNonNull
import com.frolo.core.ui.touch.TouchFlowAware
import com.frolo.muse.R
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.OnBackPressedHandler
import com.frolo.muse.ui.main.player.PlayerFragment
import com.frolo.muse.ui.main.player.current.CurrSongQueueFragment
import com.frolo.ui.StyleUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehaviorSupport
import kotlinx.android.synthetic.main.fragment_player_sheet.*
import kotlin.math.pow


class PlayerSheetFragment :
    BaseFragment(),
    OnBackPressedHandler,
    CurrSongQueueFragment.OnCloseIconClickListener {

    private val mainSheetsStateViewModel by lazy { provideMainSheetStateViewModel() }

    private val innerBottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                handleInnerBottomSheetSlideOffset(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                handleInnerBottomSheetState(newState)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_player_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        WindowInsetsHelper.skipWindowInsets(view)
        WindowInsetsHelper.skipWindowInsets(container_player)
        WindowInsetsHelper.setupWindowInsets(bottom_sheet_current_song_queue) { bottomSheet, insets ->
            bottomSheet.updatePadding(top = insets.systemWindowInsetTop)
            insets
        }

        val behavior = TouchFlowAwareBottomSheetBehavior.from<View>(bottom_sheet_current_song_queue).apply {
            addBottomSheetCallback(innerBottomSheetCallback)
            state = BottomSheetBehavior.STATE_COLLAPSED
            BottomSheetBehaviorSupport.dispatchOnSlide(bottom_sheet_current_song_queue)
            touchFlowCallback = object : TouchFlowAware.TouchFlowCallback {
                override fun onTouchFlowStarted() {
                    mainSheetsStateViewModel.setPlayerSheetDraggable(false)
                }
                override fun onTouchFlowEnded() {
                    BottomSheetBehavior.from(bottom_sheet_current_song_queue).also { behavior ->
                        handleInnerBottomSheetState(behavior.state)
                    }
                }
            }
        }
        val peekHeight = StyleUtils.resolveDimen(view.context, R.attr.actionBarSize).toInt()
        view.doOnLayout {
            behavior.peekHeight = peekHeight
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.container_player, PlayerFragment.newInstance())
            .replace(R.id.container_current_song_queue, CurrSongQueueFragment.newInstance())
            .commitNow()

        layout_hook.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeMainSheetsState(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        BottomSheetBehavior.from(bottom_sheet_current_song_queue).apply {
            removeBottomSheetCallback(innerBottomSheetCallback)
        }
        super.onDestroyView()
    }

    override fun handleOnBackPressed(): Boolean {
        return BottomSheetBehavior.from(bottom_sheet_current_song_queue).run {
            if (state != BottomSheetBehavior.STATE_COLLAPSED) {
                state = BottomSheetBehavior.STATE_COLLAPSED
                true
            } else false
        }
    }

    override fun onCloseIconClick(fragment: CurrSongQueueFragment) {
        BottomSheetBehavior.from(bottom_sheet_current_song_queue).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun observeMainSheetsState(owner: LifecycleOwner) = with(mainSheetsStateViewModel) {
        collapsePlayerSheetEvent.observeNonNull(owner) {
            BottomSheetBehavior.from(bottom_sheet_current_song_queue).state =
                BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun handleInnerBottomSheetSlideOffset(slideOffset: Float) {
        mainSheetsStateViewModel.dispatchQueueSheetSlideOffset(slideOffset)
        // For Goodness sake make sure the view is created
        if (view != null) {
            layout_hook.alpha = (1 - slideOffset * 2).coerceIn(0f, 1f)
            layout_hook.isClickable = slideOffset < 0.3
            container_current_song_queue.alpha = slideOffset
            view_dim_overlay.alpha = 1 - (1 - slideOffset).pow(2)
        }
    }

    private fun handleInnerBottomSheetState(@BottomSheetBehavior.State newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED,
            BottomSheetBehavior.STATE_SETTLING,
            BottomSheetBehavior.STATE_DRAGGING -> {
                mainSheetsStateViewModel.setPlayerSheetDraggable(false)
            }
            BottomSheetBehavior.STATE_COLLAPSED,
            BottomSheetBehavior.STATE_HIDDEN -> {
                mainSheetsStateViewModel.setPlayerSheetDraggable(true)
            }
            else -> mainSheetsStateViewModel.setPlayerSheetDraggable(true)
        }
    }

    companion object {

        fun newInstance(): PlayerSheetFragment = PlayerSheetFragment()

    }

}