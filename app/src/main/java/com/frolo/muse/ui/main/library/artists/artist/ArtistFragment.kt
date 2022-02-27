package com.frolo.muse.ui.main.library.artists.artist

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.ui.StyleUtils
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.di.activityComponent
import com.frolo.music.model.Artist
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.setupNavigation
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.confirmShortcutCreation
import com.frolo.muse.ui.main.library.artists.artist.albums.AlbumsOfArtistFragment
import com.frolo.muse.ui.main.library.artists.artist.songs.SongsOfArtistFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.android.synthetic.main.fragment_artist.*
import kotlin.math.abs
import kotlin.math.pow


class ArtistFragment: BaseFragment() {

    private val artist: Artist by serializableArg(ARG_ARTIST)

    private val onOffsetChangedListener: AppBarLayout.OnOffsetChangedListener =
        AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val scrollFactor: Float = abs(verticalOffset.toFloat() / (view_backdrop.measuredHeight))

            (view_backdrop.background as? MaterialShapeDrawable)?.apply {
                val poweredScrollFactor = scrollFactor.pow(2)
                val cornerRadius = backdropCornerRadius * (1 - poweredScrollFactor)
                this.shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setBottomLeftCorner(CornerFamily.ROUNDED, cornerRadius)
                    .setBottomRightCorner(CornerFamily.ROUNDED, cornerRadius)
                    .build()
            }
        }

    private val backdropCornerRadius: Float by lazy {
        resources.getDimension(R.dimen.backdrop_shallow_tongue_corner_radius)
    }

    private val viewModel: ArtistViewModel by lazy {
        val artist = requireArguments().getSerializable(ARG_ARTIST) as Artist
        val vmFactory = ArtistVMFactory(activityComponent, artist)
        ViewModelProviders.of(this, vmFactory).get(ArtistViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_artist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(tb_actions)

        tb_actions.apply {
            inflateMenu(R.menu.fragment_artist)
            setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.action_create_shortcut) {
                    viewModel.onCreateArtistShortcutActionSelected()
                }

                if (menuItem.itemId == R.id.action_sort) {
                    val fragment =
                            childFragmentManager.findFragmentByTag(TAG_SONGS_OF_ARTIST) as? SongsOfArtistFragment
                    fragment?.onSortOrderActionSelected()
                }

                true
            }
        }

        val transaction = childFragmentManager.beginTransaction()

        val albumsOfArtistFragment = childFragmentManager.findFragmentByTag(TAG_ALBUMS_OF_ARTIST)
        if (albumsOfArtistFragment == null) {
            val newFragment = AlbumsOfArtistFragment.newInstance(artist)
            transaction.replace(R.id.fl_albums_container, newFragment, TAG_ALBUMS_OF_ARTIST)
        }

        val songsOfArtistFragment = childFragmentManager.findFragmentByTag(TAG_SONGS_OF_ARTIST)
        if (songsOfArtistFragment == null) {
            val newFragment = SongsOfArtistFragment.newInstance(artist)
            transaction.replace(R.id.fl_songs_container, newFragment, TAG_SONGS_OF_ARTIST)
        }

        transaction.commit()

        view_backdrop.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(StyleUtils.resolveColor(view.context, R.attr.colorPrimary))
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setBottomRightCorner(CornerFamily.ROUNDED, backdropCornerRadius)
                .build()
        }

        app_bar_layout.addOnOffsetChangedListener(onOffsetChangedListener)

        tv_artist_name.text = artist.name
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        app_bar_layout.removeOnOffsetChangedListener(onOffsetChangedListener)
        super.onDestroyView()
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        confirmArtistShortcutCreationEvent.observeNonNull(owner) { artist ->
            context?.confirmShortcutCreation(artist) {
                viewModel.onCreateArtistShortcutActionConfirmed()
            }
        }
    }

    companion object {
        private const val TAG_ALBUMS_OF_ARTIST = "albums_of_artist"
        private const val TAG_SONGS_OF_ARTIST = "albums_of_artist"

        private const val ARG_ARTIST = "artist"

        fun newInstance(artist: Artist) = ArtistFragment()
                .withArg(ARG_ARTIST, artist)
    }

}