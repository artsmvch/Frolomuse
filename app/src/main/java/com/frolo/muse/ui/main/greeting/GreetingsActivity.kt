package com.frolo.muse.ui.main.greeting

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.frolo.muse.R
import com.frolo.muse.rx.disposeOnPauseOf
import com.frolo.muse.rx.subscribeSafely
import com.frolo.muse.ui.base.BaseActivity


class GreetingsActivity : BaseActivity(), GreetingsFragment.OnGreetingFinishedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        makeFullscreen()

        FrameLayout(this).also { layout ->
            layout.id = containerId
            setContentView(layout)
        }
        ensureGreetingsFragment()
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
        preferences.markGreetingsShown()
            .subscribeSafely()
            .disposeOnPauseOf(this)
    }

    override fun onPostResume() {
        super.onPostResume()
    }

    private fun ensureGreetingsFragment() {
        if (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_GREETINGS) == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerId, createGreetingsFragment(), FRAGMENT_TAG_GREETINGS)
                .commit()
        }
    }

    private fun createGreetingsFragment(): GreetingsFragment {
        val items = ArrayList<GreetingPageInfo>()

        GreetingPageInfo(
            imageId = R.drawable.ic_greeting1,
            titleId = R.string.greeting1_title,
            descriptionId = R.string.greeting1_desc,
            colorId = R.color.greeting_color1
        ).also { items.add(it) }

        GreetingPageInfo(
            imageId = R.drawable.ic_greeting2,
            titleId = R.string.greeting2_title,
            descriptionId = R.string.greeting2_desc,
            colorId = R.color.greeting_color2
        ).also { items.add(it) }

        GreetingPageInfo(
            imageId = R.drawable.ic_greeting3,
            titleId = R.string.greeting3_title,
            descriptionId = R.string.greeting3_desc,
            colorId = R.color.greeting_color3
        ).also { items.add(it) }

        return GreetingsFragment.newInstance(items)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onGreetingFinished() {
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.enter_activity, R.anim.exit_activity)
    }

    companion object {
        private const val FRAGMENT_TAG_GREETINGS = "com.frolo.muse.ui.main.greeting:greetings"

        private val containerId: Int = View.generateViewId()

        fun show(context: Context) {
            val intent = Intent(context, GreetingsActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
            context.startActivity(intent, options.toBundle())
        }
    }
}