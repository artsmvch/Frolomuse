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
import com.frolo.ui.Screen
import com.frolo.ui.StyleUtils
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.music.model.Song
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.getNameString
import com.frolo.muse.views.text.FitSingleLineTextView
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

            val maxTextSizeInPx = Screen.sp(context, 15f)

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
            val colorOnPrimarySurface = StyleUtils.resolveColor(context, R.attr.colorOnPrimarySurface)
            backgroundProgressBarColor = ColorUtils.setAlphaComponent(colorOnPrimarySurface, (0.2f * 255).toInt())
            progressBarColor = colorOnPrimarySurface
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
        viewModel.onUiCreated()
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        currentSong.observe(owner) { song: Song? ->
            if (song != null) {
                tsw_song_name.setText(song.getNameString(resources))
            } else {
                tsw_song_name.setText("")
            }
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