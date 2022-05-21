package com.frolo.muse.ui.main

import android.Manifest
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.annotation.IdRes
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
import com.frolo.muse.Logger
import com.frolo.muse.R
import com.frolo.muse.android.ViewAppSettingsIntent
import com.frolo.muse.android.getIntExtraOrNull
import com.frolo.muse.android.getIntOrNull
import com.frolo.muse.android.startActivitySafely
import com.frolo.muse.arch.observe
import com.frolo.muse.di.activityComponent
import com.frolo.muse.di.impl.navigator.AppRouterImpl
import com.frolo.muse.rating.RatingFragment
import com.frolo.muse.router.AppRouter
import com.frolo.muse.router.AppRouterStub
import com.frolo.muse.ui.IntentHandler
import com.frolo.muse.ui.PlayerHostViewModel
import com.frolo.muse.ui.ScrolledToTop
import com.frolo.muse.ui.base.*
import com.frolo.muse.ui.main.audiofx.AudioFxFragment
import com.frolo.muse.ui.main.library.LibraryFragment
import com.frolo.muse.ui.main.library.search.SearchFragment
import com.frolo.muse.ui.main.player.mini.MiniPlayerFragment
import com.frolo.muse.ui.main.settings.AppBarSettingsFragment
import com.frolo.music.model.Media
import com.frolo.player.Player
import com.frolo.ui.FragmentUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehaviorSupport
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import kotlinx.android.synthetic.main.fragment_main.*
import kotlin.math.max
import kotlin.math.pow


