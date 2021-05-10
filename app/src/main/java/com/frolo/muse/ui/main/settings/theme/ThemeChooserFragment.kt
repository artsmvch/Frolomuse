package com.frolo.muse.ui.main.settings.theme

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.Screen
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.Theme
import com.frolo.muse.repository.Preferences
import com.frolo.muse.ui.ThemeHandler
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.base.setupNavigation
import kotlinx.android.synthetic.main.fragment_theme_chooser.*


class ThemeChooserFragment : BaseFragment(), NoClipping, ThemePageCallback {

    private val preferences: Preferences by prefs()

    private val viewModel: ThemeChooserViewModel by viewModel()

    private val themePageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val pageCount = vp_themes.adapter?.itemCount ?: 0
            tv_theme_page.text = getString(R.string.page_s_of_s, (position + 1), pageCount)
        }
    }

    private var lastKnownPagerPosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_theme_chooser, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(tb_actions)

        val isLandscape = Screen.isLandscape(view.context)

        if (isLandscape) {
            rv_themes.layoutManager = GridLayoutManager(
                    view.context, 1, GridLayoutManager.HORIZONTAL, false)
            rv_themes.adapter = SimpleThemePageAdapter(
                currentTheme = preferences.theme,
                requestManager = Glide.with(this),
                callback = this
            )
            rv_themes.overScrollMode = View.OVER_SCROLL_NEVER
            rv_themes.isVisible = true
            group_pager.isVisible = false
        } else {
            vp_themes.apply {
                ThemePageCarouselHelper.setup(this)
                adapter = ThemePageAdapter(this@ThemeChooserFragment)
                registerOnPageChangeCallback(themePageCallback)
                requestTransform()
            }
            rv_themes.isVisible = false
            group_pager.isVisible = true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_PAGE_INDEX)) {
            lastKnownPagerPosition = savedInstanceState.getInt(STATE_PAGE_INDEX, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        // Here, we also have to remember the last known position,
        // cause the user can only switch between the navigation tabs,
        // in which case onSaveInstanceState may not be called.
        lastKnownPagerPosition = vp_themes.currentItem
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // The view may be null at this step
        vp_themes?.also { safeViewPager ->
            outState.putInt(STATE_PAGE_INDEX, safeViewPager.currentItem)
        }
    }

    override fun onDestroyView() {
        vp_themes.unregisterOnPageChangeCallback(themePageCallback)
        super.onDestroyView()
    }

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                safeView.updatePadding(bottom = bottom)
                safeView.clipToPadding = false
            }
        }
    }

    override fun onProBadgeClick(page: ThemePage) {
        viewModel.onProBadgeClick(page)
    }

    override fun onApplyThemeClick(page: ThemePage) {
        viewModel.onApplyThemeClick(page)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            Toast.makeText(requireContext(), err.message.orEmpty(), Toast.LENGTH_SHORT).show()
        }

        isLoading.observe(owner) { isLoading ->
            val isLandscape = Screen.isLandscape(requireContext())
            if (isLoading == true) {
                if (isLandscape) {
                    rv_themes.visibility = View.INVISIBLE
                } else {
                    group_pager.visibility = View.INVISIBLE
                }
                progress_bar.visibility = View.VISIBLE
            } else {
                if (isLandscape) {
                    rv_themes.visibility = View.VISIBLE
                } else {
                    group_pager.visibility = View.VISIBLE
                }
                progress_bar.visibility = View.GONE
            }
        }

        themeItems.observe(owner) { items ->
            val isLandscape = Screen.isLandscape(requireContext())
            if (isLandscape) {
                (rv_themes.adapter as? SimpleThemePageAdapter)?.pages = items.orEmpty()
            } else {
                (vp_themes.adapter as? ThemePageAdapter)?.pages = items.orEmpty()
                // Restore the position if needed
                lastKnownPagerPosition?.also { targetPosition ->
                    lastKnownPagerPosition = null
                    vp_themes.setCurrentItem(targetPosition, false)
                }
            }
        }

        applyThemeEvent.observeNonNull(owner) { theme ->
            applyTheme(theme)
        }
    }

    private fun applyTheme(theme: Theme) {
        val activity: Activity = this.activity ?: return
        if (activity is ThemeHandler) {
            activity.handleThemeChange()
        } else {
            activity.recreate()
        }
    }

    companion object {

        private const val STATE_PAGE_INDEX = "page_index"

        fun newInstance(): ThemeChooserFragment = ThemeChooserFragment()
    }

}