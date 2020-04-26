package com.frolo.muse.ui.main.library.artists.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.R
import com.frolo.muse.model.media.Artist
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.artists.artist.albums.AlbumsOfArtistFragment
import com.frolo.muse.ui.main.library.artists.artist.songs.SongsOfArtistFragment
import com.frolo.muse.views.showBackArrow
import kotlinx.android.synthetic.main.fragment_artist.*


class ArtistFragment: BaseFragment() {

    companion object {
        private const val TAG_ALBUMS_OF_ARTIST = "albums_of_artist"
        private const val TAG_SONGS_OF_ARTIST = "albums_of_artist"

        private const val ARG_ARTIST = "artist"

        fun newInstance(artist: Artist) = ArtistFragment()
                .withArg(ARG_ARTIST, artist)
    }

    private val artist: Artist by serializableArg(ARG_ARTIST)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_artist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(tb_actions as Toolbar)
            supportActionBar?.apply {
                showBackArrow()
                title = artist.name
                subtitle = getString(R.string.artist)
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    private fun observeViewModel(owner: LifecycleOwner) {
    }

}