class MainFragment :
    BaseFragment(),
    SimpleFragmentNavigator,
    AppRouter.Provider,
    PlayerSheetCallback,
    IntentHandler,
    OnBackPressedHandler {

    private val viewModel: MainViewModel by lazy {
        val vmFactory = activityComponent.provideViewModelFactory()
        ViewModelProviders.of(this, vmFactory).get(MainViewModel::class.java)
    }

    private val appRouter by lazy {
        AppRouterImpl(
            context = requireContext(),
            navigator = this,
            expandSlidingPlayer = {
                expandSlidingPlayer()
            }
        )
    }

    private val properties by lazy { MainScreenProperties(requireActivity()) }

    private var lastSavedInstanceState: Bundle? = null
    private var pendingIntent: Intent? = null

    private var fragNavController: FragNavController? = null
    private var playerSheetFragment: PlayerSheetFragment? = null

    private var resPermissionExplanationDialog: Dialog? = null

    private val bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                handleSlide(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
        }

    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
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
                performNavActon {
                    if (this.currentDialogFrag == destroyedFragment) {
                        this.clearDialogFragment()
                    }
                }
            }
        }

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_library -> {
                    performNavActon { switchTab(INDEX_LIBRARY) }
                    true
                }
                R.id.nav_equalizer -> {
                    performNavActon { switchTab(INDEX_EQUALIZER) }
                    true
                }
                R.id.nav_search -> {
                    performNavActon { switchTab(INDEX_SEARCH) }
                    true
                }
                R.id.nav_settings -> {
                    performNavActon { switchTab(INDEX_SETTINGS) }
                    true
                }
                else -> false
            }
        }
    private val onNavigationItemReselectedListener =
        BottomNavigationView.OnNavigationItemReselectedListener {
            performNavActon {
                if (isRootFragment) {
                    currentStack?.also { safeStack ->
                        val root = if (safeStack.size == 1) safeStack.peek() else null
                        if (root != null
                            && root is ScrolledToTop
                            && FragmentUtils.isInForeground(root)) {
                            root.scrollToTop()
                        }
                    }
                }
                clearStack()
            }
        }

    private val onFinishCallback: OnFinishCallback?
        get() = activity as? OnFinishCallback
    private val onDimmedCallback: OnDimmedCallback?
        get() = activity as? OnDimmedCallback

    override fun getRouter(): AppRouter {
        if (context == null || childFragmentManager.isStateSaved) {
            return AppRouterStub()
        }
        return appRouter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lastSavedInstanceState = savedInstanceState
        super.onCreate(savedInstanceState)
        childFragmentManager.registerFragmentLifecycleCallbacks(
            fragmentLifecycleCallbacks, true)
        if (savedInstanceState == null) {
            viewModel.onFirstCreate()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUi()
        observePlayerState(viewLifecycleOwner)
        observeViewModel(viewLifecycleOwner)
        observeScanStatus(view.context, viewLifecycleOwner)
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

    private fun observeScanStatus(context: Context, owner: LifecycleOwner) {
        ScanStatusObserver.observe(
            context = context,
            lifecycleOwner = owner,
            onScanStarted = {
                toastManager?.showToastMessage(getString(R.string.scanning_started))
            },
            onScanCompleted = {
                toastManager?.showToastMessage(getString(R.string.scanning_completed))
            }
        )
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        with(BottomSheetBehavior.from(sliding_player_layout)) {
            removeBottomSheetCallback(bottomSheetCallback)
        }
        resPermissionExplanationDialog?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        childFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        lastSavedInstanceState = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            performNavActon {
                if (!popFragment()) {
                    onFinishCallback?.finish()
                }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNavController?.currentStackIndex?.also { stackIndex ->
            outState.putInt(EXTRA_TAB_INDEX, stackIndex)
        }
        fragNavController?.onSaveInstanceState(outState)
    }

    override fun handleOnBackPressed(): Boolean {
        val fragmentManager: FragmentManager = childFragmentManager
        if (fragmentManager.isStateSaved) {
            return false
        }

        val playerSheetFragment = fragmentManager.findFragmentByTag(FRAG_TAG_PLAYER_SHEET)
        if (playerSheetFragment != null
            && playerSheetFragment is OnBackPressedHandler
            && FragmentUtils.isInForeground(playerSheetFragment)
            && playerSheetFragment.handleOnBackPressed()) {
            return true
        }

        val behavior = BottomSheetBehavior.from(sliding_player_layout)
        if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return true
        }

        val controller = this.fragNavController
        if (controller == null || controller.isStateSaved) {
            return false
        }

        val currFragment = controller.currentFrag
        if (currFragment is OnBackPressedHandler
            && FragmentUtils.isInForeground(currFragment)
            && currFragment.handleOnBackPressed()) {
            return true
        }

        if (controller.isRootFragment || !controller.popFragment()) {
            onFinishCallback?.finish()
            return true
        }
        return false
    }

    override fun pushFragment(newFragment: Fragment) {
        performNavActon {
            pushFragment(newFragment)
            collapseSlidingPlayer()
        }
    }

    override fun pop() {
        performNavActon {
            val currDialogFrag = currentDialogFrag
            if (currDialogFrag != null && currDialogFrag.isAdded) {
                clearDialogFragment()
            } else {
                val stack = currentStack
                if (stack != null && stack.size > 1) {
                    popFragments(1)
                } else {
                    onFinishCallback?.finish()
                }
            }
        }
    }

    override fun pushDialog(newDialog: DialogFragment) {
        performNavActon {
            showDialogFragment(newDialog)
        }
    }

    private inline fun performNavActon(action: FragNavController.() -> Unit) {
        val safeFragNavController = this.fragNavController ?: return
        if (safeFragNavController.isStateSaved) {
            return
        }
        safeFragNavController
            .runCatching(action)
            .onFailure { Logger.e(it) }
    }

    private fun initializeContent(savedInstanceState: Bundle?): Boolean {
        if (fragNavController != null) {
            return false
        }

        val fragmentManager: FragmentManager = childFragmentManager
        if (fragmentManager.isStateSaved) {
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
                    R.anim.screen_fade_in, R.anim.screen_fade_out
                )
                .build()
            rootFragmentListener = object : FragNavController.RootFragmentListener {
                override val numberOfRootFragments: Int = getRootFragmentCount()
                override fun getRootFragment(index: Int): Fragment = getRootFragmentAt(index)
            }
            initialize(tabIndex, savedInstanceState)
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        bottom_navigation_view.setOnNavigationItemReselectedListener(onNavigationItemReselectedListener)

        val targetMenuId = getBottomMenuItemId(tabIndex)
        if (bottom_navigation_view.selectedItemId != targetMenuId) {
            bottom_navigation_view.selectedItemId = targetMenuId
        }

        val newPlayerSheetFragment = PlayerSheetFragment()
        playerSheetFragment = newPlayerSheetFragment
        fragmentManager.beginTransaction()
            .replace(R.id.container_player, newPlayerSheetFragment, FRAG_TAG_PLAYER_SHEET)
            .replace(R.id.mini_player_container, MiniPlayerFragment(), FRAG_TAG_MIN_PLAYER)
            .commit()

        RatingFragment.install(fragmentManager)

        // Finally the root layout can be visible
        root_layout.visibility = View.VISIBLE

        sliding_player_layout.doOnLayout { v ->
            with(BottomSheetBehavior.from(v)) {
                peekHeight = properties.playerSheetPeekHeight
            }
        }

        pendingIntent?.also(::handleIntent)
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

    private fun getRootFragmentCount(): Int = 4

    private fun getRootFragmentAt(index: Int): Fragment {
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

    /**
     * Handles [intent]. If the fragments were not initialized yet then
     * this method does nothing but marks [intent] as pending so that
     * it will be handled later.
     */
    override fun handleIntent(intent: Intent): Boolean {
        val safeNavController = fragNavController
        if (safeNavController == null || safeNavController.isStateSaved) {
            // Fragments are not initialized yet, or the state is saved
            pendingIntent = intent
            return false
        }

        // Need to clean it to prevent double-handling
        pendingIntent = null

        if (intent.getBooleanExtra(EXTRA_INTENT_HANDLED, false)) {
            // This intent has already been handled
            return false
        }

        if (intent.hasExtra(EXTRA_NAV_KIND_OF_MEDIA) && intent.hasExtra(EXTRA_NAV_MEDIA_ID)) {
            val kindOfMedia = intent.getIntExtra(EXTRA_NAV_KIND_OF_MEDIA, Media.NONE)
            val mediaId = intent.getLongExtra(EXTRA_NAV_MEDIA_ID, Media.NO_ID)
            viewModel.onNavigateToMediaIntent(kindOfMedia, mediaId)
        } else {
            val currTabIndex = safeNavController.currentStackIndex
            val tabIndexExtra = intent.getIntExtra(EXTRA_TAB_INDEX, currTabIndex)
            if (tabIndexExtra != currTabIndex) {
                bottom_navigation_view.selectedItemId = getBottomMenuItemId(tabIndexExtra)
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

        return true
    }

    private fun requestRESPermission() {
        val context = this.context ?: return
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED) {
            viewModel.onRESPermissionGranted()
        } else {
            requestPermissions(arrayOf(permission), RC_READ_STORAGE)
        }
    }

    private fun explainNeedForRESPermission() {
        resPermissionExplanationDialog?.cancel()
        resPermissionExplanationDialog = MaterialAlertDialogBuilder(requireContext())
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
            TransitionManager.beginDelayedTransition(root_layout, transition)
            if (isEnabled == true) {
                snowfall_view.visibility = View.VISIBLE
            } else {
                snowfall_view.visibility = View.GONE
            }
        }

        showGreetingsEvent.observe(owner) { show ->
            if (show == true) {
                //GreetingsActivity.show(this@MainActivity)
            }
        }

        askRESPermissionsEvent.observe(owner) {
            requestRESPermission()
        }

        explainNeedForRESPermissionEvent.observe(owner) {
            explainNeedForRESPermission()
        }

        openPermissionSettingsEvent.observe(owner) {
            val context = requireContext()
            val intent: Intent = ViewAppSettingsIntent(context)
            if (!context.startActivitySafely(intent)) {
                requestRESPermission()
            }
        }
    }

    private fun observePlayerState(owner: LifecycleOwner) {
        val viewModel: PlayerHostViewModel = this.viewModel
        if (viewModel.player == null) {
            // Check immediately if the player is null
            clearAllFragmentsAndState()
        }
        viewModel.playerLiveData.observe(owner) { player: Player? ->
            if (player != null) {
                // The player is connected: let's try initializing fragments
                initializeContent(lastSavedInstanceState)
            } else {
                // The player is disconnected: no need to stay here anymore
                clearAllFragmentsAndState()
            }
        }
        viewModel.isDisconnectedLiveData.observe(owner) { isDisconnected: Boolean? ->
            if (isDisconnected == true) {
                onFinishCallback?.finish()
            }
        }
    }

    private fun clearAllFragmentsAndState() {
        FragmentUtils.popAllBackStackEntriesImmediate(childFragmentManager)
        FragmentUtils.removeAllFragmentsNow(childFragmentManager)
        lastSavedInstanceState = null
    }

    private fun expandSlidingPlayer() {
        performNavActon {
            clearDialogFragment()
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
            onDimmedCallback?.onDimmed()
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

    fun interface OnFinishCallback {
        fun finish()
    }

    fun interface OnDimmedCallback {
        fun onDimmed()
    }

    companion object {

        private const val RC_READ_STORAGE = 1043

        // Fragment tags
        private const val FRAG_TAG_PLAYER_SHEET = "com.frolo.muse.ui.main.PLAYER_SHEET"
        private const val FRAG_TAG_MIN_PLAYER = "com.frolo.muse.ui.main.MINI_PLAYER"

        const val EXTRA_INTENT_HANDLED = "com.frolo.muse.ui.main.INTENT_HANDLED"
        const val EXTRA_OPEN_PLAYER = "com.frolo.muse.ui.main.OPEN_PLAYER"
        const val EXTRA_TAB_INDEX = "com.frolo.muse.ui.main.LAST_TAB_INDEX"
        const val EXTRA_NAV_KIND_OF_MEDIA = "com.frolo.muse.ui.main.NAV_KIND_OF_MEDIA"
        const val EXTRA_NAV_MEDIA_ID = "com.frolo.muse.ui.main.NAV_MEDIA_ID"

        const val INDEX_LIBRARY = FragNavController.TAB1
        const val INDEX_EQUALIZER = FragNavController.TAB2
        const val INDEX_SEARCH = FragNavController.TAB3
        const val INDEX_SETTINGS = FragNavController.TAB4

        private const val TAB_INDEX_DEFAULT = 0

        fun newInstance(): MainFragment = MainFragment()
    }

}