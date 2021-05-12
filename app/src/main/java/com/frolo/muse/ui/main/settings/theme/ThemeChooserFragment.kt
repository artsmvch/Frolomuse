package com.frolo.muse.ui.main.settings.theme

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.Theme
import com.frolo.muse.repository.Preferences
import com.frolo.muse.ui.ThemeHandler
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.base.setupNavigation
import kotlinx.android.synthetic.main.fragment_theme_chooser.*
import kotlinx.android.synthetic.main.include_theme_pager.view.*


class ThemeChooserFragment : BaseFragment(), NoClipping, ThemePageCallback {

    private val preferences: Preferences by prefs()

    private val viewModel: ThemeChooserViewModel by viewModel()

    private var absThemePageAdapter: AbsThemePageAdapter? = null

    private val themePageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val pageCount: Int = absThemePageAdapter?.pageCount ?: 0
            val text: String = getString(R.string.page_s_of_s, (position + 1), pageCount)
            include_theme_pager?.tv_theme_page?.text = text
        }
    }

    private var lastKnownPagerPosition: Int? = null

    /**
     * In portrait orientation, this should return a non-null ViewPager2.
     * In this case, [themeRecyclerView] would be null.
     */
    private val themeViewPager: ViewPager2?
        get() {
            view ?: return null
            return include_theme_pager?.vp_themes as? ViewPager2
        }

    /**
     * In landscape orientation, this should return a non-null RecyclerView.
     * In this case, [themeViewPager] would be null.
     */
    private val themeRecyclerView: RecyclerView?
        get() {
            view ?: return null
            return include_theme_pager as? RecyclerView
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_theme_chooser, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(tb_actions)

        val hostFragment: Fragment = this
        val callback: ThemePageCallback = this

        themeViewPager?.apply {
            ThemePageCarouselHelper.setup(this)
            adapter = ThemePageFragmentAdapter(hostFragment).also {
                absThemePageAdapter = it
            }
            registerOnPageChangeCallback(themePageCallback)
            requestTransform()
        }

        themeRecyclerView?.apply {
            layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = SimpleThemePageAdapter(
                currentTheme = preferences.theme,
                requestManager = Glide.with(hostFragment),
                callback = callback
            ).also { absThemePageAdapter = it }
            overScrollMode = View.OVER_SCROLL_NEVER
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
        lastKnownPagerPosition = themeViewPager?.currentItem
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // The view may be null at this step
        themeViewPager?.also { safeViewPager ->
            outState.putInt(STATE_PAGE_INDEX, safeViewPager.currentItem)
        }
    }

    override fun onDestroyView() {
        themeViewPager?.unregisterOnPageChangeCallback(themePageCallback)
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
            if (isLoading == true) {
                include_theme_pager.visibility = View.INVISIBLE
                progress_bar.visibility = View.VISIBLE
            } else {
                include_theme_pager.visibility = View.VISIBLE
                progress_bar.visibility = View.GONE
            }
        }

        themeItems.observe(owner) { items ->
            absThemePageAdapter?.pages = items.orEmpty()
            // Restore the position if needed
            lastKnownPagerPosition?.also { targetPosition ->
                lastKnownPagerPosition = null
                themeViewPager?.setCurrentItem(targetPosition, false)
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