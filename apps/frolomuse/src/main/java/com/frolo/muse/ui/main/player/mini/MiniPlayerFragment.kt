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
import com.frolo.muse.databinding.FragmentMiniPlayerBinding
import com.frolo.music.model.Song
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.getNameString
import com.frolo.muse.views.text.FitSingleLineTextView


class MiniPlayerFragment : BaseFragment() {
    
    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding: FragmentMiniPlayerBinding get() = _binding!!

    private val viewModel: MiniPlayerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMiniPlayerBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.tswSongName) {
            val maxTextSizeInPx = Screen.sp(context, 16.5f)
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

        binding.btnPlay.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        binding.pbProgress.apply {
            val colorOnPrimarySurface = StyleUtils.resolveColor(context,
                com.google.android.material.R.attr.colorOnPrimarySurface)
            backgroundProgressBarColor = ColorUtils.setAlphaComponent(colorOnPrimarySurface, (0.2f * 255).toInt())
            progressBarColor = colorOnPrimarySurface
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
        viewModel.onUiCreated()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        currentSong.observe(owner) { song: Song? ->
            if (song != null) {
                binding.tswSongName.setText(song.getNameString(resources))
            } else {
                binding.tswSongName.setText("")
            }
        }

        playerControllersEnabled.observeNonNull(owner) { enabled ->
            binding.btnPlay.apply {
                isEnabled = enabled
                alpha = if (enabled) 1.0f else 0.35f
            }
        }

        isPlaying.observeNonNull(owner) { isPlaying ->
            val state: PlayButton.State =
                if (isPlaying) PlayButton.State.PAUSE
                else PlayButton.State.RESUME

            binding.btnPlay.setState(state, true)
        }

        maxProgress.observeNonNull(owner) { max ->
            binding.pbProgress.progressMax = max.toFloat()
        }

        progress.observeNonNull(owner) { progress ->
            binding.pbProgress.progress = progress.toFloat()
        }
    }

}