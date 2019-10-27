package com.frolo.muse.ui.main.library

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.frolo.muse.model.Library
import com.frolo.muse.ui.getSectionName
import com.frolo.muse.ui.main.library.albums.AlbumListFragment
import com.frolo.muse.ui.main.library.artists.ArtistListFragment
import com.frolo.muse.ui.main.library.favourites.FavouriteSongListFragment
import com.frolo.muse.ui.main.library.genres.GenreListFragment
import com.frolo.muse.ui.main.library.myfiles.MyFileListFragment
import com.frolo.muse.ui.main.library.playlists.PlaylistListFragment
import com.frolo.muse.ui.main.library.recent.RecentlyAddedSongListFragment
import com.frolo.muse.ui.main.library.songs.SongListFragment
import java.lang.ref.WeakReference


// TO_DO: Investigate import androidx.fragment.app.FragmentStatePagerAdapter
class LibraryPageAdapter constructor(
        fragmentManager: FragmentManager,
        context: Context
): FragmentPagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val context = WeakReference(context)

    var sections: List<@Library.Section Int> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    // holding initiated fragments
    private val fragments = SparseArray<Fragment>()

    override fun getItem(position: Int): Fragment {
        return when(sections[position]) {
            Library.ALL_SONGS -> SongListFragment()
            Library.ARTISTS -> ArtistListFragment()
            Library.ALBUMS -> AlbumListFragment()
            Library.GENRES -> GenreListFragment()
            Library.PLAYLISTS -> PlaylistListFragment()
            Library.FOLDERS -> MyFileListFragment()
            Library.FAVOURITES -> FavouriteSongListFragment()
            Library.RECENTLY_ADDED -> RecentlyAddedSongListFragment()
            else -> throw IllegalArgumentException(
                    "No item at the given position: $position")
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        fragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        fragments.remove(position)
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getItemId(position: Int): Long = sections[position].toLong()

    override fun getCount(): Int = sections.size

    override fun getPageTitle(position: Int): CharSequence? {
        return context.get().let { context ->
            if (context != null) getSectionName(context.resources, sections[position]) else ""
        }
    }

    fun getPageAt(position: Int): Fragment? = fragments[position]
}