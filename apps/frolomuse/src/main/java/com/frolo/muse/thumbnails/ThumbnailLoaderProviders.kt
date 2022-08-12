package com.frolo.muse.thumbnails

import androidx.fragment.app.Fragment
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.BaseFragment


fun BaseFragment.provideThumbnailLoader(): ThumbnailLoader {
    return provideThumbnailLoaderImpl(this)
}

fun BaseDialogFragment.provideThumbnailLoader(): ThumbnailLoader {
    return provideThumbnailLoaderImpl(this)
}

private fun provideThumbnailLoaderImpl(fragment: Fragment): ThumbnailLoader {
    return GlideThumbnailLoader(fragment)
}