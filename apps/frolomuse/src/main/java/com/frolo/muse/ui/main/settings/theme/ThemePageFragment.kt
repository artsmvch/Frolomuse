package com.frolo.muse.ui.main.settings.theme

import android.animation.TimeInterpolator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.databinding.FragmentThemePageBinding
import com.frolo.muse.model.ThemeUtils
import com.frolo.muse.rx.disposeOnStopOf
import com.frolo.muse.rx.subscribeSafely
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.tryHostAs
import com.frolo.ui.Screen
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import kotlin.math.exp
import kotlin.math.sin


class ThemePageFragment : BaseFragment() {
    private var _binding: FragmentThemePageBinding? = null
    private val binding: FragmentThemePageBinding get() = _binding!!

    private val preferences by prefs()

    /**
     * The initial theme page argument, which is set by the [androidx.fragment.app.Fragment.setArguments] method.
     * Cannot be null.
     */
    private val initialArgument: ThemePage by lazy {
        requireArguments().getParcelable<ThemePage>(ARG_THEME_PAGE) as ThemePage
    }

    /**
     * The overridden theme page argument, which is set by the [updateArgument] method. Nullable.
     */
    private var overriddenArgument: ThemePage? = null

    private val callback: ThemePageCallback? get() = tryHostAs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overriddenArgument = savedInstanceState?.getParcelable(STATE_OVERRIDDEN_THEME_PAGE) as? ThemePage
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThemePageBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val themePage = getThemePage()
        val context = view.context
        val themeResId = ThemeUtils.getStyleResourceId(themePage.theme)
        if (themeResId != null) {
            // Adding the theme preview fragment
            val previewFragment = ThemePreviewFragment.newInstance(themePage.album, themeResId, 0.625f)
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, previewFragment)
                .commit()
        } else {
            // This is an invalid state
            if (BuildConfig.DEBUG) {
                throw IllegalStateException("Could not find theme res ID: $themePage")
            }
        }

        val isCurrentThemeDark = preferences.theme.isDark
        if (isCurrentThemeDark) {
            binding.fragmentContainerCard.apply {
                strokeWidth = Screen.dp(context, 1f).coerceAtLeast(1)
                strokeColor = if (themePage.theme.isDark) {
                    context.getColor(com.google.android.material.support.R.color.md_grey_500)
                } else {
                    context.getColor(com.google.android.material.support.R.color.md_grey_50)
                }
                cardElevation = 0f
                maxCardElevation = 0f
            }
        } else {
            binding.fragmentContainerCard.apply {
                strokeWidth = 0
                strokeColor = Color.TRANSPARENT
                cardElevation = Screen.dpFloat(1.2f)
                maxCardElevation = Screen.dpFloat(2f)
            }
        }

        binding.imvPreviewProBadge.setOnClickListener {
            callback?.onProBadgeClick(getThemePage())
        }

        binding.btnApplyTheme.setOnClickListener {
            callback?.onApplyThemeClick(getThemePage())
        }

        setupButtons(themePage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        scheduleProBadgeAnimation()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        overriddenArgument?.also { themePage ->
            outState.putParcelable(STATE_OVERRIDDEN_THEME_PAGE, themePage)
        }
    }

    /**
     * The current theme page argument. Returns the overridden argument, if any, or the initial one.
     */
    @UiThread
    private fun getThemePage(): ThemePage {
        return overriddenArgument ?: initialArgument
    }

    private fun setupButtons(page: ThemePage) {
        binding.imvPreviewProBadge.isVisible = page.hasProBadge

        if (page.isApplied) {
            binding.btnApplyTheme.setText(R.string.applied)
            binding.btnApplyTheme.isEnabled = false
        } else {
            binding.btnApplyTheme.setText(R.string.apply_theme)
            binding.btnApplyTheme.isEnabled = true
        }
    }

    @UiThread
    fun updateArgument(themePage: ThemePage) {
        overriddenArgument = themePage
        view ?: return
        setupButtons(themePage)
    }

    private fun scheduleProBadgeAnimation() {
        Observable.interval(1500L, 5000L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { startProBadgeAnimation() }
            .subscribeSafely()
            .disposeOnStopOf(this)
    }

    private fun startProBadgeAnimation() {
        view ?: return

        val badgeView = binding.imvPreviewProBadge

        val frequency = 3f
        val decay = 2f

        val decayingSineWave = TimeInterpolator { input ->
            val raw = sin(frequency * input * 2 * Math.PI)
            (raw * exp((-input * decay).toDouble())).toFloat()
        }

        badgeView.animate()
            .rotationBy(24f)
            .scaleXBy(0.05f)
            .scaleYBy(0.05f)
            .setInterpolator(decayingSineWave)
            .setDuration(600L)
            .start()
    }

    companion object {
        private const val ARG_THEME_PAGE = "theme_page"
        private const val STATE_OVERRIDDEN_THEME_PAGE = "overridden_theme_page"

        fun newInstance(page: ThemePage): ThemePageFragment {
            return ThemePageFragment().apply {
                arguments = bundleOf(ARG_THEME_PAGE to page)
            }
        }
    }

}