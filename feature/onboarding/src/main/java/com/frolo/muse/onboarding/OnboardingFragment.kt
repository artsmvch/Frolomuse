package com.frolo.muse.onboarding

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.frolo.muse.onboarding.databinding.FragmentOnboardingBinding
import com.frolo.ui.Screen
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle


internal class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding: FragmentOnboardingBinding get() = _binding!!

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
    ): View? {
        _binding = FragmentOnboardingBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewPager.apply {
            adapter = OnboardingPageAdapter(pageInfoItems.orEmpty())
            registerOnPageChangeCallback(onPageChangeCallback)
            // workaround for the ViewPager2
            getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER
        }

        binding.indicatorView.apply {
            setSliderColor(
                ContextCompat.getColor(view.context, R.color.onboarding_indicator_color_normal),
                ContextCompat.getColor(view.context, R.color.onboarding_indicator_color_selected)
            )
            setSliderWidth(Screen.dpFloat(context, 10f))
            setIndicatorGap(Screen.dpFloat(context, 20f))
            setSlideMode(IndicatorSlideMode.WORM)
            setIndicatorStyle(IndicatorStyle.CIRCLE)
            setupWithViewPager(binding.viewPager)
        }

        binding.btnSkip.setOnClickListener {
            onOnboardingFinishedListener?.onOnboardingFinished(OnboardingResult.Skipped)
        }

        binding.btnDone.setOnClickListener {
            onOnboardingFinishedListener?.onOnboardingFinished(OnboardingResult.Passed)
        }

        binding.btnNext.setOnClickListener {
            val currentPosition = binding.viewPager.currentItem
            val itemCount = binding.viewPager.adapter?.itemCount ?: 0
            if (currentPosition < itemCount - 1) {
                binding.viewPager.currentItem = currentPosition + 1
            }
        }

        val backgroundArtView = ImageView(context).also { v ->
            Glide.with(this)
                .load(R.drawable.png_onboarding_background)
                //.transform(Rotate(90))
                .into(v)
            v.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        binding.parallax.apply {
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
        binding.imvBackgroundOverlay.setImageDrawable(background)

        binding.viewPager.adapter?.also { safeAdapter ->
            val itemCount = safeAdapter.itemCount
            if (itemCount > 1) {
                val absPosition = (position + positionOffset) / (itemCount - 1)
                binding.parallax.setScrollOffset(absPosition)
            } else {
                binding.parallax.setScrollOffset(0.5f)
            }
        }
    }

    private fun updateButtons(selectedPosition: Int) {
        val transition = Fade().apply {
            duration = 150
        }
        TransitionManager.beginDelayedTransition(binding.flButtons, transition)
        val itemCount = binding.viewPager.adapter?.itemCount ?: 0
        val isLastPage = selectedPosition >= itemCount - 1
        if (isLastPage) {
            binding.btnSkip.visibility = View.INVISIBLE
            binding.btnDone.visibility = View.VISIBLE
            binding.btnNext.visibility = View.INVISIBLE
        } else {
            binding.btnSkip.visibility = View.VISIBLE
            binding.btnDone.visibility = View.INVISIBLE
            binding.btnNext.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        _binding = null
    }

    fun interface OnOnboardingFinishedListener {
        fun onOnboardingFinished(result: OnboardingResult)
    }

    companion object {
        private const val ARG_PAGE_INFO_ITEMS = "page_info_items"

        fun newInstance(items: ArrayList<OnboardingPageInfo>) = OnboardingFragment().apply {
            arguments = bundleOf(ARG_PAGE_INFO_ITEMS to items)
        }
    }
}