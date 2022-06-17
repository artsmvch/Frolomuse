package com.frolo.core.ui.carousel

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pools
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.RequestManager
import com.frolo.core.ui.R
import com.frolo.debug.DebugUtils
import com.frolo.player.AudioSource


internal class FrameCarouselAdapter constructor(
    private val requestManager: RequestManager
): PagerAdapter() {

    private var list: List<AudioSource>? = null

    private val viewHoldersPool = Pools.SimplePool<ViewHolderImpl>(4)

    fun submitList(list: List<AudioSource>?, commitCallback: Runnable?) {
        this.list = list
        notifyDataSetChanged()
        commitCallback?.run()
    }

    override fun getCount(): Int = list?.count() ?: 0

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == (obj as ViewHolderImpl).view
    }

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = list?.getOrNull(position)
        if (item == null) {
            DebugUtils.dump(IllegalArgumentException(
                "Item not found at position $position"))
        }
        val holderImpl = viewHoldersPool.acquire() ?: createViewHolderImpl(container)
        container.addView(holderImpl.view)
        holderImpl.bind(item)
        return holderImpl
    }

    private fun createViewHolderImpl(container: ViewGroup): ViewHolderImpl {
        val layoutInflater = LayoutInflater.from(container.context)
        val itemView = layoutInflater.inflate(R.layout.carousel_item_frame, container, false)
        return ViewHolderImpl(
            view = itemView,
            imageContainer = itemView.findViewById(R.id.imv_art),
            imageView = itemView.findViewById(R.id.imv_art),
            progressBar = itemView.findViewById(R.id.progress_bar),
            requestManager = requestManager
        )
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        obj as ViewHolderImpl
        obj.recycle()
        container.removeView(obj.view)
        if (!viewHoldersPool.release(obj)) {
            Log.w(LOG_TAG, "ViewHolder pool is full")
        }
    }

    companion object {
        private const val LOG_TAG = "FrameCarouselAdapter"
    }
}
