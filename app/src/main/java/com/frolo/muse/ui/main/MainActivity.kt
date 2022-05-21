package com.frolo.muse.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.frolo.muse.di.ActivityComponent
import com.frolo.muse.di.ActivityComponentHolder
import com.frolo.muse.di.applicationComponent
import com.frolo.muse.di.modules.ActivityModule
import com.frolo.muse.router.AppRouter
import com.frolo.muse.router.AppRouterStub
import com.frolo.muse.ui.ThemeHandler
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.muse.ui.base.SimpleFragmentNavigator
import com.frolo.music.model.*
import com.frolo.ui.ActivityUtils
import java.util.*


class MainActivity : BaseActivity(),
    SimpleFragmentNavigator,
    ThemeHandler,
    ActivityComponentHolder,
    AppRouter.Provider,
    MainFragment.OnFinishCallback,
    MainFragment.OnDimmedCallback {

    override val activityComponent: ActivityComponent by lazy { buildActivityComponent() }

    private val activeActionModes = LinkedList<ActionMode>()
    private val properties by lazy { MainScreenProperties(this) }

    private var mainFragment: MainFragment? = null

    private var pendingIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = FrameLayout(this).apply {
            id = FRAGMENT_CONTAINER_ID
        }
        setContentView(contentView)
        mainFragment = ensureMainFragment()
        handleIntentImpl(intent)
    }

    private fun ensureMainFragment(): MainFragment {
        val fragment = supportFragmentManager.findFragmentByTag(FRAG_TAG_MAIN)
            as? MainFragment
        if (fragment != null) {
            return fragment
        }
        val newFragment = MainFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(FRAGMENT_CONTAINER_ID, newFragment, FRAG_TAG_MAIN)
            .commitNow()
        return newFragment
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
        return mainFragment?.getRouter() ?: AppRouterStub()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentImpl(intent)
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

    override fun onDimmed() {
        activeActionModes.forEach { it.finish() }
        activeActionModes.clear()
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
        window?.statusBarColor = properties.actionModeBackgroundColor
    }

    override fun onSupportActionModeFinished(mode: ActionMode) {
        super.onSupportActionModeFinished(mode)
        activeActionModes.remove(mode)
        if (activeActionModes.isEmpty()) {
            window?.statusBarColor = properties.colorPrimaryDark
        }
    }

    override fun handleThemeChange() {
        if (pendingIntent != null) {
            // Saving the not handled intent
            intent = pendingIntent
        }
        recreate()
    }

    companion object {
        private const val FRAG_TAG_MAIN = "com.frolo.muse.ui.main:MAIN_FRAGMENT"

        private val FRAGMENT_CONTAINER_ID by lazy { View.generateViewId() }

        @JvmStatic
        fun newIntent(context: Context, openPlayer: Boolean): Intent =
            Intent(context, MainActivity::class.java)
                .putExtra(MainFragment.EXTRA_OPEN_PLAYER, openPlayer)

        private fun newNavMediaIntent(context: Context, media: Media): Intent {
            return Intent(context, MainActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .putExtra(MainFragment.EXTRA_NAV_KIND_OF_MEDIA, media.kind)
                .putExtra(MainFragment.EXTRA_NAV_MEDIA_ID, media.id)
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