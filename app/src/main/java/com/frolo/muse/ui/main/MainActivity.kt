package com.frolo.muse.ui.main

import android.Manifest
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.IdRes
import androidx.appcompat.view.ActionMode
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.frolo.debug.DebugUtils
import com.frolo.muse.*
import com.frolo.muse.R
import com.frolo.muse.android.ViewAppSettingsIntent
import com.frolo.muse.android.getIntExtraOrNull
import com.frolo.muse.android.getIntOrNull
import com.frolo.muse.android.startActivitySafely
import com.frolo.muse.arch.observe
import com.frolo.muse.di.ActivityComponent
import com.frolo.muse.di.ActivityComponentHolder
import com.frolo.muse.di.applicationComponent
import com.frolo.muse.di.impl.navigator.AppRouterImpl
import com.frolo.muse.di.modules.ActivityModule
import com.frolo.muse.router.AppRouter
import com.frolo.muse.router.AppRouterStub
import com.frolo.player.Player
import com.frolo.muse.ui.ScrolledToTop
import com.frolo.muse.ui.ThemeHandler
import com.frolo.muse.ui.base.*
import com.frolo.muse.ui.main.audiofx.AudioFxFragment
import com.frolo.muse.ui.main.library.LibraryFragment
import com.frolo.muse.ui.main.library.search.SearchFragment
import com.frolo.muse.ui.main.player.mini.MiniPlayerFragment
import com.frolo.muse.ui.main.settings.AppBarSettingsFragment
import com.frolo.music.model.*
import com.frolo.ui.ActivityUtils
import com.frolo.ui.FragmentUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehaviorSupport
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.max
import kotlin.math.pow


