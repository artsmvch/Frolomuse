package com.frolo.core.ui.carousel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.frolo.debug.DebugUtils
import com.frolo.player.AudioSource


internal class CardCarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?= null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr), ICarouselView {

    private val uiCallbacks = HashMap<String, Runnable>()
    private val uiHandler = Handler(context.mainLooper)

    private val viewPager: ViewPager2 = ViewPager2(context, attrs)
    private val adapter: CardCarouselAdapter
        get() {
        val currAdapter = viewPager.adapter as? CardCarouselAdapter
        if (currAdapter != null) {
            return currAdapter
        }
        val newAdapter = CardCarouselAdapter(Glide.with(this))
        viewPager.adapter = newAdapter
        return newAdapter
    }

    private val pageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (isUserScrollingPager) {
                carouselCallbacks.forEach { callback ->
                    callback.onPositionSelected(position)
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (pagerState == ViewPager2.SCROLL_STATE_DRAGGING
                && state == ViewPager2.SCROLL_STATE_SETTLING) {
                isUserScrollingPager = true
            } else if (pagerState == ViewPager2.SCROLL_STATE_SETTLING
                && state == ViewPager2.SCROLL_STATE_IDLE) {
                isUserScrollingPager = false
            }
            pagerState = state
        }
    }

    // The state of the pager
    private var pagerState = ViewPager2.SCROLL_STATE_IDLE
    // This flag indicates whether the user is scrolling the pager
    private var isUserScrollingPager = false
    // Indicates whether a list is being submitted to the adapter at the moment
    private var isSubmittingList: Boolean = false
    // Indicates the pending position, to which the pager should scroll when a list is submitted
    private var pendingPosition: Int? = null

    private val carouselCallbacks = HashSet<ICarouselView.CarouselCallback>(2)

    override val size: Int get() = adapter.itemCount

    init {
        addView(viewPager)
        CardCarouselHelper.setup(viewPager)
        viewPager.registerOnPageChangeCallback(pageCallback)
    }

    override fun registerCallback(callback: ICarouselView.CarouselCallback) {
        carouselCallbacks.add(callback)
    }

    override fun unregisterCallback(callback: ICarouselView.CarouselCallback) {
        carouselCallbacks.remove(callback)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun invalidateData() {
        adapter.notifyDataSetChanged()
        postRequestPageTransform()
    }

    /**
     * Submits the given [list] to the adapter of the backing view pager.
     * When the submitting is complete, two callbacks are posted:
     * a callback that is responsible for requesting page transform
     * and a callback that is responsible for scrolling the pager to the pending position.
     * Before submitting, if the pending position is null, it is assigned the current pager position.
     * This is to prevent the pager from scrolling automatically when the adapter notifies about changes.
     */
    override fun submitList(list: List<AudioSource>?, commitCallback: Runnable?) {
        if (pendingPosition == null) {
            pendingPosition = viewPager.currentItem
        }

        isSubmittingList = true
        adapter.submitList(list) {
            isSubmittingList = false
            commitCallback?.run()
            postRequestPageTransform()
            postScrollToPendingPosition()
        }
    }

    /**
     * Scrolls the backing view pager to [position]. The scrolling is async in the sense
     * that [position] is stored as pending scrolling in the near future.
     */
    override fun setCurrentPosition(position: Int) {
        pendingPosition = position
        postScrollToPendingPosition()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        pagerState = ViewPager2.SCROLL_STATE_IDLE
        isUserScrollingPager = false
        isSubmittingList = false
        pendingPosition = null
        viewPager.registerOnPageChangeCallback(pageCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewPager.unregisterOnPageChangeCallback(pageCallback)
        uiCallbacks.values.forEach { callback ->
            uiHandler.removeCallbacks(callback)
        }
        uiCallbacks.clear()
    }

    private fun postOnUi(key: String, callback: Runnable) {
        if (isAttachedToWindow) {
            // Wrapper
            val newCallback = object : Runnable {
                override fun run() {
                    // Clean up this callback
                    if (uiCallbacks[key] === this) {
                        uiCallbacks.remove(key)
                    } else {
                        DebugUtils.dump(IllegalStateException("Callback not found for key $key"))
                    }
                    if (!isAttachedToWindow) {
                        DebugUtils.dump(IllegalStateException("Callback fired when detached"))
                    }
                    callback.run()
                }
            }
            val oldCallback = uiCallbacks.put(key, newCallback)
            if (oldCallback != null) {
                uiHandler.removeCallbacks(oldCallback)
            }
            uiHandler.post(newCallback)
        }
    }

    /**
     * Posts a callback to request page transform.
     * The previously posted callback is ignored.
     */
    private fun postRequestPageTransform() {
        val callback = Runnable {
            viewPager.requestTransform()
        }
        postOnUi(key = "request_page_transform", callback)
    }

    /**
     * Posts a callback to scroll to the pending position, if any.
     * The previously posted callback is ignored.
     */
    private fun postScrollToPendingPosition() {
        val callback = Runnable {
            if (!isSubmittingList) {
                pendingPosition?.also { safePosition ->
                    viewPager.setCurrentItem(safePosition, true)
                }
                pendingPosition = null
            }
        }
        postOnUi(key = "scroll_to_pending_position", callback)
    }

}