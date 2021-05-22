package com.frolo.muse.ui.base

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.postDelayed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.R
import com.frolo.muse.Logger
import com.frolo.muse.di.modules.ViewModelModule
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.repository.Preferences
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable


abstract class BaseDialogFragment : AppCompatDialogFragment() {

    // Rx permissions
    private lateinit var rxPermissions: RxPermissions
    private var rxPermissionDisposable: Disposable? = null

    // Single toast
    private var errorToast: Toast? = null

    // The following members are supposed to be injected
    private var prefs: Preferences? = null
    private var vmFactory: ViewModelModule.ViewModelFactory? = null
    private var eventLogger: EventLogger? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        rxPermissions = RxPermissions(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.also { window ->
            // TODO: use Window.setWindowAnimations
            window.attributes.windowAnimations = R.style.Base_AppTheme_WindowAnimation_Dialog
        }
    }

    override fun onStop() {
        super.onStop()
        rxPermissionDisposable?.dispose()
    }

    //<editor-fold desc="Injectors">
    internal fun prefs(): Lazy<Preferences> = lazy {
        if (prefs == null) {
            prefs = requireFrolomuseApp().appComponent.providePreferences()
        }

        prefs ?:
        throw IllegalStateException("Failed to inject preferences")
    }

    internal inline fun <reified T : ViewModel> viewModel(): Lazy<T> = lazy {
        if (vmFactory == null) {
            vmFactory = requireFrolomuseApp().appComponent.provideVMFactory()
        }

        val factory = vmFactory ?:
        throw IllegalStateException("Failed to inject view model factory")

        ViewModelProviders.of(this, factory)
                .get(T::class.java)
    }

    internal fun eventLogger(): Lazy<EventLogger> = lazy {
        if (eventLogger == null) {
            eventLogger = requireFrolomuseApp().appComponent.provideEventLogger()
        }

        eventLogger ?:
        throw IllegalStateException("Failed to inject event logger")
    }
    //</editor-fold>

    protected fun setupDialogSizeByDefault(dialog: Dialog) {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        setupDialogSize(dialog, 6 * width / 7, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    protected fun setupDialogSizeRelativelyToScreen(
        dialog: Dialog,
        widthPercent: Float? = null,
        heightPercent: Float? = null
    ) {
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        val targetWidth: Int =
            if (widthPercent != null) (widthPercent * screenWidth).toInt()
            else ViewGroup.LayoutParams.MATCH_PARENT

        val targetHeight: Int =
            if (heightPercent != null) (heightPercent * screenHeight).toInt()
            else ViewGroup.LayoutParams.MATCH_PARENT

        setupDialogSize(dialog, targetWidth, targetHeight)
    }

    protected fun setupDialogSize(dialog: Dialog, width: Int, height: Int) {
        dialog.window?.also { window ->
            window.setLayout(width, height)
        }
    }

    fun requireFrolomuseApp() = requireActivity().application as FrolomuseApp

    fun isPermissionGranted(permission: String): Boolean {
        return rxPermissions.isGranted(permission)
    }

    fun requestRxPermissions(vararg permissions: String, consumer: (granted: Boolean) -> Unit) {
        rxPermissionDisposable?.dispose()
        rxPermissionDisposable = rxPermissions.request(*permissions)?.subscribe(consumer, {
            Logger.e(it)
        })
    }

    fun checkReadPermissionFor(action: () -> Unit) {
        val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
        requestRxPermissions(*permissions) { granted ->
            if (granted) action()
        }
    }

    fun checkWritePermissionFor(action: () -> Unit) {
        val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestRxPermissions(*permissions) { granted ->
            if (granted) action()
        }
    }

    fun checkReadWritePermissionsFor(action: () -> Unit) {
        val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestRxPermissions(*permissions) { granted ->
            if (granted) action()
        }
    }

    fun postLongMessage(message: String) {
        val context = context ?: return
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun postLongMessage(@StringRes stringId: Int) {
        val context = context ?: return
        Toast.makeText(context, stringId, Toast.LENGTH_LONG).show()
    }

    fun postError(error: Throwable?) {
        errorToast?.cancel()
        val msg = error?.message.let { msg ->
            if (msg.isNullOrBlank()) getString(R.string.sorry_exception) else msg
        }
        errorToast = Toast.makeText(context, msg, Toast.LENGTH_LONG).apply { show() }
    }

    protected fun dismissDelayed(delayMillis: Long) {
        val context: Context? = context
        if (context != null) {
            Handler(context.mainLooper).postDelayed(delayMillis) {
                dismissAllowingStateLoss()
            }
        } else {
            // The fragment is not attached to a context, dismiss it right now
            dismissAllowingStateLoss()
        }
    }
}
