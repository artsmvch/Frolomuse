package com.frolo.muse.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.view.ActionMode
import androidx.core.app.ActivityOptionsCompat
import androidx.core.graphics.Insets
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observeNonNull
import com.frolo.core.ui.systembars.defaultSystemBarsHost
import com.frolo.muse.R
import com.frolo.muse.di.ActivityComponent
import com.frolo.muse.di.ActivityComponentHolder
import com.frolo.muse.di.applicationComponent
import com.frolo.muse.di.modules.ActivityModule
import com.frolo.muse.onboarding.Onboarding
import com.frolo.muse.router.AppRouter
import com.frolo.muse.router.AppRouterDelegate
import com.frolo.muse.router.AppRouterStub
import com.frolo.core.ui.marker.ThemeHandler
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.muse.ui.base.SimpleFragmentNavigator
import com.frolo.music.model.*
import com.frolo.ui.ActivityUtils
import java.util.*


class MainActivity :
    BaseActivity(),
    SimpleFragmentNavigator,
    ThemeHandler,
    ActivityComponentHolder,
    AppRouter.Provider,
    MainFragment.OnFinishCallback {

    override val activityComponent: ActivityComponent by lazy { buildActivityComponent() }

    private val properties by lazy { MainScreenProperties(this) }

    private val activeActionModes = LinkedList<ActionMode>()

    private val mainSheetsStateViewModel by lazy { provideMainSheetStateViewModel() }

    private var mainFragment: MainFragment? = null

    private var pendingIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadUi()
        if (shouldShowOnboarding()) {
            showOnboarding()
        } else {
            ensureMainFragment()
            handleIntentImpl(intent)
        }
        observeMainSheetsState(this)
    }

    private fun loadUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        WindowInsetsHelper.setupWindowInsets(findViewById(R.id.container)) { view, windowInsets ->
            val systemBarsInsets = windowInsets.systemWindowInsets
            val insetLeft = systemBarsInsets.left
            val insetRight = systemBarsInsets.right
            // Apply and consume only side insets
            val newSystemBarsInsets = Insets.of(0, systemBarsInsets.top,
                0, systemBarsInsets.bottom)
            val newWindowInsets = WindowInsetsCompat.Builder(windowInsets)
                .setInsets(WindowInsetsCompat.Type.systemBars(), newSystemBarsInsets)
                .build()
            view.updatePadding(
                left = insetLeft,
                right = insetRight
            )
            return@setupWindowInsets newWindowInsets
        }
    }

    private fun shouldShowOnboarding(): Boolean {
        // TODO: enable onboarding
        return false
//        val isFirstLaunch = activityComponent.provideAppLaunchInfoProvider().isFirstLaunch
//        return isFirstLaunch && !Onboarding.isOnboardingPassed(this)
    }

    private fun showOnboarding() {
        val launcher = activityResultRegistry.register(
            KEY_SHOW_ONBOARDING,
            object : ActivityResultContract<Any, Boolean>() {
                override fun createIntent(context: Context, input: Any?): Intent {
                    return Onboarding.createOnboardingIntent(context)
                }

                override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
                    return resultCode == Activity.RESULT_OK
                }
            },
            ActivityResultCallback<Boolean> {
                ensureMainFragment()
            }
        )
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this, R.anim.fade_in, R.anim.fade_out)
        launcher.launch(Any(), options)
    }

    private fun ensureMainFragment(): MainFragment {
        val currFragment = supportFragmentManager.findFragmentByTag(FRAG_TAG_MAIN)
            as? MainFragment
        val mainFragment: MainFragment = if (currFragment != null) {
            currFragment
        } else {
            val newFragment = MainFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, newFragment, FRAG_TAG_MAIN)
                .commitNow()
            newFragment
        }
        this.mainFragment = mainFragment
        return mainFragment
    }

    private fun buildActivityComponent(): ActivityComponent {
        return applicationComponent.activityComponent(
            activityModule = ActivityModule()
        )
    }

    override fun getRouter(): AppRouter {
        if (ActivityUtils.isFinishingOrDestroyed(this)) {
            // At this point, the activity's router is no longer valid
            return AppRouterStub()
        }
        return AppRouterDelegate { mainFragment?.getRouter() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentImpl(intent)
    }

    override fun onBackPressed() {
        val result = mainFragment?.handleOnBackPressed() ?: false
        if (!result) {
            super.onBackPressed()
        }
    }

    override fun pushFragment(newFragment: Fragment) {
        mainFragment?.pushFragment(newFragment)
    }

    override fun pop() {
        mainFragment?.pop()
    }

    override fun pushDialog(newDialog: DialogFragment) {
        mainFragment?.pushFragment(newDialog)
    }

    private fun observeMainSheetsState(owner: LifecycleOwner) = with(mainSheetsStateViewModel) {
        isDimmed.observeNonNull(owner) { isDimmed ->
            if (isDimmed) {
                activeActionModes.forEach(ActionMode::finish)
                activeActionModes.clear()
            }
        }
    }

    private fun handleIntentImpl(intent: Intent) {
        val result = mainFragment?.handleIntent(intent)
        pendingIntent = if (result != true) {
            intent
        } else {
            null
        }
    }

    override fun onSupportActionModeStarted(mode: ActionMode) {
        super.onSupportActionModeStarted(mode)
        activeActionModes.add(mode)
    }

    override fun onSupportActionModeFinished(mode: ActionMode) {
        super.onSupportActionModeFinished(mode)
        activeActionModes.remove(mode)
    }

    override fun onWindowStartingSupportActionMode(callback: ActionMode.Callback): ActionMode {
        return MainActionMode(delegate, findViewById(R.id.action_mode), defaultSystemBarsHost,
            callback).apply { createAndShow() }
    }

    override fun handleThemeChange() {
        if (pendingIntent != null) {
            // Saving the not handled intent
            intent = pendingIntent
        }
        recreate()
    }

    companion object {
        @Deprecated("Use ActivityResultLauncher")
        private const val RC_SHOW_GREETINGS = 5713

        private const val KEY_SHOW_ONBOARDING = "com.frolo.muse.ui.main:show_onboarding"

        private const val FRAG_TAG_MAIN = "com.frolo.muse.ui.main:MAIN_FRAGMENT"

        @JvmStatic
        fun newIntent(context: Context, openPlayer: Boolean): Intent =
            Intent(context, MainActivity::class.java)
                .putExtra(MainFragment.EXTRA_OPEN_PLAYER, openPlayer)

        private fun newNavMediaIntent(context: Context, media: Media): Intent {
            return Intent(context, MainActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .putExtra(MainFragment.EXTRA_NAV_KIND_OF_MEDIA, media.getMediaId().getKind())
                .putExtra(MainFragment.EXTRA_NAV_MEDIA_ID, media.getMediaId().getSourceId())
        }

        @JvmStatic
        fun newSongIntent(context: Context, song: Song): Intent =
            newNavMediaIntent(context, song)

        @JvmStatic
        fun newAlbumIntent(context: Context, album: Album): Intent =
            newNavMediaIntent(context, album)

        @JvmStatic
        fun newArtistIntent(context: Context, artist: Artist): Intent =
            newNavMediaIntent(context, artist)

        @JvmStatic
        fun newGenreIntent(context: Context, genre: Genre): Intent =
            newNavMediaIntent(context, genre)

        @JvmStatic
        fun newPlaylistIntent(context: Context, playlist: Playlist): Intent =
            newNavMediaIntent(context, playlist)

        @JvmStatic
        fun newMyFileIntent(context: Context, myFile: MyFile): Intent =
            newNavMediaIntent(context, myFile)
    }

}