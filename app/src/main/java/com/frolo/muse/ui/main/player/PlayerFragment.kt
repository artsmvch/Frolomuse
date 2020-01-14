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
import android.widget.SeekBar
import android.widget.TextSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.frolo.mediabutton.PlayButton
import com.frolo.muse.R
import com.frolo.muse.Trace
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SongQueue
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.asDurationInMs
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.AlbumArtUpdateHandler
import com.frolo.muse.ui.main.confirmDeletion
import com.frolo.muse.ui.main.showVolumeControl
import com.frolo.muse.views.Anim
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.include_message.*
import kotlinx.android.synthetic.main.include_playback_progress.*
import kotlinx.android.synthetic.main.include_player.*
import kotlinx.android.synthetic.main.include_player_controller.*
import kotlinx.android.synthetic.main.include_player_panel.*
import kotlinx.android.synthetic.main.include_player_toolbar.*


class PlayerFragment: BaseFragment() {

    companion object {
        private const val LOG_TAG = "PlayerFragment"

        // Factory
        fun newInstance() = PlayerFragment()
    }

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

    private lateinit var colorProvider: ColorProvider

    private val viewModel: PlayerViewModel by viewModel()

    // UI variables
    private var previousAlbumViewPagerState = ViewPager2.SCROLL_STATE_IDLE
    // indicates if the user is scrolling (or scrolled) the album view pager
    private var userScrolledAlbumViewPager = false

