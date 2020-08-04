package com.frolo.muse.ui.main.player

import android.graphics.Typeface
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.TextSwitcher
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.frolo.mediabutton.PlayButton
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.Logger
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.glide.observe
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.asDurationInMs
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.confirmDeletion
import com.frolo.muse.ui.main.player.carousel.AlbumCardCarouselHelper
import com.frolo.muse.ui.main.player.carousel.AlbumCardAdapter
import com.frolo.muse.ui.main.player.waveform.SoundWaveform
import com.frolo.muse.ui.main.player.waveform.StaticWaveform
import com.frolo.muse.ui.main.showVolumeControl
import com.frolo.muse.views.Anim
import com.frolo.muse.views.sound.WaveformSeekBar
import kotlinx.android.synthetic.main.include_playback_progress.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.include_player_album_art_carousel.*
import kotlinx.android.synthetic.main.include_player_controller_full.*
import kotlinx.android.synthetic.main.include_player_controller.*
import kotlinx.android.synthetic.main.include_player_tool_panel.*


class PlayerFragment: BaseFragment() {

    private class SetViewPagerPosition constructor(
        val pager: ViewPager2,
        val position: Int
    ): Runnable {

        override fun run() {
            pager.setCurrentItem(position, true)
        }

        override fun equals(other: Any?): Boolean {
            return this === other
        }
    }

    private val viewModel: PlayerViewModel by viewModel()

    // The state of album view pager
    private var albumViewPagerState = ViewPager2.SCROLL_STATE_IDLE
    // This flag indicates whether the user is scrolling the album view pager
    private var userScrolledAlbumViewPager = false

