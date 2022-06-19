package com.frolo.core.ui.carousel

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.frolo.player.AudioSource


internal class FrameCarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?= null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr), ICarouselView {

    private val viewPager: ViewPager = ViewPager(context, attrs)
    private val pagerAdapter: FrameCarouselAdapter
        get() {
        val currAdapter = viewPager.adapter as? FrameCarouselAdapter
        if (currAdapter != null) {
            return currAdapter
        }
        val newAdapter = FrameCarouselAdapter(Glide.with(this))
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
            if (isUserScrollingPager) {
                carouselCallbacks.forEach { callback ->
                    callback.onPositionSelected(position)
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (pagerState == ViewPager.SCROLL_STATE_DRAGGING
                && state == ViewPager.SCROLL_STATE_SETTLING) {
                isUserScrollingPager = true
            } else if (pagerState == ViewPager.SCROLL_STATE_SETTLING
                && state == ViewPager.SCROLL_STATE_IDLE) {
                isUserScrollingPager = false
            }
            pagerState = state
        }
    }

    // State of the pager
    private var pagerState = ViewPager.SCROLL_STATE_IDLE
    private var isUserScrollingPager = false

    private val carouselCallbacks = HashSet<ICarouselView.CarouselCallback>(2)

    init {
        addView(viewPager)
    }

    override val size: Int get() = pagerAdapter.count

    override fun registerCallback(callback: ICarouselView.CarouselCallback) {
        carouselCallbacks.add(callback)
    }

    override fun unregisterCallback(callback: ICarouselView.CarouselCallback) {
        carouselCallbacks.remove(callback)
    }

    override fun invalidateData() {
        pagerAdapter.notifyDataSetChanged()
    }

    override fun submitList(list: List<AudioSource>?, commitCallback: Runnable?) {
        pagerAdapter.submitList(list, commitCallback)
    }

    override fun setCurrentPosition(position: Int) {
        viewPager.currentItem = position
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        pagerState = ViewPager.SCROLL_STATE_IDLE
        isUserScrollingPager = false
        viewPager.addOnPageChangeListener(pagerCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewPager.removeOnPageChangeListener(pagerCallback)
    }
}