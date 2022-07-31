package com.frolo.muse.ui.base

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.frolo.core.ui.fragment.WithCustomStatusBar
import com.frolo.core.ui.fragment.WithCustomWindowInsets
import com.frolo.debug.DebugUtils
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.Logger
import com.frolo.muse.R
import com.frolo.muse.di.activityComponent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.repository.Preferences
import com.frolo.muse.toast.DefaultToastManager
import com.frolo.muse.toast.ToastManager
import com.frolo.ui.StyleUtils
import com.frolo.ui.SystemBarUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable


abstract class BaseFragment:
    Fragment(),
    WithCustomWindowInsets,
    WithCustomStatusBar {

    // Rx permissions
    private var rxPermissions: RxPermissions? = null
    private var rxPermissionDisposable: Disposable? = null

    // Container for keyed UI actions
    private val keyedUiActions = HashMap<String, Runnable>()

    // UI Tasks
    private val uiAsyncTasks = ArrayList<AsyncTask<*, *, *>>(3)

    // Single progress dialog
    private var progressDialog: Dialog? = null

    // Single toast
    private var errorToast: Toast? = null

    // The following members are supposed to be injected
    private var prefs: Preferences? = null
    private var vmFactory: ViewModelProvider.Factory? = null
    private var eventLogger: EventLogger? = null

    protected var toastManager: ToastManager? = null
        private set

    // Custom status bar
    override val isStatusBarVisible: Boolean get() = SystemBarUtils.isLight(statusBarColor)
    @get:ColorInt
    override val statusBarColor: Int get() {
        return statusBarColorRaw //ColorUtils.setAlphaComponent(statusBarColorRaw, 192)
    }
    override val isStatusBarAppearanceLight: Boolean get() {
        return SystemBarUtils.isLight(statusBarColor)
    }
    @get:ColorInt
    protected open val statusBarColorRaw: Int get() {
        val uiContext = this.context ?: kotlin.run {
            DebugUtils.dumpOnMainThread(IllegalStateException("Fragment not attached"))
            return Color.TRANSPARENT
        }
        return StyleUtils.resolveColor(uiContext, R.attr.colorPrimary)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rxPermissions = RxPermissions(this)
        toastManager = (context as? Activity)?.let(::DefaultToastManager)
    }

    override fun onStop() {
        super.onStop()
        rxPermissionDisposable?.dispose()
    }

    override fun onDestroyView() {
        progressDialog?.cancel()

        // Disposing UI actions
        keyedUiActions.values.forEach { view?.removeCallbacks(it) }
        keyedUiActions.clear()

        // Disposing UI async tasks
        uiAsyncTasks.forEach { it.cancel(true) }
        uiAsyncTasks.clear()

        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        rxPermissions = null
        toastManager = null
    }

    //<editor-fold desc="Injectors">
    internal fun prefs(): Lazy<Preferences> = lazy {
        if (prefs == null) {
            prefs = activityComponent.providePreferences()
        }

        prefs ?:
        throw IllegalStateException("Failed to inject preferences")
    }

    internal inline fun <reified T : ViewModel> viewModel(): Lazy<T> = lazy {
        if (vmFactory == null) {
            vmFactory = activityComponent.provideViewModelFactory()
        }

        val factory = vmFactory ?:
        throw IllegalStateException("Failed to inject view model factory")

        ViewModelProviders.of(this, factory)
                .get(T::class.java)
    }

    internal fun eventLogger(): Lazy<EventLogger> = lazy {
        if (eventLogger == null) {
            eventLogger = activityComponent.provideEventLogger()
        }

        eventLogger ?:
        throw IllegalStateException("Failed to inject event logger")
    }
    //</editor-fold>

    fun requireFrolomuseApp() = requireActivity().application as FrolomuseApp

    fun isPermissionGranted(permission: String): Boolean {
        return rxPermissions?.isGranted(permission)
                ?: throw IllegalStateException("Fragment not attached")
    }

    fun requestRxPermissions(
        vararg permissions: String,
        consumer: (granted: Boolean) -> Unit
    ) {

        val rxPermissions = rxPermissions
                ?: throw IllegalStateException("Fragment not attached")

        rxPermissionDisposable?.dispose()
        rxPermissionDisposable = rxPermissions.request(*permissions)
            .subscribe(consumer, { err ->
                Logger.e(err)
                toastError(err)
            })
    }

    // Checks READ_EXTERNAL_STORAGE permission.
    // If it is granted, then the [action] callback will be called immediately.
    // Otherwise, the user will be prompt to grant permission for the action.
    inline fun checkReadPermissionFor(crossinline action: () -> Unit) {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (isPermissionGranted(permission)) {
            action.invoke()
        } else {
            requestRxPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) { granted ->
                if (granted) {
                    action.invoke()
                    RESPermissionBus.dispatch()
                }
            }
        }
    }

    // Checks WRITE_EXTERNAL_STORAGE permission.
    // If it is granted, then the [action] callback will be called immediately.
    // Otherwise, the user will be prompt to grant permission for the action.
    inline fun checkWritePermissionFor(crossinline action: () -> Unit) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (isPermissionGranted(permission)) {
            action.invoke()
        } else {
            requestRxPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE) { granted ->
                if (granted) action.invoke()
            }
        }
    }

    // Checks both READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions.
    // If they are granted, then the [action] callback will be called immediately.
    // Otherwise, the user will be prompt to grant permissions for the action.
    fun checkReadWritePermissionsFor(action: () -> Unit) {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestRxPermissions(*permissions) { granted ->
            if (granted) {
                action.invoke()
                RESPermissionBus.dispatch()
            }
        }
    }

    fun toastShortMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun toastShortMessage(@StringRes stringId: Int) {
        Toast.makeText(context, stringId, Toast.LENGTH_SHORT).show()
    }

    fun toastLongMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun toastLongMessage(@StringRes stringId: Int) {
        Toast.makeText(context, stringId, Toast.LENGTH_LONG).show()
    }

    fun toastError(error: Throwable?) {
        errorToast?.cancel()
        val msg = error?.message.let { msg ->
            if (msg.isNullOrBlank()) getString(R.string.sorry_exception) else msg
        }
        errorToast = Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).apply { show() }
    }

    fun showProgressDialog(message: String = getString(R.string.loading)) {
        progressDialog?.cancel()
        val context = this.context ?: return
        progressDialog = Dialog(context).apply {
            setContentView(R.layout.dialog_progress)
            findViewById<TextView>(R.id.tv_message).text = message
            show()
        }
    }

    fun hideProgressDialog() {
        progressDialog?.cancel()
    }

    /**
     * Posts [action] for deferred execution on the UI thread. The action will be executed
     * only if the fragment has a non-null view. The action is associated with [key],
     * so the method cancels the previously posted action associated with that key.
     */
    @UiThread
    protected fun postOnUi(key: String, action: Runnable) {
        if (view == null) {
            // there is no view
            return
        }

        // Wrapping the action to make sure the fragment has a view before executing the action
        val actionWrapper = object : Runnable {

            override fun run() {
                val r: Runnable = this
                // The action is getting fired, we no longer need to store it
                if (keyedUiActions[key] == r) {
                    keyedUiActions.remove(key)
                }

                if (view != null) {
                    action.run()
                } else {
                    // That's an error
                    Logger.e(IllegalStateException("$this wanted to run a UI action, but its view was destroyed"))
                }
            }

        }

        // Saving the new action, and removing the old one, if any
        keyedUiActions.put(key, actionWrapper)?.also { oldAction ->
            view?.removeCallbacks(oldAction)
        }
        // Posting the new action
        view?.post(actionWrapper)
    }

    protected fun invalidateOptionsMenu() {
        activity?.invalidateOptionsMenu()
    }

    /**
     * Saves the given [task] to cancel it when fragment's view is destroyed.
     * If the fragment has no created view, then the task is cancelled immediately.
     */
    protected fun saveUIAsyncTask(task: AsyncTask<*, *, *>) {
        if (view == null) {
            task.cancel(true)
        } else {
            uiAsyncTasks.add(task)
        }
    }

    override fun onApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        val safeView = this.view ?: return insets
        //return safeView.dispatchApplyWindowInsets(insets)
        safeView.updatePadding(top = insets.systemWindowInsetTop)
        return insets.consumeSystemWindowInsets()
    }

}