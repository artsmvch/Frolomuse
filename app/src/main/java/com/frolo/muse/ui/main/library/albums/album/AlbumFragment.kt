package com.frolo.muse.ui.main.library.albums.album

import android.os.Bundle
import android.view.*
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.calculateCardHorizontalShadowPadding
import com.frolo.muse.calculateCardVerticalShadowPadding
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.glide.makeRequest
import com.frolo.muse.glide.observe
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.toPx
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.google.android.material.appbar.AppBarLayout
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.fragment_album.*
import kotlin.math.abs


class AlbumFragment: AbsSongCollectionFragment<Song>(), NoClipping {
    companion object {

        private const val ARG_ALBUM = "album"

        // Factory
        fun newInstance(album: Album) = AlbumFragment()
                .withArg(ARG_ALBUM, album)
    }

    private val onOffsetChangedListener: AppBarLayout.OnOffsetChangedListener =
        AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
//            if (abs(verticalOffset) < appBarLayout.measuredHeight - fab_play.measuredHeight / 2) {
//                fab_play.show()
//            } else fab_play.hide()
        }

    override val viewModel: AlbumViewModel by lazy {
        val album = requireArguments().getSerializable(ARG_ALBUM) as Album
        val vmFactory = AlbumVMFactory(requireApp().appComponent, album)
        ViewModelProviders.of(this, vmFactory)
                .get(AlbumViewModel::class.java)
    }

    override val adapter by lazy {
        SongAdapter<Song>(Glide.with(this)).apply {
            setHasStableIds(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_album, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AlbumFragment.adapter
            decorateAsLinear()
        }

        tb_actions.apply {
            inflateMenu(R.menu.fragment_album)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.action_sort -> {
                        viewModel.onSortOrderOptionSelected()
                        true
                    }

                    else -> false
                }
            }
        }

        cv_album_art.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            val shadow = 16f.toPx(view.context)
            val corner = 4f.toPx(view.context)
            val horizontalShadow = calculateCardHorizontalShadowPadding(shadow, corner).toInt()
            val verticalShadow = calculateCardVerticalShadowPadding(shadow, corner).toInt()

            leftMargin = horizontalShadow
            topMargin = verticalShadow
            rightMargin = horizontalShadow
            bottomMargin = verticalShadow
        }

        cv_album_art.setOnClickListener {
            viewModel.onAlbumArtClicked()
        }

        fab_play.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        app_bar_layout.addOnOffsetChangedListener(onOffsetChangedListener)
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
        super.onDestroyView()

        app_bar_layout.removeOnOffsetChangedListener(onOffsetChangedListener)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        mediaItemCount.observeNonNull(owner) { count ->
            //tv_title.text = requireContext().resources.getQuantityString(R.plurals.s_songs, count, count)
        }

        title.observeNonNull(owner) { title ->
            //tb_actions.title = title
        }

        albumId.observeNonNull(owner) { albumId ->
            loadAlbumArt(albumId)
        }

        playButtonVisible.observeNonNull(owner) { isVisible ->
            if (isVisible) fab_play.show() else fab_play.hide()
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
        Glide.with(this@AlbumFragment)
            .makeRequest(albumId)
            .placeholder(R.drawable.ic_album_200dp)
            .error(R.drawable.ic_album_200dp)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imv_album_art)

        Glide.with(this@AlbumFragment)
            .makeRequest(albumId)
            .placeholder(R.drawable.ic_album_200dp)
            .error(R.drawable.ic_album_200dp)
            .transform(BlurTransformation(25))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imv_blurred_album_art)
    }

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                rv_list.setPadding(left, top, right, bottom)
                rv_list.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }
}