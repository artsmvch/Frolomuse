package com.frolo.muse.ui.main.library.albums.album

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.frolo.muse.R
import com.frolo.ui.StyleUtils
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.glide.*
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.dp2px
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.setupNavigation
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.confirmShortcutCreation
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.smoothScrollToTop
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.android.synthetic.main.fragment_album.*
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlin.math.abs
import kotlin.math.pow


open class AlbumFragment: AbsSongCollectionFragment<Song>(), FragmentContentInsetsListener {

    private val onOffsetChangedListener: AppBarLayout.OnOffsetChangedListener =
        AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val scrollFactor: Float = abs(verticalOffset.toFloat() / (view_backdrop.measuredHeight))

            viewModel.onHeaderScrolled(scrollFactor)

            (view_backdrop.background as? MaterialShapeDrawable)?.apply {
                val poweredScrollFactor = scrollFactor.pow(2)
                val cornerRadius = backdropCornerRadius * (1 - poweredScrollFactor)
                this.shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setBottomRightCorner(CornerFamily.ROUNDED, cornerRadius)
                    .build()
            }
        }

    override val viewModel: AlbumViewModel by lazy {
        val album = requireArguments().getSerializable(ARG_ALBUM) as Album
        val vmFactory = AlbumVMFactory(requireFrolomuseApp().appComponent, album)
        ViewModelProviders.of(this, vmFactory)
                .get(AlbumViewModel::class.java)
    }

    override val adapter by lazy { SongOfAlbumAdapter(provideThumbnailLoader()) }

    private val backdropCornerRadius: Float by lazy { 72f.dp2px(requireContext()) }

    private var isPlayButtonAlwaysVisible: Boolean = false

    protected fun setPlayButtonAlwaysVisible() {
        isPlayButtonAlwaysVisible = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_album, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(tb_actions)

        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AlbumFragment.adapter
            addLinearItemMargins()
        }

        tb_actions.apply {
            inflateMenu(R.menu.fragment_album)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.action_create_shortcut -> {
                        viewModel.onCreateAlbumShortcutActionSelected()
                        true
                    }

                    R.id.action_sort -> {
                        viewModel.onSortOrderOptionSelected()
                        true
                    }

                    else -> false
                }
            }
        }

        cv_album_art.setOnClickListener {
            viewModel.onAlbumArtClicked()
        }

        fab_play.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        app_bar_layout.addOnOffsetChangedListener(onOffsetChangedListener)

        view_backdrop.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(StyleUtils.resolveColor(view.context, R.attr.colorPrimary))
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setBottomRightCorner(CornerFamily.ROUNDED, backdropCornerRadius)
                .build()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)

        GlideAlbumArtHelper.get().observe(this) { updatedAlbumId ->
            viewModel.albumId.value?.also { albumId ->
                if (albumId == updatedAlbumId) loadAlbumArt(albumId)
            }
        }
    }

    override fun onDestroyView() {
        app_bar_layout.removeOnOffsetChangedListener(onOffsetChangedListener)
        super.onDestroyView()
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        albumName.observe(owner) { albumName ->
            tv_album_name.text = albumName
        }

        artistName.observe(owner) { artistName ->
            tv_artist_name.text = artistName
        }

        albumId.observeNonNull(owner) { albumId ->
            loadAlbumArt(albumId)
        }

        playButtonVisible.observeNonNull(owner) { isVisible ->
            if (isPlayButtonAlwaysVisible || isVisible) {
                if (!fab_play.isOrWillBeShown) fab_play.show()
            } else {
                if (!fab_play.isOrWillBeHidden) fab_play.hide()
            }
        }

        confirmAlbumShortcutCreationEvent.observeNonNull(owner) { album ->
            context?.confirmShortcutCreation(album) {
                viewModel.onCreateAlbumShortcutActionConfirmed()
            }
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

    private fun loadAlbumArt(albumId: Long) {
        val requestOptions = RequestOptions()
        // Use theme of the host context for placeholder and error drawables
        context?.theme?.let { safeTheme ->
            requestOptions.theme(safeTheme)
        }
        Glide.with(this@AlbumFragment)
            .makeRequestAsBitmap(albumId)
            .placeholder(R.drawable.ic_album_200dp)
            .error(R.drawable.ic_album_200dp)
            .apply(requestOptions)
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(imv_album_art)
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

        // Required argument
        const val ARG_ALBUM = "album"

        // Factory
        fun newInstance(album: Album) = AlbumFragment()
                .withArg(ARG_ALBUM, album)
    }

}