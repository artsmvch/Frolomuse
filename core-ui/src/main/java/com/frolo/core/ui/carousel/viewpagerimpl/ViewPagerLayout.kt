package com.frolo.core.ui.carousel.viewpagerimpl

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager
import com.frolo.core.ui.carousel.ICarouselView
import com.frolo.player.AudioSource


internal abstract class ViewPagerLayout<P: ViewPagerLayout.Adapter> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr), ICarouselView {

    private val viewPager: ViewPager = ViewPager(context, attrs)
    private val adapter: Adapter get() {
        val currAdapter = viewPager.adapter as? Adapter
        if (currAdapter != null) {
            return currAdapter
        }
        val newAdapter = onCreatePagerAdapter()
        viewPager.adapter = newAdapter
        return newAdapter
    }

    private val pagerCallback = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) = Unit

        override fun onPageSelected(position: Int) {
            dispatchPageSelected(position, isScrolling)
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (pagerState == ViewPager.SCROLL_STATE_DRAGGING
                && state == ViewPager.SCROLL_STATE_SETTLING) {
                isScrolling = true
            } else if (pagerState == ViewPager.SCROLL_STATE_SETTLING
                && state == ViewPager.SCROLL_STATE_IDLE) {
                isScrolling = false
            }
            pagerState = state
        }
    }

    // State of the pager
    private var pagerState: Int = ViewPager.SCROLL_STATE_IDLE
    private var isScrolling: Boolean = false

    private val carouselCallbacks = HashSet<ICarouselView.CarouselCallback>(2)

    init {
        addView(viewPager)
    }

    protected abstract fun onCreatePagerAdapter(): P

    override val size: Int get() = adapter.count

    override fun registerCallback(callback: ICarouselView.CarouselCallback) {
        carouselCallbacks.add(callback)
    }

    override fun unregisterCallback(callback: ICarouselView.CarouselCallback) {
        carouselCallbacks.remove(callback)
    }

    override fun invalidateData() {
        adapter.notifyDataSetChanged()
    }

    override fun submitList(list: List<AudioSource>?, commitCallback: Runnable?) {
        adapter.submitList(list, commitCallback)
    }

    override fun setCurrentPosition(position: Int) {
        viewPager.currentItem = position
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resetPagerState()
        viewPager.addOnPageChangeListener(pagerCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewPager.removeOnPageChangeListener(pagerCallback)
    }

    private fun resetPagerState() {
        pagerState = ViewPager.SCROLL_STATE_IDLE
        isScrolling = false
    }

    private fun dispatchPageSelected(position: Int, byUser: Boolean) {
        carouselCallbacks.forEach { callback ->
            callback.onPositionSelected(position, byUser)
        }
    }

    abstract class Adapter : androidx.viewpager.widget.PagerAdapter() {
        abstract fun submitList(list: List<AudioSource>?, commitCallback: Runnable?)
    }
}