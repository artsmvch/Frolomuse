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
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextSwitcher
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
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
import com.frolo.muse.databinding.FragmentPlayerBinding
import com.frolo.muse.ui.asDurationInMs
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.getAlbumEditorOptionText
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.*
import com.frolo.muse.ui.main.player.waveform.SoundWaveform
import com.frolo.muse.ui.main.player.waveform.StaticWaveform
import com.frolo.music.model.Song
import com.frolo.player.Player
import com.frolo.ui.ColorUtils2
import com.frolo.waveformseekbar.WaveformSeekBar


class PlayerFragment: BaseFragment() {
    
    private var _binding: FragmentPlayerBinding? = null
    private val binding: FragmentPlayerBinding get() = _binding!!

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
            binding.carousel.invalidateData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isTrackingProgress = false

        // Intercepting all touches to prevent their processing in the lower view layers
        view.setOnTouchListener { _, _ -> true }

        if (mainScreenProperties.ignoreArtBackgroundForStatusBar) {
            handleArtBackgroundColorChange(mainScreenProperties.colorPlayerSurface)
        } else {
            binding.carouselBackground.onSurfaceColorChangeListener =
                CarouselBackgroundView.OnSurfaceColorChangeListener { color, isIntermediate ->
                    handleArtBackgroundColorChange(color)
                }
        }
        binding.carousel.setPlaceholderText(R.string.no_songs_in_queue)

        WindowInsetsHelper.setupWindowInsets(view) { _, insets ->
            if (mainScreenProperties.isLandscape) {
                view.updatePadding(top = insets.systemWindowInsetTop)
            } else {
                binding.playerToolbar.root.updatePadding(top = insets.systemWindowInsetTop)
            }
            return@setupWindowInsets insets
        }

        binding.playerToolbar.btnClose.setOnClickListener {
            mainSheetsStateViewModel.collapsePlayerSheet()
        }

        binding.playerToolbar.btnOptionsMenu.setOnClickListener {
            viewModel.onOptionsMenuClicked()
        }

        initTextSwitcher(binding.tswSongName, textSizeInSp = 26f, Typeface.BOLD)
        initTextSwitcher(binding.tswArtistName, textSizeInSp = 13f, Typeface.NORMAL)

        binding.includePlayerControllerFull.buttons.btnPlay.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        binding.includePlayerControllerFull.buttons.btnSkipToPrevious.setOnClickListener {
            viewModel.onSkipToPreviousButtonClicked()
        }

        binding.includePlayerControllerFull.buttons.btnSkipToPrevious.doOnPulseTouchDown {
            viewModel.onSkipToPreviousButtonLongClicked()
        }

        binding.includePlayerControllerFull.buttons.btnSkipToNext.setOnClickListener {
            viewModel.onSkipToNextButtonClicked()
        }

        binding.includePlayerControllerFull.buttons.btnSkipToNext.doOnPulseTouchDown {
            viewModel.onSkipToNextButtonLongClicked()
        }

        binding.includePlayerControllerFull.buttons.btnRepeatMode.setOnClickListener {
            viewModel.onRepeatModeButtonClicked()
        }

        binding.includePlayerControllerFull.buttons.btnShuffleMode.setOnClickListener {
            viewModel.onShuffleModeButtonClicked()
        }

        binding.includePlayerControllerFull.btnAb.setOnClickListener {
            viewModel.onABButtonClicked()
        }

        binding.btnLike.setOnClickListener {
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
        binding.carousel.registerCallback(carouselCallback)
        binding.includePlayerControllerFull.progress.waveformSeekBar.setCallback(waveformCallback)
    }

    override fun onStop() {
        super.onStop()
        binding.carousel.unregisterCallback(carouselCallback)
        binding.includePlayerControllerFull.progress.waveformSeekBar.setCallback(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.carouselBackground.onSurfaceColorChangeListener = null
        _binding = null
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
            binding.includePlayerControllerFull.buttons.btnRepeatMode.setImageDrawable(drawable)
            binding.includePlayerControllerFull.buttons.btnRepeatMode.setColorFilter(mainScreenProperties.colorModeOff,
                android.graphics.PorterDuff.Mode.SRC_IN)
            drawable.start()
        } else {
            val repeatOneFlag = mode == Player.REPEAT_ONE
            val drawableId = if (repeatOneFlag)
                R.drawable.ic_repeat_one_to_all
            else R.drawable.ic_repeat_all_to_one

            val drawable = ContextCompat
                    .getDrawable(context, drawableId) as AnimatedVectorDrawable

            binding.includePlayerControllerFull.buttons.btnRepeatMode.setImageDrawable(drawable)
            binding.includePlayerControllerFull.buttons.btnRepeatMode.setColorFilter(mainScreenProperties.colorModeOn,
                android.graphics.PorterDuff.Mode.SRC_IN)
            if (repeatOneFlag) drawable.start()
        }
    }

