package com.frolo.muse.ui.main.onboarding

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.dp2px
import com.frolo.muse.ui.base.BaseFragment
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import kotlinx.android.synthetic.main.fragment_onboarding.*


class OnboardingFragment : BaseFragment() {

    private val onOnboardingFinishedListener: OnOnboardingFinishedListener?
        get() = activity as? OnOnboardingFinishedListener ?: parentFragment as? OnOnboardingFinishedListener

    private val pageInfoItems: List<OnboardingPageInfo>? by lazy {
        arguments?.getParcelableArrayList<OnboardingPageInfo>(ARG_PAGE_INFO_ITEMS)
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            updateBackground(position, positionOffset)
        }

        override fun onPageSelected(position: Int) {
            updateButtons(position)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_onboarding, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view_pager.apply {
            adapter = OnboardingPageAdapter(pageInfoItems.orEmpty())
            registerOnPageChangeCallback(onPageChangeCallback)
            // workaround for the ViewPager2
            getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER
        }

        indicator_view.apply {
            setSliderColor(Color.parseColor("#55FFFFFF"), Color.parseColor("#FFFFFF"))
            setSliderWidth(10f.dp2px(context))
            setIndicatorGap(20f.dp2px(context))
            setSlideMode(IndicatorSlideMode.WORM)
            setIndicatorStyle(IndicatorStyle.CIRCLE)
            setupWithViewPager(view_pager)
        }

        btn_skip.setOnClickListener {
            onOnboardingFinishedListener?.onOnboardingFinished(OnboardingResult.Skipped)
        }

        btn_done.setOnClickListener {
            onOnboardingFinishedListener?.onOnboardingFinished(OnboardingResult.Passed)
        }

        btn_next.setOnClickListener {
            val currentPosition = view_pager.currentItem
            val itemCount = view_pager.adapter?.itemCount ?: 0
            if (currentPosition < itemCount - 1) {
                view_pager.currentItem = currentPosition + 1
            }
        }

        val backgroundArtView = ImageView(context).also { v ->
            Glide.with(this)
                .load(R.drawable.png_onboarding_background)
                //.transform(Rotate(90))
                .into(v)
            v.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        parallax.apply {
            addView(backgroundArtView)
            setParallaxWidth(1.5f)
        }

        // for the proper setup when view is created
        onPageChangeCallback.onPageScrolled(0, 0f, 0)
    }

    private fun updateBackground(position: Int, positionOffset: Float) {
        val ctx = context ?: return
        val pageInfo1 = pageInfoItems?.getOrNull(position) ?: return
        val pageInfo2 = pageInfoItems?.getOrNull(position + 1)
        val color1 = ContextCompat.getColor(ctx, pageInfo1.colorId)
        val color2 = pageInfo2?.let { ContextCompat.getColor(ctx, it.colorId) }
        val background = if (color2 != null) {
            val blendedColor = ColorUtils.blendARGB(color1, color2, positionOffset)
            ColorDrawable(blendedColor)
        } else {
            ColorDrawable(color1)
        }
        imv_background_overlay.setImageDrawable(background)

        view_pager.adapter?.also { safeAdapter ->
            val itemCount = safeAdapter.itemCount
            if (itemCount > 1) {
                val absPosition = (position + positionOffset) / (itemCount - 1)
                parallax.setScrollOffset(absPosition)
            } else {
                parallax.setScrollOffset(0.5f)
            }
        }
    }

    private fun updateButtons(selectedPosition: Int) {
        val transition = Fade().apply {
            duration = 150
        }
        TransitionManager.beginDelayedTransition(fl_buttons, transition)
        val itemCount = view_pager.adapter?.itemCount ?: 0
        val isLastPage = selectedPosition >= itemCount - 1
        if (isLastPage) {
            btn_skip.visibility = View.INVISIBLE
            btn_done.visibility = View.VISIBLE
            btn_next.visibility = View.INVISIBLE
        } else {
            btn_skip.visibility = View.VISIBLE
            btn_done.visibility = View.INVISIBLE
            btn_next.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view_pager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

    interface OnOnboardingFinishedListener {
        fun onOnboardingFinished(result: OnboardingResult)
    }

    companion object {
        private const val ARG_PAGE_INFO_ITEMS = "page_info_items"

        fun newInstance(items: ArrayList<OnboardingPageInfo>) = OnboardingFragment().apply {
            arguments = bundleOf(ARG_PAGE_INFO_ITEMS to items)
        }
    }
}