    // View pager callbacks
    private var requestTransformCallback: Runnable? = null
    private var setCurrentItemCallback: Runnable? = null

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            Trace.d(LOG_TAG, "Swiped to $position [by_user=$userScrolledAlbumViewPager]")
            if (userScrolledAlbumViewPager) {
                viewModel.onSwipedToPosition(position)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (previousAlbumViewPagerState == ViewPager2.SCROLL_STATE_DRAGGING
                    && state == ViewPager2.SCROLL_STATE_SETTLING) {
                userScrolledAlbumViewPager = true
            } else if (previousAlbumViewPagerState == ViewPager2.SCROLL_STATE_SETTLING
                    && state == ViewPager2.SCROLL_STATE_IDLE) {
                userScrolledAlbumViewPager = false
            }
            previousAlbumViewPagerState = state
        }
    }

    // indicates if the user is currently tracking the progress bar
    private var isTrackingProgress = false
    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, byUser: Boolean) {
            if (byUser) { // by user
                viewModel.onSeekProgressTo(seekBar.progress)
            }
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) {
            isTrackingProgress = true
        }
        override fun onStopTrackingTouch(seekBar: SeekBar) {
            if (isTrackingProgress) {
                isTrackingProgress = false
                viewModel.onProgressSoughtTo(seekBar.progress)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        colorProvider = ColorProvider(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        AlbumArtUpdateHandler.attach(this) { _, _ ->
            (vp_album_art.adapter as? SongAdapter)?.notifyDataSetChanged()

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
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
        checkReadPermissionFor {
            viewModel.onOpened()
        }
    }

    override fun onStart() {
        super.onStart()
        vp_album_art.registerOnPageChangeCallback(onPageChangeCallback)
        sb_progress.setOnSeekBarChangeListener(seekBarListener)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_player, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
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
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        vp_album_art.unregisterOnPageChangeCallback(onPageChangeCallback)
        sb_progress.setOnSeekBarChangeListener(null)
    }

    override fun onDestroyView() {
        vp_album_art.apply {
            removeCallbacks(requestTransformCallback)
            removeCallbacks(setCurrentItemCallback)
        }
        super.onDestroyView()
    }

    private fun initUI() {
        (activity as AppCompatActivity?)?.apply { 
            setSupportActionBar(inc_toolbar as Toolbar)
            title = ""
        }

        // NOTE: Need to set default values to the following variables every time fragment view created.
        previousAlbumViewPagerState = ViewPager2.SCROLL_STATE_IDLE
        userScrolledAlbumViewPager = false
        isTrackingProgress = false

        vp_album_art.apply {
            CardViewPagerLayoutListener.setup(this)
            // make the previews visible
            offscreenPageLimit = 1
            // do NOT clip the children, because otherwise the previews are not visible
            clipChildren = false
            // do not clip to padding so the previews will be visible
            clipToPadding = false
            // disable over scroll because the position of this effect is wrong according to padding
            overScrollMode = ViewPager2.OVER_SCROLL_NEVER
            // the line above doesn't work, but the following should
            (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            // setup the adapter
            adapter = SongAdapter(Glide.with(this@PlayerFragment))
        }

        // show overlay with appropriate message if current song is null
        layout_player_placeholder.apply {
            setOnClickListener { /*stub*/ }
            tv_message.text = getString(R.string.current_playlist_is_empty)
        }

        initTextSwitcher(tsw_song_name, 18f, Typeface.DEFAULT_BOLD)
        initTextSwitcher(tsw_artist_name, 12f)
        btn_play.setOnClickListener { viewModel.onPlayButtonClicked() }
        btn_skip_to_previous.setOnClickListener { viewModel.onSkipToPreviousButtonClicked() }
        btn_skip_to_previous.setOnTouchListener(PulsingTouchDownListener(500, 750) {
            viewModel.onSkipToPreviousButtonLongClicked()
        })
        btn_skip_to_next.setOnClickListener { viewModel.onSkipToNextButtonClicked() }
        btn_skip_to_next.setOnTouchListener(PulsingTouchDownListener(500, 750) {
            viewModel.onSkipToNextButtonLongClicked()
        })
        btn_repeat_mode.setOnClickListener { viewModel.onRepeatModeButtonClicked() }
        btn_shuffle_mode.setOnClickListener { viewModel.onShuffleModeButtonClicked() }
        btn_ab.setOnClickListener { viewModel.onABButtonClicked() }
        btn_view_playlist.setOnClickListener { viewModel.onViewCurrentPlayingOptionSelected() }
        btn_like.setOnClickListener { viewModel.onLikeClicked() }
        btn_volume.setOnClickListener { viewModel.onVolumeControlClicked() }
    }

    /********************************
     ********* UI UPDATES ***********
     *******************************/

    // Helper method
    private fun initTextSwitcher(
            view: TextSwitcher,
            textSizeInSp: Float,
            typeface: Typeface = Typeface.DEFAULT) {
        view.setFactory {
            AppCompatTextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp)
                gravity = Gravity.START
                //textView.setTypeface(MyApplication.getTypeface());
                layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT).apply { gravity = Gravity.CENTER }

                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                this.typeface = typeface
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
            btn_repeat_mode.setColorFilter(colorProvider.colorModeOff, android.graphics.PorterDuff.Mode.SRC_IN)
            drawable.start()
        } else {
            val repeatOneFlag = mode == Player.REPEAT_ONE
            val drawableId = if (repeatOneFlag)
                R.drawable.ic_repeat_one_to_all
            else R.drawable.ic_repeat_all_to_one

            val drawable = ContextCompat
                    .getDrawable(context, drawableId) as AnimatedVectorDrawable

            btn_repeat_mode.setImageDrawable(drawable)
            btn_repeat_mode.setColorFilter(colorProvider.colorModeOn, android.graphics.PorterDuff.Mode.SRC_IN)
            if (repeatOneFlag) drawable.start()
        }
    }

    private fun updateShuffleIcon(@Player.ShuffleMode mode: Int, animate: Boolean) {
        val enable = mode == Player.SHUFFLE_ON
        val colorFilter = if (enable) colorProvider.colorModeOn else colorProvider.colorModeOff
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

    private fun updateAB(aPointed: Boolean, bPointed: Boolean, animate: Boolean) {
        val ss = SpannableString("A-B")
        val flag = SpannableString.SPAN_INCLUSIVE_INCLUSIVE
        ss.setSpan(ForegroundColorSpan(if (aPointed) colorProvider.colorModeOn else colorProvider.colorModeOff), 0, 1, flag)
        ss.setSpan(ForegroundColorSpan(if (bPointed) colorProvider.colorModeOn else colorProvider.colorModeOff), 1, 2, flag)
        ss.setSpan(ForegroundColorSpan(if (bPointed) colorProvider.colorModeOn else colorProvider.colorModeOff), 2, 3, flag)
        btn_ab.text = ss
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            songDeletedEvent.observeNonNull(owner) {
                toastShortMessage(R.string.deleted)
            }

            isFavourite.observeNonNull(owner) { isFavourite ->
                updateFavouriteIcon(isFavourite, animate = true)
            }

            songQueue.observe(owner) { queue: SongQueue? ->
                Trace.d(LOG_TAG, "SongQueue changed")
                (vp_album_art.adapter as? SongAdapter)?.submitQueue(queue)
            }

            invalidateSongQueueEvent.observeNonNull(owner) {
                Trace.d(LOG_TAG, "InvalidateSongQueue event fired")
                vp_album_art.adapter?.notifyDataSetChanged()
            }

            song.observeNonNull(owner) { song: Song? ->
                Trace.d(LOG_TAG, "Song changed")
                if (song != null) {
                    tsw_song_name.setText(song.getNameString(resources))
                    tsw_artist_name.setText(song.getArtistString(resources))
                } else {
                    tsw_song_name.setText("")
                    tsw_artist_name.setText("")
                }
            }

            placeholderVisible.observeNonNull(owner) { isVisible ->
                layout_player_placeholder.visibility = if (isVisible) View.VISIBLE else View.GONE
            }

            songPosition.observeNonNull(owner) { position ->
                Trace.d(LOG_TAG, "Song position changed to $position")

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
                sb_progress.max = duration
                tv_duration.text = duration.asDurationInMs()
            }

            playbackProgress.observeNonNull(owner) { progress ->
                if (!isTrackingProgress) {
                    sb_progress.progress = progress
                    tv_position.text = progress.asDurationInMs()
                }
            }

            playbackStatus.observeNonNull(owner) { status: Boolean ->
                updatePlayButton(status)
            }

            abStatus.observeNonNull(owner) { abStatus ->
                updateAB(abStatus.first, abStatus.second, false)
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
    }
}