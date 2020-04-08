package com.frolo.muse.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.frolo.muse.R
import com.frolo.muse.ui.base.BackPressHandler
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.main.player.PlayerFragment
import com.frolo.muse.ui.main.player.TouchFrameLayout
import com.frolo.muse.ui.main.player.current.CurrSongQueueFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_player_sheet.*
import kotlinx.android.synthetic.main.include_message.*


class PlayerSheetFragment : BaseFragment(), BackPressHandler {

    private val playerSheetCallback: PlayerSheetCallback?
        get() = activity as? PlayerSheetCallback

    // BottomSheet: CurrentSongQueue
    private val bottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                fl_hook.alpha = 1 - slideOffset
                fl_hook.isClickable = slideOffset < 0.4
                container_current_song_queue.alpha = slideOffset
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    playerSheetCallback?.setPlayerSheetDraggable(false)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    playerSheetCallback?.setPlayerSheetDraggable(true)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_player_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        layout_player_placeholder.apply {
            setOnTouchListener { _, _ -> true }
            tv_message.text = getString(R.string.current_playlist_is_empty)
        }

        val behavior = BottomSheetBehavior.from(bottom_sheet_current_song_queue)
            .apply {
                addBottomSheetCallback(bottomSheetCallback)
                state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetCallback.onSlide(bottom_sheet_current_song_queue, 0.0f)
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

        fl_hook.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
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

    companion object {

        fun newInstance(): PlayerSheetFragment = PlayerSheetFragment()

    }

}