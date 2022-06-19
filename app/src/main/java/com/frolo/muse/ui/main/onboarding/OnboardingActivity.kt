package com.frolo.muse.ui.main.onboarding

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.frolo.muse.R
import com.frolo.muse.di.applicationComponent
import com.frolo.muse.repository.OnboardingPreferences
import com.frolo.muse.rx.disposeOnPauseOf
import com.frolo.muse.rx.subscribeSafely
import com.frolo.muse.ui.base.BaseActivity


class OnboardingActivity : BaseActivity(), OnboardingFragment.OnOnboardingFinishedListener {

    private val onboardingPreferences: OnboardingPreferences by lazy {
        applicationComponent.provideOnboardingPreferences()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        makeFullscreen()
        FrameLayout(this).also { layout ->
            layout.id = FRAGMENT_CONTAINER_ID
            setContentView(layout)
        }
        ensureOnboardingFragment()
        overridePendingTransition(R.anim.enter_activity, R.anim.exit_activity)
    }

    private fun makeFullscreen() {
        val window = this.window ?: return
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun onRestart() {
        super.onRestart()
        makeFullscreen()
    }

    override fun onResume() {
        super.onResume()
        onboardingPreferences.markOnboardingDone()
            .subscribeSafely()
            .disposeOnPauseOf(this)
    }

    private fun ensureOnboardingFragment() {
        if (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_ONBOARDING) == null) {
            supportFragmentManager.beginTransaction()
                .replace(FRAGMENT_CONTAINER_ID, createOnboardingFragment(), FRAGMENT_TAG_ONBOARDING)
                .commitNow()
        }
    }

    private fun createOnboardingFragment(): OnboardingFragment {
        val items = ArrayList<OnboardingPageInfo>(3)

        OnboardingPageInfo(
            imageId = R.drawable.ic_onboarding1,
            titleId = R.string.onboarding1_title,
            descriptionId = R.string.onboarding1_desc,
            colorId = R.color.onboarding_color1
        ).also { items.add(it) }

        OnboardingPageInfo(
            imageId = R.drawable.ic_onboarding2,
            titleId = R.string.onboarding2_title,
            descriptionId = R.string.onboarding2_desc,
            colorId = R.color.onboarding_color2
        ).also { items.add(it) }

        OnboardingPageInfo(
            imageId = R.drawable.ic_onboarding3,
            titleId = R.string.onboarding3_title,
            descriptionId = R.string.onboarding3_desc,
            colorId = R.color.onboarding_color3
        ).also { items.add(it) }

        return OnboardingFragment.newInstance(items)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onOnboardingFinished(result: OnboardingResult) {
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_activity, R.anim.exit_activity)
    }

    companion object {
        private const val FRAGMENT_TAG_ONBOARDING = "com.frolo.muse.ui.main.onboarding:onboarding"

        private val FRAGMENT_CONTAINER_ID: Int by lazy { View.generateViewId() }

        fun newIntent(context: Context): Intent {
            return Intent(context, OnboardingActivity::class.java)
        }

        fun show(context: Context) {
            val intent = Intent(context, OnboardingActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
            context.startActivity(intent, options.toBundle())
        }
    }
}