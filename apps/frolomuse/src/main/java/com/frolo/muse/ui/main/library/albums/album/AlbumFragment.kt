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
import com.frolo.core.ui.glide.GlideAlbumArtHelper
import com.frolo.core.ui.glide.makeAlbumArtRequestAsBitmap
import com.frolo.core.ui.glide.observe
import com.frolo.muse.R
import com.frolo.ui.StyleUtils
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.FragmentAlbumBinding
import com.frolo.muse.di.activityComponent
import com.frolo.music.model.Album
import com.frolo.music.model.Song
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
import kotlin.math.abs
import kotlin.math.pow


open class AlbumFragment: AbsSongCollectionFragment<Song>(), FragmentContentInsetsListener {
    private var _binding: FragmentAlbumBinding? = null
    private val binding: FragmentAlbumBinding get() = _binding!!

    private val onOffsetChangedListener: AppBarLayout.OnOffsetChangedListener =
        AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val scrollFactor: Float = abs(verticalOffset.toFloat() / (binding.viewBackdrop.measuredHeight))

            viewModel.onHeaderScrolled(scrollFactor)

            (binding.viewBackdrop.background as? MaterialShapeDrawable)?.apply {
                val poweredScrollFactor = scrollFactor.pow(2)
                val cornerRadius = backdropCornerRadius * (1 - poweredScrollFactor)
                this.shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setBottomRightCorner(CornerFamily.ROUNDED, cornerRadius)
                    .build()
            }
        }

    override val viewModel: AlbumViewModel by lazy {
        val album = requireArguments().getSerializable(ARG_ALBUM) as Album
        val vmFactory = AlbumVMFactory(activityComponent, activityComponent, album)
        ViewModelProviders.of(this, vmFactory)
            .get(AlbumViewModel::class.java)
    }

    override val adapter by lazy { SongOfAlbumAdapter(provideThumbnailLoader()) }

    private val backdropCornerRadius: Float by lazy {
        resources.getDimension(R.dimen.backdrop_large_tongue_corner_radius)
    }

    private var isPlayButtonAlwaysVisible: Boolean = false

    protected fun setPlayButtonAlwaysVisible() {
        isPlayButtonAlwaysVisible = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlbumBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(binding.tbActions)

        binding.includeBaseList.rvList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AlbumFragment.adapter
            addLinearItemMargins()
        }

        binding.tbActions.apply {
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

        binding.cvAlbumArt.setOnClickListener {
            viewModel.onAlbumArtClicked()
        }

        binding.fabPlay.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        binding.appBarLayout.addOnOffsetChangedListener(onOffsetChangedListener)

        binding.viewBackdrop.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(StyleUtils.resolveColor(view.context,
                com.google.android.material.R.attr.colorPrimary))
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
        binding.appBarLayout.removeOnOffsetChangedListener(onOffsetChangedListener)
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        albumName.observe(owner) { albumName ->
            binding.tvAlbumName.text = albumName
        }

        artistName.observe(owner) { artistName ->
            binding.tvArtistName.text = artistName
        }

        albumId.observeNonNull(owner) { albumId ->
            loadAlbumArt(albumId)
        }

        playButtonVisible.observeNonNull(owner) { isVisible ->
            if (isPlayButtonAlwaysVisible || isVisible) {
                if (!binding.fabPlay.isOrWillBeShown) binding.fabPlay.show()
            } else {
                if (!binding.fabPlay.isOrWillBeHidden) binding.fabPlay.hide()
            }
        }

        confirmAlbumShortcutCreationEvent.observeNonNull(owner) { album ->
            context?.confirmShortcutCreation(album) {
                viewModel.onCreateAlbumShortcutActionConfirmed()
            }
        }
    }

    override fun onSetLoading(loading: Boolean) {
        binding.includeBaseList.pbLoading.root.visibility =
            if (loading) View.VISIBLE else View.GONE
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        binding.includeBaseList.layoutListPlaceholder.root.visibility =
            if (visible) View.VISIBLE else View.GONE
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
            .makeAlbumArtRequestAsBitmap(albumId)
            .placeholder(R.drawable.ic_album_200dp)
            .error(R.drawable.ic_album_200dp)
            .apply(requestOptions)
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(binding.imvAlbumArt)
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                binding.includeBaseList.rvList.setPadding(left, top, right, bottom)
                binding.includeBaseList.rvList.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }

    override fun scrollToTop() {
        binding.includeBaseList.rvList.smoothScrollToTop()
    }

    companion object {

        // Required argument
        const val ARG_ALBUM = "album"

        // Factory
        fun newInstance(album: Album) = AlbumFragment()
                .withArg(ARG_ALBUM, album)
    }

}