class MainActivity : BaseActivity(),
        SimpleFragmentNavigator,
        PlayerSheetCallback,
        ThemeHandler,
        ActivityComponentHolder,
        AppRouter.Provider {

    override val activityComponent: ActivityComponent by lazy { buildActivityComponent() }

    private val appRouter by lazy {
        AppRouterImpl(
            context = this,
            navigator = this,
            expandSlidingPlayer = { expandSlidingPlayer() }
        )
    }

    private val viewModel: MainViewModel by lazy {
        val vmFactory = activityComponent.provideViewModelFactory()
        ViewModelProviders.of(this, vmFactory).get(MainViewModel::class.java)
    }

    private var lastSavedInstanceState: Bundle? = null

    private var fragNavController: FragNavController? = null
    private val activeActionModes = LinkedList<ActionMode>()
    private var playerSheetFragment: PlayerSheetFragment? = null

    private var rateDialog: Dialog? = null
    private var resPermissionExplanationDialog: Dialog? = null

    private val properties by lazy { MainActivityProperties(this) }

    private var pendingIntent: Intent? = null

    private val bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                handleSlide(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
        }

    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                if (f is FragmentContentInsetsListener) {
                    val insets = properties.fragmentContentInsets
                    f.applyContentInsets(insets.left, insets.top, insets.right, insets.bottom)
                }
            }

            override fun onFragmentDestroyed(fm: FragmentManager, fragment: Fragment) {
                // https://github.com/ncapdevi/FragNav/issues/246
                maybeClearDialogFragment(fragment)
            }

            private fun maybeClearDialogFragment(destroyedFragment: Fragment) {
                kotlin.runCatching {
                    fragNavController?.doIfStateNotSaved {
                        if (this.currentDialogFrag == destroyedFragment) {
                            this.clearDialogFragment()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        lastSavedInstanceState = savedInstanceState
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadUi()
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)
        observePlayerState()
        handleIntent(intent)
        observeViewModel(this)
        observeScanStatus()
        if (savedInstanceState == null) {
            viewModel.onFirstCreate()
        }
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
        return appRouter
    }

    private fun loadUi() {
        bottom_navigation_view.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(properties.colorSurface)
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, properties.bottomNavigationCornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, properties.bottomNavigationCornerRadius)
                .build()
        }

        sliding_player_layout.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(properties.colorPrimarySurface)
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, properties.bottomNavigationCornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, properties.bottomNavigationCornerRadius)
                .build()
        }
        with(BottomSheetBehavior.from(sliding_player_layout)) {
            addBottomSheetCallback(bottomSheetCallback)
        }
        BottomSheetBehaviorSupport.dispatchOnSlide(sliding_player_layout)

        mini_player_container.setOnClickListener {
            expandSlidingPlayer()
        }
    }

    private fun observeScanStatus() {
        ScanStatusObserver.observe(
            context = this,
            lifecycleOwner = this,
            onScanStarted = {
                postMessage(getString(R.string.scanning_started))
            },
            onScanCompleted = {
                postMessage(getString(R.string.scanning_completed))
            },
            onScanCancelled = {
                // User has cancelled the scanning himself, no need to notify him about that
            }
        )
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
        // The best place to re-try initializing fragments, because after calling
        // super.onStart(), the state of the fragment manager is not saved.
        maybeInitializeFragments(viewModel.player, lastSavedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onStop() {
        viewModel.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        with(BottomSheetBehavior.from(sliding_player_layout)) {
            removeBottomSheetCallback(bottomSheetCallback)
        }
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        lastSavedInstanceState = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            fragNavController?.doIfStateNotSaved {
                // First try to pop a fragment.
                // If out of luck, then finish the activity.
                if (!popFragment()) finish()
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_READ_STORAGE) {
            val index = permissions.indexOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (index >= 0) {
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.onRESPermissionGranted()
                    RESPermissionBus.dispatch()
                } else {
                    viewModel.onRESPermissionDenied()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNavController?.currentStackIndex?.also { stackIndex ->
            outState.putInt(EXTRA_TAB_INDEX, stackIndex)
        }
        fragNavController?.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.isStateSaved) {
            // FragmentManager's state is saved, cannot perform any action on it.
            super.onBackPressed()
            return
        }

        val frag = fragmentManager.findFragmentByTag(FRAG_TAG_PLAYER_SHEET)
        if (frag != null && frag is BackPressHandler && frag.isResumed) {
            // We can delegate the back press only if the fragment is BackPressHandler and is resumed
            if (frag.onBackPress()) {
                // The player sheet fragment successfully handled the back press.
                return
            }
        }

        val behavior = BottomSheetBehavior.from(sliding_player_layout)
        if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            // Collapse the player sheet if it is expanded.
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }

        val controller = this.fragNavController

        if (controller == null || controller.isStateSaved) {
            super.onBackPressed()
            return
        }

        val currentFrag = controller.currentFrag
        if (currentFrag is BackPressHandler && currentFrag.isResumed) {
            if (currentFrag.onBackPress()) {
                // The current fragment successfully handled the back press.
                return
            }
        }

        if (controller.isRootFragment) {
            // Just call finish. Calling onBackPressed() causes popping bac stack from the fragment manager.
            // This will simply removes PlayerHolderFragment and not finish the activity.
            // This is not what we want.
            //super.onBackPressed()
            finish()
        } else if (!controller.popFragment()) { // no fragments left in the stack
            // Just call finish. Calling onBackPressed() causes popping bac stack from the fragment manager.
            // This will simply removes PlayerHolderFragment and not finish the activity.
            // This is not what we want.
            //super.onBackPressed()
            finish()
        }
    }

    override fun pushFragment(newFragment: Fragment) {
        fragNavController?.doIfStateNotSaved {
            pushFragment(newFragment)
            collapseSlidingPlayer()
        }
    }

    override fun pop() {
        fragNavController?.doIfStateNotSaved {
            val currDialogFrag = currentDialogFrag
            if (currDialogFrag != null && currDialogFrag.isAdded) {
                // There's a dialog fragment opened.
                // Clear it first.
                try {
                    clearDialogFragment()
                } catch (error: Throwable) {
                    Logger.e(error)
                }
            } else {
                val stack = currentStack
                if (stack != null && stack.size > 1) {
                    // pop stack only if it's not null and its size is more than 1.
                    popFragments(1)
                } else {
                    // Stack is empty or has only root fragment.
                    // Let's finish the activity.
                    finish()
                }
            }
        }
    }

    override fun pushDialog(newDialog: DialogFragment) {
        fragNavController?.doIfStateNotSaved {
            showDialogFragment(newDialog)
        }
    }

    /**
     * Tries to initialize [FragNavController] and configure navigation related widgets for it.
     * If [player] is null, then this immediately returns false, cause no fragments can work without a non-null player instance.
     * If fragments were initialized earlier, then this also return false.
     */
    private fun maybeInitializeFragments(player: Player?, savedInstanceState: Bundle?): Boolean {
        if (player == null) {
            return false
        }

        if (fragNavController != null) {
            // It was initialized before.
            return false
        }

        val fragmentManager = supportFragmentManager
        if (fragmentManager.isStateSaved) {
            // FragmentManager's state is saved, we cannot perform any operation on it.
            return false
        }

        // Extracting tab index, if any
        val tabIndex: Int =
            savedInstanceState?.getIntOrNull(EXTRA_TAB_INDEX) ?:
            pendingIntent?.getIntExtraOrNull(EXTRA_TAB_INDEX) ?:
            TAB_INDEX_DEFAULT

        fragNavController = FragNavController(fragmentManager, R.id.container).apply {
            defaultTransactionOptions = FragNavTransactionOptions
                .newBuilder()
                .customAnimations(
                    R.anim.screen_fade_in, R.anim.screen_fade_out,
                    R.anim.screen_fade_in, R.anim.screen_fade_out)
                .build()

            rootFragmentListener = object : FragNavController.RootFragmentListener {
                override val numberOfRootFragments = 4

                override fun getRootFragment(index: Int): Fragment {
                    return when (index) {
                        INDEX_LIBRARY ->    LibraryFragment.newInstance()
                        INDEX_EQUALIZER ->  AudioFxFragment.newInstance()
                        INDEX_SEARCH ->     SearchFragment.newInstance()
                        INDEX_SETTINGS ->   AppBarSettingsFragment.newInstance()
                        else -> {
                            DebugUtils.dumpOnMainThread(IllegalStateException(
                                "Unexpected root index: $index"))
                            Fragment()
                        }
                    }
                }

            }

            initialize(tabIndex, savedInstanceState)
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_library -> {
                    fragNavController?.doIfStateNotSaved { switchTab(INDEX_LIBRARY) }
                    true
                }

                R.id.nav_equalizer -> {
                    fragNavController?.doIfStateNotSaved { switchTab(INDEX_EQUALIZER) }
                    true
                }

                R.id.nav_search -> {
                    fragNavController?.doIfStateNotSaved { switchTab(INDEX_SEARCH) }
                    true
                }

                R.id.nav_settings -> {
                    fragNavController?.doIfStateNotSaved { switchTab(INDEX_SETTINGS) }
                    true
                }

                else -> false
            }
        }

        bottom_navigation_view.setOnNavigationItemReselectedListener {
            fragNavController?.doIfStateNotSaved {
                if (isRootFragment) {
                    // If we are at the root fragment, we can try to scroll
                    // to the top of the content of the root fragment
                    currentStack?.also { safeStack ->
                        val root = if (safeStack.size == 1) safeStack.peek() else null
                        if (root != null
                                && root is ScrolledToTop
                                && FragmentUtils.isInForeground(root)) {
                            // The fragment has to be in the foreground
                            root.scrollToTop()
                        }
                    }
                }
                clearStack()
            }
        }

        val targetMenuId = getBottomMenuItemId(tabIndex)
        if (bottom_navigation_view.selectedItemId != targetMenuId) {
            bottom_navigation_view.selectedItemId = targetMenuId
        }

        // Initializing PlayerSheet and MiniPlayer
        val newPlayerSheetFragment = PlayerSheetFragment()

        playerSheetFragment = newPlayerSheetFragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.container_player, newPlayerSheetFragment, FRAG_TAG_PLAYER_SHEET)
            .replace(R.id.mini_player_container, MiniPlayerFragment(), FRAG_TAG_MIN_PLAYER)
            .commit()

        // Finally the root layout can be visible
        cl_root.visibility = View.VISIBLE

        sliding_player_layout.doOnLayout { v ->
            with(BottomSheetBehavior.from(v)) {
                peekHeight = properties.playerSheetPeekHeight
            }
        }

        pendingIntent?.also { safeIntent ->
            handleIntent(safeIntent)
        }

        return true
    }

    @IdRes
    private fun getBottomMenuItemId(tabIndex: Int): Int {
        return when(tabIndex) {
            INDEX_LIBRARY ->    R.id.nav_library
            INDEX_EQUALIZER ->  R.id.nav_equalizer
            INDEX_SEARCH ->     R.id.nav_search
            INDEX_SETTINGS ->   R.id.nav_settings
            else -> {
                DebugUtils.dumpOnMainThread(IllegalStateException(
                    "Unexpected tab index: $tabIndex"))
                R.id.nav_library
            }
        }
    }

    /**
     * Handles the given [intent].
     * If the fragments are not initialized yet then this method does nothing
     * but marks [intent] as not handled (see [notHandledIntent] so that
     * it will be handled later when the fragments are initialized.
     */
    private fun handleIntent(intent: Intent) {
        val safeNavController = fragNavController
        if (safeNavController == null || safeNavController.isStateSaved) {
            // Fragments are not initialized yet, or the state is saved
            pendingIntent = intent
            return
        }

        // Need to clean it to prevent double-handling
        pendingIntent = null

        if (intent.getBooleanExtra(EXTRA_INTENT_HANDLED, false)) {
            // This intent has already been handled
            return
        }

        if (intent.hasExtra(EXTRA_NAV_KIND_OF_MEDIA) && intent.hasExtra(EXTRA_NAV_MEDIA_ID)) {
            val kindOfMedia = intent.getIntExtra(EXTRA_NAV_KIND_OF_MEDIA, Media.NONE)
            val mediaId = intent.getLongExtra(EXTRA_NAV_MEDIA_ID, Media.NO_ID)
            viewModel.onNavigateToMediaIntent(kindOfMedia, mediaId)
        } else {
            val currTabIndex = safeNavController.currentStackIndex
            val tabIndexExtra = intent.getIntExtra(EXTRA_TAB_INDEX, currTabIndex)
            if (tabIndexExtra != currTabIndex) {
                bottom_navigation_view.selectedItemId = when (tabIndexExtra) {
                    INDEX_LIBRARY ->    R.id.nav_library
                    INDEX_EQUALIZER ->  R.id.nav_equalizer
                    INDEX_SEARCH ->     R.id.nav_search
                    INDEX_SETTINGS ->   R.id.nav_settings
                    else ->             R.id.nav_library
                }
            }
        }

        // Check if we need to open the player screen
        if (intent.getBooleanExtra(EXTRA_OPEN_PLAYER, false)) {
            expandSlidingPlayer()
        }

        // Check if we need to open an audio source
        val uri = intent.data
        val action = intent.action
        if (action == Intent.ACTION_VIEW && uri?.scheme == ContentResolver.SCHEME_FILE) {
            uri.path?.apply { viewModel.onOpenAudioSourceIntent(this) }
        }

        // Mark this intent as handled
        intent.putExtra(EXTRA_INTENT_HANDLED, true)
    }

    private fun showRateDialog() {
        rateDialog?.dismiss()

        // First, check if there is an already shown dialog
        fragNavController?.doIfStateNotSaved {
            val currDialog = this.currentDialogFrag
            if (currDialog != null && currDialog.isShowing) {
                // It's better not to show the Rate dialog over the existing dialog
                return
            }
        }

        val dialog = RateDialog(this) { dialog, what ->
            dialog.dismiss()
            when (what) {
                RateDialog.Button.RATE -> viewModel.onRateDialogAnswerYes()

                RateDialog.Button.NO -> viewModel.onRateDialogAnswerNo()

                RateDialog.Button.REMIND_LATER -> viewModel.onRateDialogAnswerRemindLater()
            }
        }

        rateDialog = dialog.apply {
            setOnCancelListener { viewModel.onCancelledRateDialog() }
            show()
        }
    }

    private fun requestRESPermission() {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.onRESPermissionGranted()
        } else {
            requestPermissions(arrayOf(permission), RC_READ_STORAGE)
        }
    }

    private fun explainNeedForRESPermission() {
        resPermissionExplanationDialog?.cancel()
        resPermissionExplanationDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_denied)
            .setMessage(R.string.need_for_res_permission_explanation)
            .setCancelable(false)
            .setNegativeButton(R.string.cancel) { _, _ ->
                viewModel.onDeniedRESPermissionExplanation()
            }
            .setPositiveButton(R.string.grant_res_permission) { _, _ ->
                viewModel.onAgreedWithRESPermissionExplanation()
            }
            .show()
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isSnowfallEnabled.observe(owner) { isEnabled ->
            val transition = Fade().apply {
                duration = 150L
                addTarget(snowfall_view)
            }
            TransitionManager.beginDelayedTransition(cl_root, transition)
            if (isEnabled == true) {
                snowfall_view.visibility = View.VISIBLE
            } else {
                snowfall_view.visibility = View.GONE
            }
        }

        askToRateEvent.observe(owner) {
            showRateDialog()
        }

        askRESPermissionsEvent.observe(owner) {
            requestRESPermission()
        }

        explainNeedForRESPermissionEvent.observe(owner) {
            explainNeedForRESPermission()
        }

        openPermissionSettingsEvent.observe(owner) {
            val intent: Intent = ViewAppSettingsIntent(this@MainActivity)
            if (!startActivitySafely(intent)) {
                requestRESPermission()
            }
        }
    }

    private fun observePlayerState() {
        val owner = PlayerStateLifecycleOwner(this)
        viewModel.playerLiveData.observe(owner) { player: Player? ->
            if (player != null) {
                // The player is connected: let's try initializing fragments
                maybeInitializeFragments(player, lastSavedInstanceState)
            } else {
                // The player is disconnected: no need to stay here anymore
                FragmentUtils.popAllBackStackEntriesImmediate(supportFragmentManager)
                FragmentUtils.removeAllFragmentsNow(supportFragmentManager)
                lastSavedInstanceState = null
            }
        }
        viewModel.isDisconnectedLiveData.observe(owner) { isDisconnected: Boolean? ->
            if (isDisconnected == true) {
                finish()
            }
        }
    }

    private fun expandSlidingPlayer() {
        fragNavController?.doIfStateNotSaved {
            // Wrapping in a try-catch, because sometimes it crashes
            try {
                clearDialogFragment()
            } catch (error: Throwable) {
                Logger.e(error)
            }
        }
        BottomSheetBehavior.from(sliding_player_layout).state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun collapseSlidingPlayer() {
        BottomSheetBehavior.from(sliding_player_layout).state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun handleSlide(slideOffset: Float) {
        bottom_navigation_view.also { child ->
            val overTranslation = 1.2f
            val heightToAnimate = slideOffset * child.height * overTranslation
            child.animate()
                .translationY(heightToAnimate)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(0)
                .start()
        }

        view_dim_overlay.alpha = 1 - (1 - slideOffset).pow(2)

        mini_player_container.alpha = max(0f, 1f - slideOffset * 4)
        mini_player_container.touchesDisabled = slideOffset > 0.4

        (sliding_player_layout.background as? MaterialShapeDrawable)?.apply {
            val blendRatio = max(0f, 1f - slideOffset * 2)
            val blendedColor = ColorUtils.blendARGB(properties.colorSurface,
                properties.colorPrimarySurface, blendRatio)
            fillColor = ColorStateList.valueOf(blendedColor)

            val factoredCornerRadius = properties.playerSheetCornerRadius * (1 - slideOffset)
            this.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, factoredCornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, factoredCornerRadius)
                .build()
        }

        if (slideOffset > 0.6) {
            activeActionModes.forEach { it.finish() }
            activeActionModes.clear()
        }

        playerSheetFragment?.onSlideOffset(slideOffset)
    }

    override fun setPlayerSheetDraggable(draggable: Boolean) {
        BottomSheetBehavior.from(sliding_player_layout).apply {
            isDraggable = draggable
        }
    }

    override fun requestCollapse() {
        collapseSlidingPlayer()
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

        private const val RC_READ_STORAGE = 1043

        // Fragment tags
        private const val FRAG_TAG_PLAYER_SHEET = "com.frolo.muse.ui.main.PLAYER_SHEET"
        private const val FRAG_TAG_MIN_PLAYER = "com.frolo.muse.ui.main.MINI_PLAYER"

        private const val EXTRA_INTENT_HANDLED = "com.frolo.muse.ui.main.INTENT_HANDLED"
        private const val EXTRA_OPEN_PLAYER = "com.frolo.muse.ui.main.OPEN_PLAYER"
        private const val EXTRA_TAB_INDEX = "com.frolo.muse.ui.main.LAST_TAB_INDEX"
        private const val EXTRA_NAV_KIND_OF_MEDIA = "com.frolo.muse.ui.main.NAV_KIND_OF_MEDIA"
        private const val EXTRA_NAV_MEDIA_ID = "com.frolo.muse.ui.main.NAV_MEDIA_ID"

        const val INDEX_LIBRARY = FragNavController.TAB1
        const val INDEX_EQUALIZER = FragNavController.TAB2
        const val INDEX_SEARCH = FragNavController.TAB3
        const val INDEX_SETTINGS = FragNavController.TAB4

        private const val TAB_INDEX_DEFAULT = 0

        private inline fun FragNavController.doIfStateNotSaved(block: FragNavController.() -> Unit) {
            if (!isStateSaved) block.invoke(this)
        }

        @JvmStatic
        fun newIntent(context: Context, tabIndex: Int = INDEX_LIBRARY): Intent =
            Intent(context, MainActivity::class.java).putExtra(EXTRA_TAB_INDEX, tabIndex)

        @JvmStatic
        fun newIntent(context: Context, openPlayer: Boolean): Intent =
            Intent(context, MainActivity::class.java).putExtra(EXTRA_OPEN_PLAYER, openPlayer)

        private fun newNavMediaIntent(context: Context, media: Media): Intent {
            return Intent(context, MainActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                //.addCategory(Intent.CATEGORY_DEFAULT)
                .putExtra(EXTRA_NAV_KIND_OF_MEDIA, media.kind)
                .putExtra(EXTRA_NAV_MEDIA_ID, media.id)
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