package com.frolo.muse.ui.main.library

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.viewpager.widget.ViewPager
import com.frolo.muse.R
import com.frolo.muse.admob.AdListenerBuilder
import com.frolo.muse.admob.BannerState
import com.frolo.muse.android.displayCompat
import com.frolo.muse.arch.observe
import com.frolo.muse.model.Library
import com.frolo.muse.repository.Preferences
import com.frolo.muse.ui.base.BackPressHandler
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.removeAllFragmentsNow
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.fragment_library.*


class LibraryFragment: BaseFragment(),
        BackPressHandler,
        NoClipping {

    private val preferences: Preferences by prefs()

    private var sections: List<@Library.Section Int>? = null

    private val viewModel by viewModel<LibraryViewModel>()

    private var adView: AdView? = null

    private val onPageChangeCallback = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) = Unit

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            // it's better to check only if position offset is in range -0,1..0,1
            if (positionOffset > -0.1 && positionOffset < 0.1) {
                invalidateFab()
            }
        }

        override fun onPageSelected(position: Int) {
            invalidateFab()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_library, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(tb_actions)
            title = getString(R.string.nav_library)
        }

        // Retrieving library sections from the preferences should be a synchronous operation
        val actualSections = preferences.librarySections
                .filter { section -> preferences.isLibrarySectionEnabled(section) }

        if (actualSections != sections) {
            // It's a compelled workaround to prevent the action bar from adding menus
            // of previous fragments that are not at the current position
            childFragmentManager.removeAllFragmentsNow()
            sections = actualSections
        }

        vp_sections.apply {
            adapter = LibraryPageAdapter(childFragmentManager, context).apply {
                sections = actualSections
            }

            // Registering OnPageChangeListener should go after the adapter is set up
            addOnPageChangeListener(onPageChangeCallback)
        }

        tl_sections.setupWithViewPager(vp_sections)

        fab_action.setOnClickListener {
            val adapter = vp_sections.adapter as? LibraryPageAdapter
            val currFragment = adapter?.getPageAt(vp_sections.currentItem)
            if (currFragment is FabCallback && currFragment.isUsingFab()) {
                currFragment.handleClickOnFab()
            }
        }

        invalidateFab()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    /**
     * Creates a new [AdView] and setups its size and unit ID, then loads an ad for it.
     * The ad view will be added to the ad container.
     * A reference to the newly created AdView is stored to manage its lifecycle.
     */
    private fun setupAdMobBanner(state: BannerState?) {

        if (state?.canBeShown == true && adView != null) {
            // it's already set up
            return
        }

        if (state == null || !state.canBeShown) {
            // we don't have to show the ad
            ad_container.removeAllViews()
            adView = null
            return
        }

        val context = requireContext()
        val adView = AdView(context)
        adView.adSize = getAdSize()
        adView.adUnitId = state.bannerId

        AdListenerBuilder()
            .doDefaultLogging(LOG_TAG)
            .doWhenAdLoaded {
                setAdContainerVisible(isVisible = true, animate = true)
            }
            .doWhenAdFailedToLoad {
                setAdContainerVisible(isVisible = false, animate = true)
            }
            .buildAndSetIn(adView)

        // The AD request
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // making sure that there are no unnecessary views
        ad_container.removeAllViews()
        // adding the AdView to the container
        ad_container.addView(adView)

        setAdContainerVisible(isVisible = false, animate = true)

        this.adView = adView
    }

    private fun getAdSize(): AdSize {
        val context = requireContext()
        val display = context.displayCompat ?: return AdSize.SMART_BANNER
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val adWidth = (outMetrics.widthPixels / outMetrics.density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    private fun setAdContainerVisible(isVisible: Boolean, animate: Boolean) {
        val targetVisibility = if (isVisible) View.VISIBLE else View.GONE

        if (ad_container.visibility == targetVisibility) {
            return
        }

        val rootView = view
        if (animate && rootView is ViewGroup) {
            // use the transition mechanism to animate layout changes
            val transition = AutoTransition().apply {
                duration = 200L
            }
            TransitionManager.beginDelayedTransition(rootView, transition)
        }
        ad_container.visibility = targetVisibility
    }

    override fun onResume() {
        super.onResume()
        adView?.resume()
    }

    override fun onPause() {
        super.onPause()
        adView?.pause()
    }

    override fun onDestroyView() {
        vp_sections.removeOnPageChangeListener(onPageChangeCallback)

        adView?.destroy()
        adView = null

        super.onDestroyView()
    }

    override fun onBackPress(): Boolean {
        val position = vp_sections.currentItem
        val adapter = vp_sections.adapter as? LibraryPageAdapter ?: return false
        val fragment: BackPressHandler = adapter.getPageAt(position) as? BackPressHandler ?: return false
        return fragment.onBackPress()
    }

    private fun invalidateFab() {
        val adapter = vp_sections.adapter as? LibraryPageAdapter
        val currFragment = adapter?.getPageAt(vp_sections.currentItem)
        if (currFragment is FabCallback && currFragment.isUsingFab()) {
            currFragment.decorateFab(fab_action)
            fab_action.show()
        } else {
            fab_action.hide()
        }
    }

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
        fab_action.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin += left
            topMargin += top
            rightMargin += right
            bottomMargin += bottom
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        bannerState.observe(owner) { state ->
            setupAdMobBanner(state)
        }
    }

    companion object {

        private const val LOG_TAG = "LibraryFragment"

        // Factory
        fun newInstance() = LibraryFragment()

    }
}