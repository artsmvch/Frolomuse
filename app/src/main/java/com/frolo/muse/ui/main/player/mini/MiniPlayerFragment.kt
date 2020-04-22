package com.frolo.muse.ui.main.player.mini

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.LifecycleOwner
import com.frolo.mediabutton.PlayButton
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Song
import com.frolo.muse.sp2px
import com.frolo.muse.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_mini_player.*


class MiniPlayerFragment : BaseFragment() {

    private val viewModel: MiniPlayerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_mini_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(tsw_song_name) {

            val maxTextSizeInPx = 15f.sp2px(context).toInt()

            setFactory {
                FitSingleLineTextView(context).apply {
                    gravity = Gravity.START or Gravity.TOP
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.START or Gravity.TOP
                    }

                    includeFontPadding = false

                    setMaxTextSize(maxTextSizeInPx)
                }
            }
            setInAnimation(context, R.anim.fade_in)
            setOutAnimation(context, R.anim.fade_out)
        }

        btn_play.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        pb_progress.apply {
            val colorOnPrimary = StyleUtil.readColorAttrValue(context, R.attr.colorOnPrimary)
            backgroundProgressBarColor = ColorUtils.setAlphaComponent(colorOnPrimary, (0.2f * 255).toInt())
            progressBarColor = colorOnPrimary
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        currentSong.observe(owner) { song: Song? ->
            tsw_song_name.setText(song?.title)
        }

        playerControllersEnabled.observeNonNull(owner) { enabled ->
            btn_play.apply {
                isEnabled = enabled
                alpha = if (enabled) 1.0f else 0.35f
            }
        }

        isPlaying.observeNonNull(owner) { isPlaying ->
            val state: PlayButton.State =
                if (isPlaying) PlayButton.State.PAUSE
                else PlayButton.State.RESUME

            btn_play.setState(state, true)
        }

        maxProgress.observeNonNull(owner) { max ->
            pb_progress.progressMax = max.toFloat()
        }

        progress.observeNonNull(owner) { progress ->
            pb_progress.progress = progress.toFloat()
        }
    }

}