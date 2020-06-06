package com.frolo.muse.ui.main

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.appcompat.view.ActionMode
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.Logger
import com.frolo.muse.arch.observe
import com.frolo.muse.engine.Player
import com.frolo.muse.dp2px
import com.frolo.muse.model.media.*
import com.frolo.muse.ui.PlayerHostActivity
import com.frolo.muse.ui.ThemeHandler
import com.frolo.muse.ui.base.*
import com.frolo.muse.ui.main.audiofx.AudioFxFragment
import com.frolo.muse.ui.main.library.LibraryFragment
import com.frolo.muse.ui.main.library.search.SearchFragment
import com.frolo.muse.ui.main.player.mini.MiniPlayerFragment
import com.frolo.muse.ui.main.settings.AppBarSettingsFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
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


// TODO: show some splash until the player is connected and the fragment are loaded
class MainActivity : PlayerHostActivity(),
        FragmentNavigator,
        PlayerSheetCallback,
        ThemeHandler {

    // Reference to the presentation layer
    private val viewModel: MainViewModel by lazy {
        val vmFactory = requireApp().appComponent.provideVMFactory()
        ViewModelProviders.of(this, vmFactory).get(MainViewModel::class.java)
    }

    // Fragment controller
    private var fragNavControllerInitialized: Boolean = false
    private var fragNavController: FragNavController? = null
    private var currTabIndex: Int = TAB_INDEX_DEFAULT

    // Rate Dialog
    private var rateDialog: Dialog? = null

    // RES Permission Explanation
    private var resPermissionExplanationDialog: Dialog? = null

    // Active support action mode
    private val activeActionModes = LinkedList<ActionMode>()

    private val bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                handleSlide(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
        }

    private var playerSheetFragment: PlayerSheetFragment? = null

    @get:ColorInt
    private val colorPrimary: Int by lazy {
        StyleUtil.readColorAttrValue(this, R.attr.colorPrimary)
    }

    @get:ColorInt
    private val colorPrimaryDark: Int by lazy {
        StyleUtil.readColorAttrValue(this, R.attr.colorPrimaryDark)
    }

    @get:ColorInt
    private val colorPrimarySurface: Int by lazy {
        StyleUtil.readColorAttrValue(this, R.attr.colorPrimarySurface)
    }

    @get:ColorInt
    private val colorSurface: Int by lazy {
        StyleUtil.readColorAttrValue(this, R.attr.colorSurface)
    }

    @get:ColorInt
    private val actionModeBackgroundColor: Int by lazy {
        // TODO: we need to be careful with this because the actual value of this attribute may be a drawable
        StyleUtil.readColorAttrValue(this, R.attr.actionModeBackground)
    }

    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks =
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                if (f is NoClipping) {
                    f.removeClipping(0, 0, 0, 56f.dp2px(v.context).toInt())
                }
            }
        }

    private var notHandledIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)

        setContentView(R.layout.activity_main)

        loadUI()

        // we need to determine the index
        currTabIndex = if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_TAB_INDEX)) {
            savedInstanceState.getInt(EXTRA_TAB_INDEX, TAB_INDEX_DEFAULT)
        } else {
            intent?.getIntExtra(EXTRA_TAB_INDEX, TAB_INDEX_DEFAULT) ?: TAB_INDEX_DEFAULT
        }

        requireApp().onFragmentNavigatorCreated(this)

        observerViewModel(this)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)

        observeScanStatus()

        maybeInitializeFragments(player, lastSavedInstanceState)

        // TODO: need to postpone the handling of the intent cause fragments may not be initialized
        handleIntent(intent)
    }

    private fun loadUI() {
        val bottomNavCornerRadius = 48f.dp2px(this)

        bottom_navigation_view.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(colorSurface)
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, bottomNavCornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, bottomNavCornerRadius)
                .build()
        }

        sliding_player_layout.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(colorPrimarySurface)
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, bottomNavCornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, bottomNavCornerRadius)
                .build()
        }

        mini_player_container.setOnClickListener {
            expandSlidingPlayer()
        }

        handleSlide(0.0f)
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

    override fun onRestart() {
        super.onRestart()
        maybeInitializeFragments(player, lastSavedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        with(BottomSheetBehavior.from(sliding_player_layout)) {
            addBottomSheetCallback(bottomSheetCallback)
        }

        viewModel.onStart()
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
        with(BottomSheetBehavior.from(sliding_player_layout)) {
            removeBottomSheetCallback(bottomSheetCallback)
        }

        viewModel.onStop()

        if (isFinishing) {
            supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        }

        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
        requireApp().onFragmentNavigatorDestroyed()
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
                    RESPermissionObserver.dispatchGranted(this)
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
        outState.putInt(EXTRA_TAB_INDEX, currTabIndex)
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
            // TODO: do we really need to collapse it?
            collapseSlidingPlayer()
        }
    }

    override fun pop() {
        fragNavController?.doIfStateNotSaved {
            val currDialogFrag = currentDialogFrag
            if (currDialogFrag != null
                    && currDialogFrag.isAdded) {
                // There's a dialog fragment opened.
                // Clear it first.
                clearDialogFragment()
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

    override fun playerDidConnect(player: Player) {
        requireApp().onPlayerConnected(player)
        viewModel.onPlayerConnected(player)
        maybeInitializeFragments(player, lastSavedInstanceState)
    }

    override fun playerDidDisconnect(player: Player) {
        viewModel.onPlayerDisconnected()
        requireApp().onPlayerDisconnected()
        finish()
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

        if (fragNavControllerInitialized) {
            return false
        }

        val fragmentManager = supportFragmentManager

        if (fragmentManager.isStateSaved) {
            // FragmentManager's state is saved, we cannot perform any operation on it.
            return false
        }

        fragNavController = FragNavController(fragmentManager, R.id.container).apply {
            defaultTransactionOptions = FragNavTransactionOptions
                .newBuilder()
                .customAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .build()

            rootFragmentListener = object : FragNavController.RootFragmentListener {
                override val numberOfRootFragments = 5

                override fun getRootFragment(index: Int): Fragment {
                    when (index) {
                        INDEX_LIBRARY -> return LibraryFragment.newInstance()
                        //INDEX_PLAYER -> return PlayerFragment.newInstance()
                        INDEX_EQUALIZER-> return AudioFxFragment.newInstance()
                        INDEX_SEARCH -> return SearchFragment.newInstance()
                        INDEX_SETTINGS-> return AppBarSettingsFragment.newInstance()
                    }
                    throw IllegalStateException("Unknown index: $index")
                }

            }

            initialize(currTabIndex, savedInstanceState)
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_library -> {
                    fragNavController?.doIfStateNotSaved { switchTab(INDEX_LIBRARY) }
                    currTabIndex = 0
                    true
                }

                R.id.nav_equalizer -> {
                    fragNavController?.doIfStateNotSaved { switchTab(INDEX_EQUALIZER) }
                    currTabIndex = 1
                    true
                }

                R.id.nav_search -> {
                    fragNavController?.doIfStateNotSaved { switchTab(INDEX_SEARCH) }
                    currTabIndex = 2
                    true
                }

                R.id.nav_settings -> {
                    fragNavController?.doIfStateNotSaved { switchTab(INDEX_SETTINGS) }
                    currTabIndex = 3
                    true
                }

                else -> false
            }
        }

        bottom_navigation_view.setOnNavigationItemReselectedListener {
            fragNavController?.doIfStateNotSaved { clearStack() }
        }

        bottom_navigation_view.selectedItemId = when(currTabIndex) {
            INDEX_LIBRARY -> R.id.nav_library
            INDEX_EQUALIZER -> R.id.nav_equalizer
            INDEX_SEARCH -> R.id.nav_search
            INDEX_SETTINGS -> R.id.nav_settings
            else -> R.id.nav_library
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
                peekHeight = 96f.dp2px(this@MainActivity).toInt()
            }
        }

        fragNavControllerInitialized = true

        notHandledIntent?.also { safeIntent ->
            handleIntent(safeIntent)
        }

        return true
    }

    /**
     * Handles the given [intent].
     * If the fragments are not initialized yet then this method does nothing
     * but marks [intent] as not handled (see [notHandledIntent] so that
     * it will be handled later when the fragments are initialized.
     */
    private fun handleIntent(intent: Intent) {
        if (!fragNavControllerInitialized) {
            // Fragments are not initialized yet
            notHandledIntent = intent
            return
        }

        // Need to clean it to prevent double-handling
        notHandledIntent = null

        if (intent.getBooleanExtra(EXTRA_INTENT_HANDLED, false)) {
            // This intent has already been handled
            return
        }

        if (intent.hasExtra(EXTRA_NAV_KIND_OF_MEDIA) && intent.hasExtra(EXTRA_NAV_MEDIA_ID)) {
            val kindOfMedia = intent.getIntExtra(EXTRA_NAV_KIND_OF_MEDIA, Media.NONE)
            val mediaId = intent.getLongExtra(EXTRA_NAV_MEDIA_ID, Media.NO_ID)
            viewModel.onNavigateToMediaIntent(kindOfMedia, mediaId)
        } else {
            // TODO: Actually this also must have a specific action
            val tabIndexExtra = intent.getIntExtra(EXTRA_TAB_INDEX, currTabIndex)
            if (tabIndexExtra != currTabIndex) {
                bottom_navigation_view.selectedItemId = when (tabIndexExtra) {
                    INDEX_LIBRARY -> R.id.nav_library
                    INDEX_EQUALIZER -> R.id.nav_equalizer
                    INDEX_SEARCH -> R.id.nav_search
                    INDEX_SETTINGS -> R.id.nav_settings
                    else -> R.id.nav_library
                }
            }
        }

        // Check if we need to open the player screen
        if (intent.getBooleanExtra(EXTRA_OPEN_PLAYER, false)) {
            expandSlidingPlayer()
        }

        // Mark this intent as handled
        intent.putExtra(EXTRA_INTENT_HANDLED, true)
    }

    private fun showRateDialog() {
        rateDialog?.dismiss()

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
            .setMessage(R.string.need_for_res_permission_explanation)
            .setCancelable(false)
            .setNegativeButton(R.string.cancel) { _, _ ->
                viewModel.onDeniedRESPermissionExplanation()
            }
            .setPositiveButton(R.string.grand_res_permission) { _, _ ->
                viewModel.onAgreedWithRESPermissionExplanation()
            }
            .show()
    }

    private fun observerViewModel(owner: LifecycleOwner) = with(viewModel) {
        askToRateEvent.observe(owner) {
            showRateDialog()
        }

        askRESPermissionsEvent.observe(owner) {
            requestRESPermission()
        }

        explainNeedForRESPermissionEvent.observe(owner) {
            explainNeedForRESPermission()
        }
    }

    fun expandSlidingPlayer() {
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
            val blendedColor = ColorUtils.blendARGB(colorSurface, colorPrimarySurface, blendRatio)
            fillColor = ColorStateList.valueOf(blendedColor)

            val cornerRadius = 48f.dp2px(this@MainActivity) * (1 - slideOffset)
            Logger.d("MainActivitySlide", "cornerRadius=$cornerRadius")
            this.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
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
        window?.statusBarColor = actionModeBackgroundColor
    }

    override fun onSupportActionModeFinished(mode: ActionMode) {
        super.onSupportActionModeFinished(mode)
        activeActionModes.remove(mode)
        if (activeActionModes.isEmpty()) {
            window?.statusBarColor = colorPrimaryDark
        }
    }

    override fun handleThemeChange() {
        if (notHandledIntent != null) {
            // Saving the not handled intent
            intent = notHandledIntent
        }
        recreate()
    }

    companion object {
        private const val RC_READ_STORAGE = 1043

        // Fragment tags
        private const val FRAG_TAG_PLAYER_SHEET = "com.frolo.muse.ui.main.PLAYER_SHEET"
        private const val FRAG_TAG_MIN_PLAYER = "com.frolo.muse.ui.main.MINI_PLAYER"

        //private const val ACTION_NAV_MEDIA = "com.frolo.muse.ui.main.ACTION_NAV_MEDIA"

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

        private fun FragNavController.doIfStateNotSaved(block: FragNavController.() -> Unit) {
            if (!isStateSaved) block.invoke(this)
        }

        //region Intent factories

        fun newIntent(context: Context, tabIndex: Int = INDEX_LIBRARY): Intent =
                Intent(context, MainActivity::class.java).putExtra(EXTRA_TAB_INDEX, tabIndex)

        fun newIntent(context: Context, openPlayer: Boolean): Intent =
                Intent(context, MainActivity::class.java).putExtra(EXTRA_OPEN_PLAYER, openPlayer)

        private fun newNavMediaIntent(context: Context, media: Media): Intent {
            return Intent(context, MainActivity::class.java)
                    .setAction(Intent.ACTION_MAIN)
                    //.addCategory(Intent.CATEGORY_DEFAULT)
                    .putExtra(EXTRA_NAV_KIND_OF_MEDIA, media.kind)
                    .putExtra(EXTRA_NAV_MEDIA_ID, media.id)
        }

        fun newSongIntent(context: Context, song: Song): Intent =
                newNavMediaIntent(context, song)

        fun newAlbumIntent(context: Context, album: Album): Intent =
                newNavMediaIntent(context, album)

        fun newArtistIntent(context: Context, artist: Artist): Intent =
                newNavMediaIntent(context, artist)

        fun newGenreIntent(context: Context, genre: Genre): Intent =
                newNavMediaIntent(context, genre)

        fun newPlaylistIntent(context: Context, playlist: Playlist): Intent =
                newNavMediaIntent(context, playlist)

        fun newMyFileIntent(context: Context, myFile: MyFile): Intent =
                newNavMediaIntent(context, myFile)

        //endregion
    }

}