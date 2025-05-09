package com.frolo.muse.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observeNonNull
import com.frolo.core.ui.touch.TouchFlowAware
import com.frolo.muse.R
import com.frolo.muse.databinding.FragmentPlayerSheetBinding
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.OnBackPressedHandler
import com.frolo.muse.ui.main.player.PlayerFragment
import com.frolo.muse.ui.main.player.current.CurrSongQueueFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehaviorSupport
import kotlin.math.pow


class PlayerSheetFragment :
    BaseFragment(),
    OnBackPressedHandler,
    CurrSongQueueFragment.OnCloseIconClickListener {

    private var _binding: FragmentPlayerSheetBinding? = null
    private val binding: FragmentPlayerSheetBinding get() = _binding!!

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
    ): View? {
        _binding = FragmentPlayerSheetBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        WindowInsetsHelper.setupWindowInsets(view) { _, insets ->
            // Applying insets to the queue layout via margins
            val insetTop = insets.systemWindowInsetTop
            val layoutParams = binding.coordinator.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams
                && layoutParams.topMargin != insetTop) {
                layoutParams.topMargin = insetTop
                binding.coordinator.layoutParams = layoutParams
            }
            return@setupWindowInsets insets
        }
        WindowInsetsHelper.skipWindowInsets(binding.containerPlayer)
        WindowInsetsHelper.skipWindowInsets(binding.viewDimOverlay)

        val behavior = TouchFlowAwareBottomSheetBehavior.from<View>(binding.queueSheetLayout).apply {
            addBottomSheetCallback(innerBottomSheetCallback)
            state = BottomSheetBehavior.STATE_COLLAPSED
            BottomSheetBehaviorSupport.dispatchOnSlide(binding.queueSheetLayout)
            touchFlowCallback = object : TouchFlowAware.TouchFlowCallback {
                override fun onTouchFlowStarted() {
                    mainSheetsStateViewModel.setPlayerSheetDraggable(false)
                }
                override fun onTouchFlowEnded() {
                    BottomSheetBehavior.from(binding.queueSheetLayout).also { behavior ->
                        handleInnerBottomSheetState(behavior.state)
                    }
                }
            }
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.container_player, PlayerFragment.newInstance())
            .replace(R.id.container_current_song_queue, CurrSongQueueFragment.newInstance())
            .commitNow()

        binding.layoutHook.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeMainSheetsState(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        BottomSheetBehavior.from(binding.queueSheetLayout).apply {
            removeBottomSheetCallback(innerBottomSheetCallback)
        }
        super.onDestroyView()
        _binding = null
    }

    override fun handleOnBackPressed(): Boolean {
        return BottomSheetBehavior.from(binding.queueSheetLayout).run {
            if (state != BottomSheetBehavior.STATE_COLLAPSED) {
                state = BottomSheetBehavior.STATE_COLLAPSED
                true
            } else false
        }
    }

    override fun onCloseIconClick(fragment: CurrSongQueueFragment) {
        BottomSheetBehavior.from(binding.queueSheetLayout).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun observeMainSheetsState(owner: LifecycleOwner) = with(mainSheetsStateViewModel) {
        collapsePlayerSheetEvent.observeNonNull(owner) {
            BottomSheetBehavior.from(binding.queueSheetLayout).state =
                BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun handleInnerBottomSheetSlideOffset(slideOffset: Float) {
        mainSheetsStateViewModel.dispatchQueueSheetSlideOffset(slideOffset)
        // For Goodness sake make sure the view is created
        if (view != null) {
            binding.layoutHook.alpha = (1 - slideOffset * 2).coerceIn(0f, 1f)
            binding.layoutHook.isClickable = slideOffset < 0.3
            binding.containerCurrentSongQueue.alpha = slideOffset
            binding.viewDimOverlay.alpha = 1 - (1 - slideOffset).pow(2)
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