    // View pager callbacks
    private var requestTransformCallback: Runnable? = null
    private var setCurrentItemCallback: Runnable? = null

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            Logger.d(LOG_TAG, "Swiped to $position [by_user=$userScrolledAlbumViewPager]")
            if (userScrolledAlbumViewPager) {
                viewModel.onSwipedToPosition(position)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (albumViewPagerState == ViewPager2.SCROLL_STATE_DRAGGING
                    && state == ViewPager2.SCROLL_STATE_SETTLING) {
                userScrolledAlbumViewPager = true
            } else if (albumViewPagerState == ViewPager2.SCROLL_STATE_SETTLING
                    && state == ViewPager2.SCROLL_STATE_IDLE) {
                userScrolledAlbumViewPager = false
            }
            albumViewPagerState = state
        }
    }

    // This flag indicates whether the user is currently tracking the progress bar
    private var isTrackingProgress = false
    private val seekBarListener = object : WaveformSeekBar.OnSeekBarChangeListener {
        override fun onProgressInPercentageChanged(seekBar: WaveformSeekBar, percent: Float, fromUser: Boolean) {
            if (fromUser) {
                viewModel.onSeekProgressToPercent(percent)
            }
        }

        override fun onStartTrackingTouch(seekBar: WaveformSeekBar) {
            isTrackingProgress = true
        }

        override fun onStopTrackingTouch(seekBar: WaveformSeekBar) {
            if (isTrackingProgress) {
                isTrackingProgress = false
                viewModel.onProgressSoughtToPercent(seekBar.progressPercent)
            }
        }

    }

    @get:ColorInt
    private val colorModeOff: Int by lazy {
        StyleUtil.readColorAttrValue(requireContext(), R.attr.iconImageTint)
    }

    @get:ColorInt
    private val colorModeOn: Int by lazy {
        StyleUtil.readColorAttrValue(requireContext(), R.attr.colorAccent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlideAlbumArtHelper.get().observe(this) {
            (vp_album_art.adapter as? AlbumCardAdapter)?.notifyDataSetChanged()

            vp_album_art.removeCallbacks(requestTransformCallback)
            requestTransformCallback = Runnable {
                vp_album_art.requestTransform()
            }
            vp_album_art.post(requestTransformCallback)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // NOTE: Need to set default values to the following variables every time fragment view created.
        albumViewPagerState = ViewPager2.SCROLL_STATE_IDLE
        userScrolledAlbumViewPager = false
        isTrackingProgress = false

        // Intercepting all touches to prevent their processing in the lower view layers
        view.setOnTouchListener { _, _ -> true }

        btn_options_menu.setOnClickListener { v ->
            showOptionsMenu(v)
        }

        vp_album_art.apply {
            AlbumCardCarouselHelper.setup(this)
            adapter = AlbumCardAdapter(requestManager = Glide.with(this@PlayerFragment))
        }

        initTextSwitcher(tsw_song_name, 18f, Typeface.BOLD)
        initTextSwitcher(tsw_artist_name, 12f)

        btn_play.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        btn_skip_to_previous.setOnClickListener {
            viewModel.onSkipToPreviousButtonClicked()
        }

        btn_skip_to_previous.doOnPulseTouchDown {
            viewModel.onSkipToPreviousButtonLongClicked()
        }

        btn_skip_to_next.setOnClickListener {
            viewModel.onSkipToNextButtonClicked()
        }

        btn_skip_to_next.doOnPulseTouchDown {
            viewModel.onSkipToNextButtonLongClicked()
        }

        btn_repeat_mode.setOnClickListener {
            viewModel.onRepeatModeButtonClicked()
        }

        btn_shuffle_mode.setOnClickListener {
            viewModel.onShuffleModeButtonClicked()
        }

        btn_ab.setOnClickListener {
            viewModel.onABButtonClicked()
        }

        btn_like.setOnClickListener {
            viewModel.onLikeClicked()
        }

        btn_volume.setOnClickListener {
            viewModel.onVolumeControlClicked()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
        viewModel.onOpened()
    }

    override fun onStart() {
        super.onStart()
        vp_album_art.registerOnPageChangeCallback(onPageChangeCallback)
        waveform_seek_bar.setOnSeekBarChangeListener(seekBarListener)
    }

    override fun onStop() {
        super.onStop()
        vp_album_art.unregisterOnPageChangeCallback(onPageChangeCallback)
        waveform_seek_bar.setOnSeekBarChangeListener(null)
    }

    override fun onDestroyView() {
        vp_album_art.apply {
            removeCallbacks(requestTransformCallback)
            removeCallbacks(setCurrentItemCallback)
        }

        super.onDestroyView()
    }

    /********************************
     ********* UI UPDATES ***********
     *******************************/

    // Helper method
    private fun initTextSwitcher(
        view: TextSwitcher,
        textSizeInSp: Float,
        typefaceStyle: Int = Typeface.NORMAL
    ) {
        view.setFactory {
            AppCompatTextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp)
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }

                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setTypeface(typeface, typefaceStyle)
            }
        }
        view.setInAnimation(context, R.anim.fade_in)
        view.setOutAnimation(context, R.anim.fade_out)
    }

    private fun updateRepeatIcon(@Player.RepeatMode mode: Int, animate: Boolean) {
        val context = context ?: return
        if (mode == Player.REPEAT_OFF) {
            val drawable = ContextCompat
                    .getDrawable(context, R.drawable.ic_repeat_all_to_one) as AnimatedVectorDrawable
            btn_repeat_mode.setImageDrawable(drawable)
            btn_repeat_mode.setColorFilter(colorModeOff, android.graphics.PorterDuff.Mode.SRC_IN)
            drawable.start()
        } else {
            val repeatOneFlag = mode == Player.REPEAT_ONE
            val drawableId = if (repeatOneFlag)
                R.drawable.ic_repeat_one_to_all
            else R.drawable.ic_repeat_all_to_one

            val drawable = ContextCompat
                    .getDrawable(context, drawableId) as AnimatedVectorDrawable

            btn_repeat_mode.setImageDrawable(drawable)
            btn_repeat_mode.setColorFilter(colorModeOn, android.graphics.PorterDuff.Mode.SRC_IN)
            if (repeatOneFlag) drawable.start()
        }
    }

    private fun updateShuffleIcon(@Player.ShuffleMode mode: Int, animate: Boolean) {
        val enable = mode == Player.SHUFFLE_ON
        val colorFilter = if (enable) colorModeOn else colorModeOff
        btn_shuffle_mode.setImageResource(R.drawable.ic_shuffle)
        btn_shuffle_mode.setColorFilter(colorFilter, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun updateFavouriteIcon(favourite: Boolean, animate: Boolean) {
        if (favourite) {
            btn_like.setImageResource(R.drawable.ic_filled_heart)
            if (animate) Anim.like(btn_like)
        } else {
            btn_like.setImageResource(R.drawable.ic_heart)
            if (animate) Anim.unlike(btn_like)
        }
    }

    private fun updatePlayButton(isPlaying: Boolean) {
        if (isPlaying) {
            btn_play.setState(PlayButton.State.PAUSE, true)
        } else {
            btn_play.setState(PlayButton.State.RESUME, true)
        }
    }

    private fun updateABText(aPointed: Boolean, bPointed: Boolean, animate: Boolean) {
        val ss = SpannableString("A-B")
        val flags = SpannableString.SPAN_INCLUSIVE_INCLUSIVE
        ss.setSpan(ForegroundColorSpan(if (aPointed) colorModeOn else colorModeOff), 0, 1, flags)
        ss.setSpan(ForegroundColorSpan(if (bPointed) colorModeOn else colorModeOff), 1, 3, flags)
        btn_ab.text = ss
    }

    private fun showOptionsMenu(anchorView: View) {
        val popup = PopupMenu(anchorView.context, anchorView)

        popup.inflate(R.menu.fragment_player)

        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.action_edit_song_tags -> viewModel.onEditSongOptionSelected()
                R.id.action_edit_album_cover -> viewModel.onEditAlbumOptionSelected()
                R.id.action_share -> viewModel.onShareOptionSelected()
                R.id.action_cut_ringtone -> viewModel.onRingCutterOptionSelected()
                R.id.action_delete -> viewModel.onDeleteOptionSelected()
                R.id.action_view_album -> viewModel.onViewAlbumOptionSelected()
                R.id.action_view_artist -> viewModel.onViewArtistOptionSelected()
                R.id.action_create_poster -> viewModel.onViewPosterOptionSelected()
                //R.id.action_view_lyrics -> viewModel.onViewLyricsOptionSelected()
            }
            return@setOnMenuItemClickListener true
        }

        popup.show()
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        songDeletedEvent.observeNonNull(owner) {
            toastShortMessage(R.string.deleted)
        }

        isFavourite.observeNonNull(owner) { isFavourite ->
            updateFavouriteIcon(isFavourite, animate = true)
        }

        songQueue.observe(owner) { queue: AudioSourceQueue? ->
            Logger.d(LOG_TAG, "SongQueue changed")
            (vp_album_art.adapter as? AlbumCardAdapter)?.submitQueue(queue)
        }

        invalidateSongQueueEvent.observeNonNull(owner) {
            Logger.d(LOG_TAG, "InvalidateSongQueue event fired")
            vp_album_art.adapter?.notifyDataSetChanged()
        }

        song.observe(owner) { song: Song? ->
            Logger.d(LOG_TAG, "Song changed")
            if (song != null) {
                tsw_song_name.setText(song.getNameString(resources))
                tsw_artist_name.setText(song.getArtistString(resources))
            } else {
                tsw_song_name.setText("")
                tsw_artist_name.setText("")
            }
        }

        albumArtCarouselVisible.observeNonNull(owner) { visible ->
            val transition = Fade().apply {
                addTarget(vp_album_art)
                addTarget(tv_no_songs_in_queue)
                duration = 200L
            }
            TransitionManager.beginDelayedTransition(fl_album_art_carousel_container, transition)
            vp_album_art.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            tv_no_songs_in_queue.visibility = if (visible) View.INVISIBLE else View.VISIBLE
        }

        playerControllersEnabled.observeNonNull(owner) { enabled ->

            // Controllers in the top panel
            btn_options_menu.markControllerEnabled(enabled)
            btn_like.markControllerEnabled(enabled)
            btn_volume.markControllerEnabled(enabled)

            // Waveform progress bar
            waveform_seek_bar.markControllerEnabled(enabled)

            // Main player controllers
            btn_repeat_mode.markControllerEnabled(enabled)
            btn_skip_to_previous.markControllerEnabled(enabled)
            btn_play.markControllerEnabled(enabled)
            btn_shuffle_mode.markControllerEnabled(enabled)
            btn_skip_to_next.markControllerEnabled(enabled)

            // Other controllers
            btn_ab.markControllerEnabled(enabled)
        }

        sound.observe(owner) { sound ->
            if (sound != null) {
                val waveform = SoundWaveform(sound)
                waveform_seek_bar.setWaveform(waveform, true)
            } else {
                val waveform = StaticWaveform(BuildConfig.SOUND_FRAME_GAIN_COUNT, 1, 10)
                waveform_seek_bar.setWaveform(waveform, false)
            }
        }

        songPosition.observeNonNull(owner) { position ->
            Logger.d(LOG_TAG, "Song position changed to $position")

            setCurrentItemCallback?.also { safeCallback ->
                vp_album_art.removeCallbacks(safeCallback)
            }

            // There is an issue with setting current item in ViewPager2:
            // If we set current item to 1 and then in some near future set item to 2
            // Then the final item position will be 1. WTF?
            setCurrentItemCallback = SetViewPagerPosition(vp_album_art, position)

            vp_album_art.postDelayed(setCurrentItemCallback, 150)
        }

        showVolumeControlEvent.observe(owner) {
            context?.showVolumeControl()
        }

        playbackDuration.observeNonNull(owner) { duration ->
            tv_duration.text = duration.asDurationInMs()
        }

        playbackProgress.observeNonNull(owner) { progress ->
            tv_position.text = progress.asDurationInMs()
        }

        progressPercent.observeNonNull(owner) { percent ->
            if (!isTrackingProgress) {
                waveform_seek_bar.setProgressInPercentage(percent)
            }
        }

        isPlaying.observeNonNull(owner) { status: Boolean ->
            updatePlayButton(status)
        }

        abState.observeNonNull(owner) { abState ->
            updateABText(abState.isAPointed, abState.isBPointed, false)
        }

        shuffleMode.observeNonNull(owner) { mode ->
            updateShuffleIcon(mode, true)
        }

        repeatMode.observeNonNull(owner) { mode ->
            updateRepeatIcon(mode, true)
        }

        // Confirmation
        confirmDeletionEvent.observeNonNull(owner) { song ->
            val msg = getString(R.string.confirmation_delete_item)
            activity?.confirmDeletion(msg) {
                checkWritePermissionFor {
                    viewModel.onConfirmedDeletion(song)
                }
            }
        }
    }

    companion object {
        private const val LOG_TAG = "PlayerFragment"

        private fun View.markControllerEnabled(enabled: Boolean) {
            isEnabled = enabled
            alpha = if (enabled) 1.0f else 0.5f
        }

        // Factory
        fun newInstance() = PlayerFragment()
    }

}