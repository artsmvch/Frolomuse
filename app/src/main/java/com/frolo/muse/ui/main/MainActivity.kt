package com.frolo.muse.ui.main

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.Trace
import com.frolo.muse.arch.observe
import com.frolo.muse.engine.Player
import com.frolo.muse.toPx
import com.frolo.muse.ui.PlayerHostActivity
import com.frolo.muse.ui.base.BackPressHandler
import com.frolo.muse.ui.base.FragmentNavigator
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.main.audiofx.AudioFxFragment
import com.frolo.muse.ui.main.library.LibraryFragment
import com.frolo.muse.ui.main.library.search.SearchFragment
import com.frolo.muse.ui.main.player.PlayerFragment
import com.frolo.muse.ui.main.player.mini.MiniPlayerFragment
import com.frolo.muse.ui.main.settings.AppBarSettingsFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.max
import kotlin.math.pow


class MainActivity : PlayerHostActivity(),
        FragmentNavigator {

    companion object {
        private const val RC_READ_STORAGE = 1043

        private const val EXTRA_TAB_INDEX = "last_tab_index"

        const val INDEX_LIBRARY = FragNavController.TAB1
        const val INDEX_EQUALIZER = FragNavController.TAB2
        const val INDEX_SEARCH = FragNavController.TAB3
        const val INDEX_SETTINGS = FragNavController.TAB4

        private const val TAB_INDEX_DEFAULT = 0

        fun newIntent(context: Context, tabIndex: Int = INDEX_LIBRARY): Intent =
                Intent(context, MainActivity::class.java).putExtra(EXTRA_TAB_INDEX, tabIndex)
    }

    /*presentation*/
    private val viewModel: MainViewModel by lazy {
        val vmFactory = requireApp()
                .appComponent
                .provideVMFactory()
        ViewModelProviders.of(this, vmFactory)[MainViewModel::class.java]
    }

    // Fragment controller
    private var pendingFragControllerInitialization = false
    private val isFragmentManagerStateSaved: Boolean get() = supportFragmentManager.isStateSaved
    private var fragNavController: FragNavController? = null
    private var currTabIndex = TAB_INDEX_DEFAULT

    // Rate Dialog
    private var rateDialog: Dialog? = null

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
                if (f is NoClipping) {
                    f.removeClipping(0, 0, 0, 56f.toPx(v.context).toInt())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }

    private fun loadUI() {
        val bottomNavCornerRadius = 48f.toPx(this)

        bottom_navigation_view.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(Color.WHITE)
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, bottomNavCornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, bottomNavCornerRadius)
                .build()
        }

        sliding_player_layout.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(Color.WHITE)
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, bottomNavCornerRadius)
                    .setTopRightCorner(CornerFamily.ROUNDED, bottomNavCornerRadius)
                    .build()
        }

        mini_player_container.setOnClickListener {
            expandSlidingPlayer()
        }

        btn_slide_down.setOnClickListener {
            collapseSlidingPlayer()
        }

        handleSlide(0.0f)
    }

    override fun onRestart() {
        super.onRestart()
        if (pendingFragControllerInitialization) {
            pendingFragControllerInitialization = false
            initializeFragNavController(lastSavedInstanceState)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()

        with(BottomSheetBehavior.from(sliding_player_layout)) {
            addBottomSheetCallback(bottomSheetCallback)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()

        with(BottomSheetBehavior.from(sliding_player_layout)) {
            removeBottomSheetCallback(bottomSheetCallback)
        }
    }

    override fun onStop() {
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
        return if (item.itemId == android.R.id.home) {
            fragNavController?.let { controller ->
                if (!controller.isRootFragment && controller.popFragment().not())
                    finish()
            }
            true
        } else super.onOptionsItemSelected(item)
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
                    viewModel.onReadStoragePermissionGranted()
                } else {
                    viewModel.onReadStoragePermissionDenied()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNewIntent(intent)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_TAB_INDEX, currTabIndex)
        fragNavController?.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        val behavior = BottomSheetBehavior.from(sliding_player_layout)
        if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }

        fragNavController.let { controller ->
            if (isFragmentManagerStateSaved) {
                super.onBackPressed()
            }

            if (controller == null) { // not even initialized yet
                finish()
                return
            }

            val current = controller.currentFrag
            if (current is BackPressHandler
                    /*
                    Also need to check if the fragment has a view created,
                    so it is able to handle the back press
                    */
                    && current.view != null) {
                if (current.onBackPress()) { // fragment successfully handled it itself
                    return
                }
            }

            if (controller.isRootFragment) {
                // Just call finish. Calling onBackPressed() causes popping bac stack from the fragment manager.
                // This will simply removes PlayerHolderFragment and not finish the activity.
                // This is not what we want.
                //super.onBackPressed()
                finish()
            } else if (controller.popFragment().not()) { // no fragments left in the stack
                // Just call finish. Calling onBackPressed() causes popping bac stack from the fragment manager.
                // This will simply removes PlayerHolderFragment and not finish the activity.
                // This is not what we want.
                //super.onBackPressed()
                finish()
            }
        }
    }

    override fun pushFragment(newFragment: Fragment) {
        if (isFragmentManagerStateSaved)
            return
        fragNavController?.pushFragment(newFragment)
    }

    override fun pop() {
        if (isFragmentManagerStateSaved)
            return
        fragNavController?.let { controller ->
            val currDialogFrag = controller.currentDialogFrag
            if (currDialogFrag != null
                    && currDialogFrag.isAdded) {
                // There's a dialog fragment opened.
                // Clear it first.
                controller.clearDialogFragment()
            } else {
                val stack = controller.currentStack
                if (stack != null && stack.size > 1) {
                    // pop stack only if it's not null and its size is more than 1.
                    controller.popFragments(1)
                } else {
                    // Stack is empty or has only root fragment.
                    // Let's finish the activity.
                    finish()
                }
            }
        }
    }

    override fun pushDialog(newDialog: DialogFragment) {
        if (isFragmentManagerStateSaved)
            return

        fragNavController?.showDialogFragment(newDialog)
    }

    override fun onPlayerConnected(player: Player) {
        requireApp().onPlayerConnected(player)
        viewModel.onPlayerConnected(player)
        initializeFragNavController(lastSavedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container_player, PlayerFragment())
            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.mini_player_container, MiniPlayerFragment())
            .commit()
    }

    override fun onPlayerDisconnected() {
        viewModel.onPlayerDisconnected()
        requireApp().onPlayerDisconnected()
        finish()
    }

    // return true if created successfully
    private fun initializeFragNavController(savedInstanceState: Bundle?): Boolean {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.isStateSaved) {
            pendingFragControllerInitialization = true
            return false
        }

        fragNavController = FragNavController(fragmentManager, R.id.container).apply {
            //fragmentHideStrategy = FragNavController.REMOVE
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
            if (isFragmentManagerStateSaved)
                return@setOnNavigationItemSelectedListener true

            val safeFragNavController = fragNavController
                    ?: return@setOnNavigationItemSelectedListener false

            when (menuItem.itemId) {
                R.id.nav_library -> {
                    safeFragNavController.switchTab(INDEX_LIBRARY)
                    currTabIndex = 0
                    true
                }

                R.id.nav_equalizer -> {
                    safeFragNavController.switchTab(INDEX_EQUALIZER)
                    currTabIndex = 1
                    true
                }

                R.id.nav_search -> {
                    safeFragNavController.switchTab(INDEX_SEARCH)
                    currTabIndex = 2
                    true
                }

                R.id.nav_settings -> {
                    safeFragNavController.switchTab(INDEX_SETTINGS)
                    currTabIndex = 3
                    true
                }

                else -> false
            }
        }
        bottom_navigation_view.setOnNavigationItemReselectedListener { fragNavController?.clearStack() }
        bottom_navigation_view.selectedItemId = when(currTabIndex) {
            INDEX_LIBRARY -> R.id.nav_library
            //INDEX_PLAYER -> R.id.nav_player
            INDEX_EQUALIZER -> R.id.nav_equalizer
            INDEX_SEARCH -> R.id.nav_search
            INDEX_SETTINGS -> R.id.nav_settings
            else -> R.id.nav_library
        }
        return true
    }

    private fun showRateDialog() {
        rateDialog?.dismiss()

        val dialog = RateDialog(this) { dialog, what ->
            dialog.dismiss()
            when (what) {
                RateDialog.Button.NO -> viewModel.onDismissRate()
                RateDialog.Button.REMIND_LATER -> viewModel.onWishingAskingLater()
                RateDialog.Button.RATE -> viewModel.onApproveToRate()
            }
        }

        rateDialog = dialog.apply {
            setOnCancelListener { viewModel.onCancelledRateDialog() }
            show()
        }
    }

    private fun handleNewIntent(intent: Intent) {
        val extraTabIndex = intent.getIntExtra(EXTRA_TAB_INDEX, currTabIndex)
        if (extraTabIndex != currTabIndex) {
            bottom_navigation_view.selectedItemId = when(extraTabIndex) {
                INDEX_LIBRARY -> R.id.nav_library
                //INDEX_PLAYER -> R.id.nav_player
                INDEX_EQUALIZER -> R.id.nav_equalizer
                INDEX_SEARCH -> R.id.nav_search
                INDEX_SETTINGS -> R.id.nav_settings
                else -> R.id.nav_library
            }
        }
    }

    private fun checkReadStoragePermission(requestIfNeeded: Boolean) {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.onReadStoragePermissionGranted()
        } else if (requestIfNeeded) {
            requestPermissions(arrayOf(permission), RC_READ_STORAGE)
        }
    }

    private fun requestReadStoragePermission() {
        checkReadStoragePermission(true)
    }

    private fun observerViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            askToRateEvent.observe(owner) {
                showRateDialog()
            }

            askReadStoragePermissionsEvent.observe(owner) {
                requestReadStoragePermission()
            }
        }
    }

    private fun expandSlidingPlayer() {
        BottomSheetBehavior.from(sliding_player_layout).state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun collapseSlidingPlayer() {
        BottomSheetBehavior.from(sliding_player_layout).state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun handleSlide(slideOffset: Float) {
        bottom_navigation_view.also { child ->
            val heightToAnimate = slideOffset * child.height
            child.animate()
                    .translationY(heightToAnimate)
                    .setInterpolator(DecelerateInterpolator())
                    .setDuration(0)
                    .start()
        }

        view_dim_overlay.alpha = 1 - (1 - slideOffset).pow(2)

        mini_player_container.alpha = max(0f, 1f - slideOffset * 4)
        mini_player_container.touchesDisabled = slideOffset > 0.4

        btn_slide_down.alpha = max(0f, slideOffset * 2 - 1)
        btn_slide_down.rotation = (1 - slideOffset) * 180
        btn_slide_down.isEnabled = slideOffset > 0.5

        (sliding_player_layout.background as? MaterialShapeDrawable)?.apply {
            val targetColor = StyleUtil.readColorAttrValue(this@MainActivity, R.attr.colorPrimary)
            fillColor = ColorStateList.valueOf(ColorUtils.blendARGB(Color.WHITE, targetColor, (1 - slideOffset)))

            val cornerRadius = 48f.toPx(this@MainActivity) * (1 - slideOffset)
            Trace.d("MainActivitySlide", "cornerRadius=$cornerRadius")
            this.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCorner(CornerFamily.ROUNDED, cornerRadius)
                .setTopRightCorner(CornerFamily.ROUNDED, cornerRadius)
                .build()
        }
    }

}