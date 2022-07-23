package com.frolo.muse.ui.main.player

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextSwitcher
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.core.graphics.Palette
import com.frolo.core.ui.animations.AppAnimations
import com.frolo.core.ui.carousel.CarouselBackgroundView
import com.frolo.core.ui.carousel.ICarouselView
import com.frolo.core.ui.glide.GlideAlbumArtHelper
import com.frolo.core.ui.glide.observe
import com.frolo.core.ui.systembars.SystemBarsControlOwner
import com.frolo.core.ui.systembars.SystemBarsController
import com.frolo.core.ui.systembars.defaultSystemBarsHost
import com.frolo.mediabutton.PlayButton
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.ui.asDurationInMs
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.getAlbumEditorOptionText
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.MainScreenProperties
import com.frolo.muse.ui.main.confirmDeletion
import com.frolo.muse.ui.main.player.waveform.SoundWaveform
import com.frolo.muse.ui.main.player.waveform.StaticWaveform
import com.frolo.muse.ui.main.provideMainSheetStateViewModel
import com.frolo.muse.ui.main.showVolumeControl
import com.frolo.music.model.Song
import com.frolo.player.Player
import com.frolo.ui.ColorUtils2
import com.frolo.ui.Screen
import com.frolo.ui.StyleUtils
import com.frolo.waveformseekbar.WaveformSeekBar
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.include_playback_progress.*
import kotlinx.android.synthetic.main.include_player_controller.*
import kotlinx.android.synthetic.main.include_player_controller_full.*
import kotlinx.android.synthetic.main.include_player_toolbar.*


class PlayerFragment: BaseFragment() {

    private val viewModel: PlayerViewModel by viewModel()
    private val mainSheetsStateViewModel by lazy { provideMainSheetStateViewModel() }

    private val systemBarsControlOwner = object : SystemBarsControlOwner {
        override fun onSystemBarsControlObtained(controller: SystemBarsController) {
            updateSystemBars(controller = controller)
        }
    }

