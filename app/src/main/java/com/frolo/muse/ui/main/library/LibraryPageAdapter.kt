package com.frolo.muse.ui.main.library

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.frolo.muse.DebugUtils
import com.frolo.muse.Features
import com.frolo.muse.model.Library
import com.frolo.muse.ui.getSectionName
import com.frolo.muse.ui.main.library.albums.AlbumListFragment
import com.frolo.muse.ui.main.library.artists.ArtistListFragment
import com.frolo.muse.ui.main.library.buckets.AudioBucketListFragment
import com.frolo.muse.ui.main.library.favourites.FavouriteSongListFragment
import com.frolo.muse.ui.main.library.genres.GenreListFragment
import com.frolo.muse.ui.main.library.mostplayed.MostPlayedFragment
import com.frolo.muse.ui.main.library.myfiles.MyFileListFragment
import com.frolo.muse.ui.main.library.playlists.PlaylistListFragment
import com.frolo.muse.ui.main.library.recent.RecentlyAddedSongListFragment
import com.frolo.muse.ui.main.library.songs.SongListFragment
import java.lang.ref.WeakReference


class LibraryPageAdapter constructor(
    context: Context,
    fragmentManager: FragmentManager,
    val sections: List<@Library.Section Int>
): FragmentPagerAdapter(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val context = WeakReference(context)
    private val instantiatedFragments = SparseArray<WeakReference<Fragment>>()

    override fun getItem(position: Int): Fragment {
        return when(val section = sections[position]) {
            Library.ALL_SONGS ->        SongListFragment()
            Library.ARTISTS ->          ArtistListFragment()
            Library.ALBUMS ->           AlbumListFragment()
            Library.GENRES ->           GenreListFragment()
            Library.PLAYLISTS ->        PlaylistListFragment()
            Library.FOLDERS -> {
                if (Features.isPlainOldFileExplorerFeatureAvailable()) {
                    MyFileListFragment()
                } else {
                    AudioBucketListFragment()
                }
            }
            Library.FAVOURITES ->       FavouriteSongListFragment()
            Library.RECENTLY_ADDED ->   RecentlyAddedSongListFragment()
            Library.MOST_PLAYED ->      MostPlayedFragment()
            else -> {
                DebugUtils.dumpOnMainThread(IllegalArgumentException("Unexpected section: $section"))
                Fragment()
            }
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = super.instantiateItem(container, position)
        if (item is Fragment) {
            instantiatedFragments.put(position, WeakReference(item))
        } else {
            DebugUtils.dumpOnMainThread(IllegalStateException("$item is not a fragment"))
            instantiatedFragments.remove(position)
        }
        return item
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        instantiatedFragments.remove(position)
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getItemId(position: Int): Long = sections[position].toLong()

    override fun getCount(): Int = sections.size

    override fun getPageTitle(position: Int): CharSequence {
        val safeContext = context.get() ?: return EMPTY_PAGE_TITLE
        return getSectionName(safeContext.resources, sections[position])
    }

    internal fun getPageAt(position: Int): Fragment? = instantiatedFragments[position]?.get()

    companion object {
        private const val EMPTY_PAGE_TITLE = ""
    }
}