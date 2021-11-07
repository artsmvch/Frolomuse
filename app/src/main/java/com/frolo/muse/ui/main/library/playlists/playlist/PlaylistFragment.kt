package com.frolo.muse.ui.main.library.playlists.playlist

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.*
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.adapter.SimpleItemTouchHelperCallback
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.base.DragSongAdapter
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.setupNavigation
import com.frolo.muse.ui.main.confirmShortcutCreation
import com.frolo.muse.ui.smoothScrollToTop
import com.frolo.muse.ui.toString
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.android.synthetic.main.fragment_playlist.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlin.math.abs
import kotlin.math.pow


class PlaylistFragment: AbsSongCollectionFragment<Song>(), FragmentContentInsetsListener {

    override val viewModel: PlaylistViewModel by lazy {
        val playlist = requireArguments().getSerializable(ARG_PLAYLIST) as Playlist
        val vmFactory = PlaylistVMFactory(requireFrolomuseApp().appComponent, playlist)
        ViewModelProviders.of(this, vmFactory)
                .get(PlaylistViewModel::class.java)
    }

    private lateinit var itemTouchHelper: ItemTouchHelper

    private val onDragListener = object : DragSongAdapter.OnDragListener {
        override fun onTouchDragView(holder: RecyclerView.ViewHolder) {
            itemTouchHelper.startDrag(holder)
        }

        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            view?.apply {
                removeCallbacksSafely(onDragEndedCallback)
                val callback = Runnable { dispatchItemMoved(fromPosition, toPosition) }
                post(callback)
                onDragEndedCallback = callback
            }

        }

        override fun onItemDismissed(position: Int) {
            dispatchItemRemoved(adapter.getItemAt(position))
        }
    }

    private var onDragEndedCallback: Runnable? = null

    override val adapter: SongAdapter<Song> by lazy {
        DragSongAdapter(provideThumbnailLoader(), onDragListener)
    }

    private val onOffsetChangedListener: AppBarLayout.OnOffsetChangedListener =
        AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val scrollFactor: Float = abs(verticalOffset.toFloat() / (view_backdrop.measuredHeight))

            (view_backdrop.background as? MaterialShapeDrawable)?.apply {
                val poweredScrollFactor = scrollFactor.pow(2)
                val cornerRadius = backdropCornerRadius * (1 - poweredScrollFactor)
                this.shapeAppearanceModel = ShapeAppearanceModel.builder()
                        .setBottomRightCorner(CornerFamily.ROUNDED, cornerRadius)
                        .build()
            }
        }

    private val backdropCornerRadius: Float by lazy { 72f.dp2px(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(tb_actions)

        val callback = SimpleItemTouchHelperCallback(adapter as DragSongAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rv_list)
        itemTouchHelper = touchHelper

        rv_list.apply {
            layoutManager = LinearLayoutManager(context)

            adapter = this@PlaylistFragment.adapter

            addLinearItemMargins()
        }

        tb_actions.apply {
            inflateMenu(R.menu.fragment_playlist)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit ->
                        viewModel.onEditPlaylistOptionSelected()

                    R.id.action_create_shortcut ->
                        viewModel.onCreatePlaylistShortcutActionSelected()

                    R.id.action_sort ->
                        viewModel.onSortOrderOptionSelected()
                }
                return@setOnMenuItemClickListener true
            }
        }

        app_bar_layout.addOnOffsetChangedListener(onOffsetChangedListener)

        view_backdrop.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(StyleUtil.resolveColor(view.context, R.attr.colorPrimary))
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setBottomRightCorner(CornerFamily.ROUNDED, backdropCornerRadius)
                .build()
        }

        btn_play.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        btn_add_song.setOnClickListener {
            viewModel.onAddSongButtonClicked()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        view?.removeCallbacks(onDragEndedCallback)
        onDragEndedCallback = null

        app_bar_layout.removeOnOffsetChangedListener(onOffsetChangedListener)

        super.onDestroyView()
    }

    private fun dispatchItemRemoved(item: Song) {
        maybeCheckWritePermissionFor {
            viewModel.onItemRemoved(item)
        }
    }

    private fun dispatchItemMoved(fromPosition: Int, toPosition: Int) {
        maybeCheckWritePermissionFor {
            val listSnapshot = adapter.getSnapshot()
            viewModel.onItemMoved(fromPosition, toPosition, listSnapshot)
        }
    }

    /**
     * Optionally checks the write permission for [action] on the playlist. If the playlist is not
     * from the shared storage, then the write permission is not required (since the playlist is stored
     * and managed by the application).
     * Otherwise, the permission will be checked and / or requested (if necessary).
     * But it will be only requested, if the state of the fragment is not saved.
     */
    private inline fun maybeCheckWritePermissionFor(crossinline action: () -> Unit) {
        val currentPlaylist = viewModel.playlist.value
        if (currentPlaylist != null && !currentPlaylist.isFromSharedStorage) {
            action.invoke()
        } else if (!isStateSaved) {
            checkWritePermissionFor(action)
        }
    }

    override fun onSetLoading(loading: Boolean) {
        pb_loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        layout_list_placeholder.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        playlist.observe(owner) { item ->
            tv_playlist_name.text = item?.name
        }

        songCountWithTotalDuration.observe(owner) { songCountWithTotalDuration ->
            tv_playlist_info.text = songCountWithTotalDuration?.toString(resources)
        }

        isSwappingEnabled.observeNonNull(owner) { isSwappingEnabled ->
            (rv_list.adapter as DragSongAdapter).also { adapter ->
                adapter.itemViewType = if (isSwappingEnabled) {
                    DragSongAdapter.VIEW_TYPE_SWAPPABLE
                } else {
                    DragSongAdapter.VIEW_TYPE_NORMAL
                }
            }
        }

        confirmPlaylistShortcutCreationEvent.observeNonNull(owner) { playlist ->
            context?.confirmShortcutCreation(playlist) {
                viewModel.onCreatePlaylistShortcutActionConfirmed()
            }
        }
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                rv_list.setPadding(left, top, right, bottom)
                rv_list.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }

    override fun scrollToTop() {
        rv_list?.smoothScrollToTop()
    }

    companion object {
        private const val ARG_PLAYLIST = "playlist"

        // Factory
        fun newInstance(playlist: Playlist) = PlaylistFragment()
                .withArg(ARG_PLAYLIST, playlist)
    }

}