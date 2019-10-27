package com.frolo.muse.ui.main.library

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.frolo.muse.model.Library
import com.frolo.muse.ui.main.library.albums.AlbumListFragment
import com.frolo.muse.ui.main.library.artists.ArtistListFragment
import com.frolo.muse.ui.main.library.favourites.FavouriteSongListFragment
import com.frolo.muse.ui.main.library.genres.GenreListFragment
import com.frolo.muse.ui.main.library.myfiles.MyFileListFragment
import com.frolo.muse.ui.main.library.playlists.PlaylistListFragment
import com.frolo.muse.ui.main.library.recent.RecentlyAddedSongListFragment
import com.frolo.muse.ui.main.library.songs.SongListFragment

// Will be used when the library migrates from ViewPager to ViewPager2
@Deprecated("Only until we migrate to ViewPager2")
class SectionAdapter constructor(
        host: Fragment,
        val sections: List<@Library.Section Int>
) : FragmentStateAdapter(host) {

    // holding initiated fragments
    private val fragments = SparseArray<Fragment>()

    override fun getItemCount(): Int = sections.size

    override fun createFragment(position: Int): Fragment {
        return when(sections[position]) {
            Library.ALL_SONGS -> SongListFragment()
            Library.ARTISTS -> ArtistListFragment()
            Library.ALBUMS -> AlbumListFragment()
            Library.GENRES -> GenreListFragment()
            Library.PLAYLISTS -> PlaylistListFragment()
            Library.FOLDERS -> MyFileListFragment()
            Library.FAVOURITES -> FavouriteSongListFragment()
            Library.RECENTLY_ADDED -> RecentlyAddedSongListFragment()
            else -> throw IllegalArgumentException("Wrong item position: $position")
        }
    }

    fun getPageAt(position: Int): Fragment? = fragments[position]
}