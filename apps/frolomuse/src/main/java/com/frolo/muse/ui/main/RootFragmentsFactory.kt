package com.frolo.muse.ui.main

import androidx.fragment.app.Fragment
import com.frolo.muse.ui.main.audiofx2.AudioFx2Fragment
import com.frolo.muse.ui.main.library.LibraryFragment
import com.frolo.muse.ui.main.library.search.SearchFragment
import com.frolo.muse.ui.main.settings.AppBarSettingsFragment

internal class RootFragmentsFactory {
    fun createLibraryFragment(): Fragment {
        return LibraryFragment.newInstance()
    }

    fun createAudioFxFragment(): Fragment {
        return AudioFx2Fragment.newInstance()
    }

    fun createSearchFragment(): Fragment {
        return SearchFragment.newInstance()
    }

    fun createSettingsFragment(): Fragment {
        return AppBarSettingsFragment.newInstance()
    }
}