    private val carouselCallback = object : ICarouselView.CarouselCallback {
        override fun onPositionSelected(position: Int, byUser: Boolean) {
            if (byUser) {
                viewModel.onSwipedToPosition(position)
            }
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

    private val mainScreenProperties by lazy { MainScreenProperties(requireActivity()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlideAlbumArtHelper.get().observe(this) {
            carousel.invalidateData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isTrackingProgress = false

        // Intercepting all touches to prevent their processing in the lower view layers
        view.setOnTouchListener { _, _ -> true }

        if (mainScreenProperties.ignoreArtBackgroundForStatusBar) {
            handleArtBackgroundColorChange(mainScreenProperties.colorPlayerSurface)
        } else {
            carousel_background.onSurfaceColorChangeListener =
                CarouselBackgroundView.OnSurfaceColorChangeListener { color, isIntermediate ->
                    handleArtBackgroundColorChange(color)
                }
        }
        carousel.setPlaceholderText(R.string.no_songs_in_queue)

        view.fitsSystemWindows = true
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            if (mainScreenProperties.isLandscape) {
                view.updatePadding(top = insets.systemWindowInsetTop)
            } else {
                player_toolbar.updatePadding(top = insets.systemWindowInsetTop)
            }
            insets
        }

        btn_close.setOnClickListener {
            mainSheetsStateViewModel.collapsePlayerSheet()
        }

        btn_options_menu.setOnClickListener {
            viewModel.onOptionsMenuClicked()
        }

        initTextSwitcher(tsw_song_name, textSizeInSp = 26f, Typeface.BOLD)
        initTextSwitcher(tsw_artist_name, textSizeInSp = 13f, Typeface.NORMAL)

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
        observeMainSheetsState(viewLifecycleOwner)
        viewModel.onUiCreated()
    }

    override fun onStart() {
        super.onStart()
        carousel.registerCallback(carouselCallback)
        waveform_seek_bar.setCallback(waveformCallback)
    }

    override fun onStop() {
        super.onStop()
        carousel.unregisterCallback(carouselCallback)
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
                includeFontPadding = false
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setTypeface(typeface, typefaceStyle)
                setTextColor(mainScreenProperties.colorPlayerText)
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
            btn_repeat_mode.setColorFilter(mainScreenProperties.colorModeOff,
                android.graphics.PorterDuff.Mode.SRC_IN)
            drawable.start()
        } else {
            val repeatOneFlag = mode == Player.REPEAT_ONE
            val drawableId = if (repeatOneFlag)
                R.drawable.ic_repeat_one_to_all
            else R.drawable.ic_repeat_all_to_one

            val drawable = ContextCompat
                    .getDrawable(context, drawableId) as AnimatedVectorDrawable

            btn_repeat_mode.setImageDrawable(drawable)
            btn_repeat_mode.setColorFilter(mainScreenProperties.colorModeOn,
                android.graphics.PorterDuff.Mode.SRC_IN)
            if (repeatOneFlag) drawable.start()
        }
    }

    private fun updateShuffleIcon(@Player.ShuffleMode mode: Int, animate: Boolean) {
        val enable = mode == Player.SHUFFLE_ON
        val colorFilter = mainScreenProperties.getModeColor(enable)
        btn_shuffle_mode.setImageResource(R.drawable.ic_shuffle)
        btn_shuffle_mode.setColorFilter(colorFilter, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun updateFavouriteIcon(favourite: Boolean) {
        if (favourite) {
            btn_like.setImageResource(R.drawable.ic_filled_heart)
            btn_like.setBackgroundResource(R.drawable.bg_like_button_default)
        } else {
            btn_like.setImageResource(R.drawable.ic_heart)
            btn_like.setBackgroundResource(R.drawable.bg_like_button_default)
        }
    }

    private fun animateFavouriteIcon(favourite: Boolean) {
        AppAnimations.animateLike(btn_like)
    }

    private fun updatePlayButton(isPlaying: Boolean) {
        if (isPlaying) {
            btn_play.setState(PlayButton.State.PAUSE, true)
        } else {
            btn_play.setState(PlayButton.State.RESUME, true)
        }
    }

    private fun updateABText(aPointed: Boolean, bPointed: Boolean, animate: Boolean) {
        val text = SpannableString("A-B")
        val flags = SpannableString.SPAN_INCLUSIVE_INCLUSIVE
        text.setSpan(ForegroundColorSpan(mainScreenProperties.getModeColor(aPointed)), 0, 1, flags)
        text.setSpan(ForegroundColorSpan(mainScreenProperties.getModeColor(bPointed)), 1, 3, flags)
        btn_ab.text = text
    }

    private fun handleArtBackgroundColorChange(@ColorInt color: Int) {
        val tint = mainScreenProperties.getPlayerToolbarElementColor(color)
        btn_close.imageTintList = tint
        btn_options_menu.imageTintList = tint
        updateSystemBars(artBackgroundColor = color)
    }

    private fun updateSystemBars(
        @ColorInt artBackgroundColor: Int = retrieveArtBackgroundColor(),
        controller: SystemBarsController? =
            defaultSystemBarsHost?.getSystemBarsController(systemBarsControlOwner)
    ) {
        val isStatusBarLight = ColorUtils2.isLight(color = artBackgroundColor)
        controller?.setStatusBarAppearanceLight(isStatusBarLight)
    }

    @ColorInt
    private fun retrieveArtBackgroundColor(): Int {
        return if (mainScreenProperties.ignoreArtBackgroundForStatusBar) {
            mainScreenProperties.colorPlayerSurface
        } else {
            carousel_background?.surfaceColor ?: mainScreenProperties.colorPlayerSurface
        }
    }

    private fun animateArtBackgroundColor(palette: Palette?) {
        val targetColor = mainScreenProperties.extractArtBackgroundColor(palette)
        carousel_background?.setSurfaceColor(targetColor, animated = true)
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
            carousel.submitList(list)
        }

        invalidateAudioSourceListEvent.observeNonNull(owner) { list ->
            carousel.submitList(list)
        }

        currPosition.observeNonNull(owner) { position ->
            carousel.setCurrentPosition(position)
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

        playerControllersEnabled.observeNonNull(owner) { enabled ->

            // Controllers in the top panel
//            btn_options_menu.markControllerEnabled(enabled)
            btn_like.markControllerEnabled(enabled)
//            btn_volume.markControllerEnabled(enabled)

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

        soundWave.observe(owner) { soundWave ->
            if (soundWave != null) {
                val waveform = SoundWaveform(soundWave)
                waveform_seek_bar.setWaveform(waveform, true)
            } else {
                val waveform = StaticWaveform(BuildConfig.SOUND_WAVEFORM_LENGTH, 1, 10)
                waveform_seek_bar.setWaveform(waveform, true)
            }
        }

        palette.observe(owner) { palette ->
            animateArtBackgroundColor(palette)
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

    private fun observeMainSheetsState(owner: LifecycleOwner) = with(mainSheetsStateViewModel) {
        isPlayerSheetVisible.observeNonNull(owner) { isVisible ->
            animateToolbarElementToVisibility(btn_close, isVisible)
            animateToolbarElementToVisibility(btn_options_menu, isVisible)
        }

        slideState.observeNonNull(owner) { slideState ->
            // We want to control appearance of the status bar if the screen is under it
            if (slideState.isPlayerSheetUnderStatusBar) {
                defaultSystemBarsHost?.obtainSystemBarsControl(systemBarsControlOwner)
            } else{
                defaultSystemBarsHost?.abandonSystemBarsControl(systemBarsControlOwner)
            }
        }
    }

    // NOTE: do not animate to GONE visibility as this breaks sheet dragging
    private fun animateToolbarElementToVisibility(view: View, isVisible: Boolean) {
        val targetVisibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        val initialScale: Float
        val initialAlpha: Float
        val targetScale: Float
        val targetAlpha: Float
        if (isVisible) {
            initialScale = 0.4f
            initialAlpha = 0.0f
            targetScale = 1f
            targetAlpha = 1f
        } else {
            initialScale = 1f
            initialAlpha = 1f
            targetScale = 0.4f
            targetAlpha = 0.0f
        }
        view.visibility = View.VISIBLE
        view.scaleX = initialScale
        view.scaleY = initialScale
        view.alpha = initialAlpha
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .alpha(targetAlpha)
            .setDuration(150L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { view.visibility = targetVisibility }
            .start()
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