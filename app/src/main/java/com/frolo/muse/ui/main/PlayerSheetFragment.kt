package com.frolo.muse.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.ui.base.BackPressHandler
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.main.player.PlayerFragment
import com.frolo.muse.ui.main.player.TouchFrameLayout
import com.frolo.muse.ui.main.player.current.CurrSongQueueFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_player_sheet.*
import kotlin.math.pow


class PlayerSheetFragment : BaseFragment(),
        BackPressHandler,
        CurrSongQueueFragment.OnCloseIconClickListener {

    private val playerSheetCallback: PlayerSheetCallback?
        get() = activity as? PlayerSheetCallback

    // BottomSheet: CurrentSongQueue
    private val bottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // For Goodness sake make sure the view is created
                view ?: return

                layout_hook.alpha = (1 - slideOffset * 2).coerceIn(0f, 1f)
                layout_hook.isClickable = slideOffset < 0.3

                container_current_song_queue.alpha = slideOffset
                view_dim_overlay.alpha = 1 - (1 - slideOffset).pow(2)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    playerSheetCallback?.setPlayerSheetDraggable(false)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    playerSheetCallback?.setPlayerSheetDraggable(true)
                }
            }
        }

    // This is used to remember the slide offset of this sheet
    // so we can properly configure widgets when onViewCreated is called.
    private var currSheetSlideOffset: Float? = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_player_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val behavior = BottomSheetBehavior.from(bottom_sheet_current_song_queue)
            .apply {
                addBottomSheetCallback(bottomSheetCallback)
                state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetCallback.onSlide(bottom_sheet_current_song_queue, 0.0f)
            }

        view.doOnLayout {
            behavior.peekHeight =
                StyleUtil.resolveDimen(view.context, R.attr.actionBarSize).toInt()
        }

        bottom_sheet_current_song_queue.touchCallback =
            object : TouchFrameLayout.TouchCallback {
                override fun onTouchDown() {
                    playerSheetCallback?.setPlayerSheetDraggable(false)
                }

                override fun onTouchRelease() {
                    playerSheetCallback?.setPlayerSheetDraggable(false)
                }
            }

        childFragmentManager.beginTransaction()
            .replace(R.id.container_player, PlayerFragment.newInstance())
            .replace(R.id.container_current_song_queue, CurrSongQueueFragment.newInstance())
            .commit()

        layout_hook.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        imv_close.setOnClickListener {
            // First of all, we need to collapse the inner bottom sheet to avoid the case
            // when the player sheet is collapsed itself, but the inner bottom sheet is not.
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            playerSheetCallback?.requestCollapse()
        }

        currSheetSlideOffset?.also { safeSlideOffset ->
            onSlideOffset(safeSlideOffset)
        }
    }

    override fun onDestroyView() {
        BottomSheetBehavior.from(bottom_sheet_current_song_queue)
            .apply {
                removeBottomSheetCallback(bottomSheetCallback)
            }

        super.onDestroyView()
    }

    override fun onBackPress(): Boolean {
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

    fun onSlideOffset(offset: Float) {
        currSheetSlideOffset = offset
        view?.also {
            imv_close?.alpha = (offset * 4 - 3).coerceIn(0f, 1f)
        }
    }

    companion object {

        fun newInstance(): PlayerSheetFragment = PlayerSheetFragment()

    }

}