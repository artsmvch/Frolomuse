package com.frolo.muse.ui.main.player

import android.content.Context
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
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.player.AudioSource
import com.frolo.player.Player
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.glide.observe
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.asDurationInMs
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.getAlbumEditorOptionText
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.confirmDeletion
import com.frolo.muse.ui.main.player.carousel.AlbumCardCarouselHelper
import com.frolo.muse.ui.main.player.carousel.AlbumCardAdapter
import com.frolo.muse.ui.main.player.waveform.SoundWaveform
import com.frolo.muse.ui.main.player.waveform.StaticWaveform
import com.frolo.muse.ui.main.showVolumeControl
import com.frolo.muse.views.Anim
import com.frolo.waveformseekbar.WaveformSeekBar
import kotlinx.android.synthetic.main.include_playback_progress.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.include_player_album_art_carousel.*
import kotlinx.android.synthetic.main.include_player_controller_full.*
import kotlinx.android.synthetic.main.include_player_controller.*
import kotlinx.android.synthetic.main.include_player_tool_panel.*


class PlayerFragment: BaseFragment() {

    private val viewModel: PlayerViewModel by viewModel()

    // The state of album view pager
    private var albumViewPagerState = ViewPager2.SCROLL_STATE_IDLE
    // This flag indicates whether the user is scrolling the album view pager
    private var userScrolledAlbumViewPager = false

    /**
     * AlbumArt pager state and callbacks.
     */

    // Indicates whether a list is being submitted to the adapter at the moment
    private var isSubmittingList: Boolean = false
    // Indicates the pending position, to which the pager should scroll when a list is submitted
    private var pendingPosition: Int? = null

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
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
    private val waveformCallback = object : WaveformSeekBar.Callback {
        override fun onProgressChanged(seekBar: WaveformSeekBar, percent: Float, fromUser: Boolean) {
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
        StyleUtil.resolveColor(requireContext(), R.attr.iconImageTint)
    }

    @get:ColorInt
    private val colorModeOn: Int by lazy {
        StyleUtil.resolveColor(requireContext(), R.attr.colorAccent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlideAlbumArtHelper.get().observe(this) {
            (vp_album_art.adapter as? AlbumCardAdapter)?.notifyDataSetChanged()
            postRequestPageTransform()
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
        isSubmittingList = false
        pendingPosition = null

        // Intercepting all touches to prevent their processing in the lower view layers
        view.setOnTouchListener { _, _ -> true }

        btn_options_menu.setOnClickListener {
            viewModel.onOptionsMenuClicked()
        }

        vp_album_art.apply {
            AlbumCardCarouselHelper.setup(this)
            adapter = AlbumCardAdapter(requestManager = Glide.with(this@PlayerFragment))
        }

        initTextSwitcher(tsw_song_name, 20f, Typeface.BOLD)
        initTextSwitcher(tsw_artist_name, 13f)

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
        viewModel.onUiCreated()
    }

    override fun onStart() {
        super.onStart()
        vp_album_art.registerOnPageChangeCallback(onPageChangeCallback)
        waveform_seek_bar.setCallback(waveformCallback)
    }

    override fun onStop() {
        super.onStop()
        vp_album_art.unregisterOnPageChangeCallback(onPageChangeCallback)
        waveform_seek_bar.setCallback(null)
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
        val context: Context = view.context
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

    private fun updateFavouriteIcon(favourite: Boolean) {
        if (favourite) {
            btn_like.setImageResource(R.drawable.ic_filled_heart)
        } else {
            btn_like.setImageResource(R.drawable.ic_heart)
        }
    }

    private fun animateFavouriteIcon(favourite: Boolean) {
        if (favourite) Anim.like(btn_like) else Anim.unlike(btn_like)
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

    private fun showOptionsMenu(optionsMenu: PlayerOptionsMenu) {
        val anchorView: View = btn_options_menu
        val context = anchorView.context
        val popup = PopupMenu(context, anchorView)

        popup.inflate(R.menu.fragment_player)

        val menu: Menu = popup.menu
        menu.findItem(R.id.action_edit_album_cover)?.title = context.getAlbumEditorOptionText()
        menu.findItem(R.id.action_view_lyrics)?.isVisible = optionsMenu.isLyricsViewerEnabled

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
                R.id.action_add_to_playlist -> viewModel.onAddToPlaylistOptionSelected()
                R.id.action_view_lyrics -> viewModel.onViewLyricsOptionSelected()
            }
            return@setOnMenuItemClickListener true
        }

        popup.show()
    }

    /**
     * Submits the given [list] to the adapter of the AlbumArt pager.
     * When the submitting is complete, two callbacks are posted:
     * a callback that is responsible for requesting page transform
     * and a callback that is responsible for scrolling the pager to the pending position.
     * Before submitting, if the pending position is null, it is assigned the current pager position.
     * This is to prevent the pager from scrolling automatically when the adapter notifies about changes.
     */
    private fun submitList(list: List<AudioSource>?) {
        val adapter = vp_album_art.adapter as AlbumCardAdapter

        if (pendingPosition == null) {
            pendingPosition = vp_album_art.currentItem
        }

        isSubmittingList = true
        adapter.submitList(list) {
            isSubmittingList = false
            if (view != null) {
                // We do this only if the fragment has a UI
                postRequestPageTransform()
                postScrollToPendingPosition()
            }
        }
    }

    /**
     * Scrolls the AlbumArt pager to the given [position]. The scrolling is async in the sense
     * that [position] is stored as pending scrolling in the near future.
     */
    private fun scrollToPosition(position: Int) {
        pendingPosition = position
        postScrollToPendingPosition()
    }

    /**
     * Posts a callback to scroll to the pending position, if any.
     * The old callback is removed from the AlbumArt pager.
     */
    private fun postScrollToPendingPosition() {
        val callback = Runnable {
            if (!isSubmittingList) {
                pendingPosition?.also { safePosition ->
                    vp_album_art.setCurrentItem(safePosition, true)
                }
                pendingPosition = null
            }
        }

        postOnUi("scroll_to_pending_position", callback)
    }

    /**
     * Posts a callback to request page transform.
     * The old callback is removed from the AlbumArt pager.
     */
    private fun postRequestPageTransform() {
        val callback = Runnable {
            vp_album_art.requestTransform()
        }

        postOnUi("request_page_transform", callback)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        songDeletedEvent.observeNonNull(owner) {
            toastShortMessage(R.string.deleted)
        }

        animateFavouriteEvent.observeNonNull(owner) { isFavourite ->
            animateFavouriteIcon(isFavourite)
        }

        isFavourite.observeNonNull(owner) { isFavourite ->
            updateFavouriteIcon(isFavourite)
        }

        audioSourceList.observe(owner) { list ->
            submitList(list)
        }

        invalidateAudioSourceListEvent.observeNonNull(owner) { list ->
            submitList(list)
        }

        currPosition.observeNonNull(owner) { position ->
            scrollToPosition(position)
        }

        song.observe(owner) { song: Song? ->
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
                waveform_seek_bar.setWaveform(waveform, true)
            }
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
        confirmDeletionEvent.observeNonNull(owner) { confirmation ->
            context?.confirmDeletion(confirmation) { type ->
                checkWritePermissionFor {
                    viewModel.onConfirmedDeletion(confirmation.mediaItem, type)
                }
            }
        }

        // Options menu
        showOptionsMenuEvent.observeNonNull(owner) { optionsMenu ->
            showOptionsMenu(optionsMenu)
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