    private fun updateShuffleIcon(@Player.ShuffleMode mode: Int, animate: Boolean) {
        val enable = mode == Player.SHUFFLE_ON
        val colorFilter = mainScreenProperties.getModeColor(enable)
        binding.includePlayerControllerFull.buttons.btnShuffleMode.setImageResource(R.drawable.ic_shuffle)
        binding.includePlayerControllerFull.buttons.btnShuffleMode.setColorFilter(colorFilter, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun updateFavouriteIcon(favourite: Boolean) {
        if (favourite) {
            binding.btnLike.setImageResource(R.drawable.ic_filled_heart)
            binding.btnLike.setBackgroundResource(R.drawable.bg_like_button_default)
        } else {
            binding.btnLike.setImageResource(R.drawable.ic_heart)
            binding.btnLike.setBackgroundResource(R.drawable.bg_like_button_default)
        }
    }

    private fun animateFavouriteIcon(favourite: Boolean) {
        AppAnimations.animateLike(binding.btnLike)
    }

    private fun updatePlayButton(isPlaying: Boolean) {
        if (isPlaying) {
            binding.includePlayerControllerFull.buttons.btnPlay.setState(PlayButton.State.PAUSE, true)
        } else {
            binding.includePlayerControllerFull.buttons.btnPlay.setState(PlayButton.State.RESUME, true)
        }
    }

    private fun updateABText(aPointed: Boolean, bPointed: Boolean, animate: Boolean) {
        val text = SpannableString("A-B")
        val flags = SpannableString.SPAN_INCLUSIVE_INCLUSIVE
        text.setSpan(ForegroundColorSpan(mainScreenProperties.getModeColor(aPointed)), 0, 1, flags)
        text.setSpan(ForegroundColorSpan(mainScreenProperties.getModeColor(bPointed)), 1, 3, flags)
        binding.includePlayerControllerFull.btnAb.text = text
    }

    private fun handleArtBackgroundColorChange(@ColorInt color: Int) {
        val tint = mainScreenProperties.getPlayerToolbarElementColor(color)
        binding.playerToolbar.btnClose.imageTintList = tint
        binding.playerToolbar.btnOptionsMenu.imageTintList = tint
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
            binding.carouselBackground.surfaceColor ?: mainScreenProperties.colorPlayerSurface
        }
    }

    private fun updateArtBackground(palette: Palette?) {
        val targetColor = mainScreenProperties.extractArtBackgroundColor(palette)
        binding.carouselBackground.setSurfaceColor(targetColor, animated = true)
        binding.carousel.setPlaceholderTextColor(mainScreenProperties.getPlaceholderTextColor(targetColor))
    }

    private fun showOptionsMenu(optionsMenu: PlayerOptionsMenu) {
        val anchorView: View = binding.playerToolbar.btnOptionsMenu
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
            binding.carousel.submitList(list)
        }

        invalidateAudioSourceListEvent.observeNonNull(owner) { list ->
            binding.carousel.submitList(list)
        }

        currPosition.observeNonNull(owner) { position ->
            binding.carousel.setCurrentPosition(position)
        }

        song.observe(owner) { song: Song? ->
            if (song != null) {
                binding.tswSongName.setText(song.getNameString(resources))
                binding.tswArtistName.setText(song.getArtistString(resources))
            } else {
                binding.tswSongName.setText("")
                binding.tswArtistName.setText("")
            }
        }

        playerControllersEnabled.observeNonNull(owner) { enabled ->

            // Controllers in the top panel
//            binding.playerToolbar.btnOptionsMenu.markControllerEnabled(enabled)
            binding.btnLike.markControllerEnabled(enabled)
//            btn_volume.markControllerEnabled(enabled)

            // Waveform progress bar
            binding.includePlayerControllerFull.progress.waveformSeekBar.markControllerEnabled(enabled)

            // Main player controllers
            binding.includePlayerControllerFull.buttons.btnRepeatMode.markControllerEnabled(enabled)
            binding.includePlayerControllerFull.buttons.btnSkipToPrevious.markControllerEnabled(enabled)
            binding.includePlayerControllerFull.buttons.btnPlay.markControllerEnabled(enabled)
            binding.includePlayerControllerFull.buttons.btnShuffleMode.markControllerEnabled(enabled)
            binding.includePlayerControllerFull.buttons.btnSkipToNext.markControllerEnabled(enabled)

            // Other controllers
            binding.includePlayerControllerFull.btnAb.markControllerEnabled(enabled)
        }

        soundWave.observe(owner) { soundWave ->
            if (soundWave != null) {
                val waveform = SoundWaveform(soundWave)
                binding.includePlayerControllerFull.progress.waveformSeekBar.setWaveform(waveform, true)
            } else {
                val waveform = StaticWaveform(BuildConfig.SOUND_WAVEFORM_LENGTH, 1, 10)
                binding.includePlayerControllerFull.progress.waveformSeekBar.setWaveform(waveform, true)
            }
        }

        palette.observe(owner) { palette ->
            updateArtBackground(palette)
        }

        showVolumeControlEvent.observe(owner) {
            context?.showVolumeControl()
        }

        playbackDuration.observeNonNull(owner) { duration ->
            binding.includePlayerControllerFull.progress.tvDuration.text = duration.asDurationInMs()
        }

        playbackProgress.observeNonNull(owner) { progress ->
            binding.includePlayerControllerFull.progress.tvPosition.text = progress.asDurationInMs()
        }

        progressPercent.observeNonNull(owner) { percent ->
            if (!isTrackingProgress) {
                binding.includePlayerControllerFull.progress.waveformSeekBar.setProgressInPercentage(percent)
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
            animateToolbarElementToVisibility(binding.playerToolbar.btnClose, isVisible)
            animateToolbarElementToVisibility(binding.playerToolbar.btnOptionsMenu, isVisible)
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