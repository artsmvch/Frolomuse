package com.frolo.muse.ui.main.settings.theme

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.Theme
import com.frolo.muse.ui.ThemeHandler
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.base.setupNavigation
import kotlinx.android.synthetic.main.fragment_theme_chooser.*


class ThemeChooserFragment : BaseFragment(), NoClipping, ThemePageFragment.ThemePageCallback {

    private val viewModel: ThemeChooserViewModel by viewModel()

    private val themePageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val pageCount = vp_themes.adapter?.itemCount ?: 0
            tv_theme_page.text = getString(R.string.page_s_of_s, (position + 1), pageCount)
        }
    }

    private var pendingRestoredPosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_theme_chooser, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigation(tb_actions)

        vp_themes.apply {
            ThemePageCarouselHelper.setup(this)
            adapter = ThemePageAdapter(this@ThemeChooserFragment)
            registerOnPageChangeCallback(themePageCallback)
            requestTransform()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_PAGE_INDEX)) {
            pendingRestoredPosition = savedInstanceState.getInt(STATE_PAGE_INDEX, 0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_PAGE_INDEX, vp_themes.currentItem)
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
        themeItems.observe(owner) { items ->
            (vp_themes.adapter as? ThemePageAdapter)?.pages = items.orEmpty()
            pendingRestoredPosition?.also { targetPosition ->
                pendingRestoredPosition = null
                vp_themes.setCurrentItem(targetPosition, false)
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