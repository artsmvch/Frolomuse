package com.frolo.muse.ui.main.player.mini

import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.mediabutton.PlayButton
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.glide.makeRequest
import com.frolo.muse.model.media.Song
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
            setFactory {
                AppCompatTextView(context).apply {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    }

                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }
            }
            setInAnimation(context, R.anim.fade_in)
            setOutAnimation(context, R.anim.fade_out)
        }

        btn_play.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        currentSong.observe(owner) { song: Song? ->
            if (song != null) {
                tsw_song_name.setCurrentText(song.title)
                loadArt(song)
            }
        }

        isPlaying.observeNonNull(owner) { isPlaying ->
            val state: PlayButton.State =
                if (isPlaying) PlayButton.State.PAUSE
                else PlayButton.State.RESUME

            btn_play.setState(state, true)
        }
    }

    private fun loadArt(song: Song) {
        Glide.with(this)
            .makeRequest(song.albumId)
            .error(R.drawable.ic_framed_music_note_48dp)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade(200))
            .into(imv_art)
    }

}